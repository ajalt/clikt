package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix

internal fun <RunnerT> parseArgv(
    rootCommand: BaseCliktCommand<RunnerT>,
    originalArgv: List<String>,
): CommandLineParseResult<RunnerT> {
    rootCommand.resetContext(null)
    val results = mutableListOf<CommandInvocation<RunnerT>>()
    var expandedArgv = originalArgv
    var command: BaseCliktCommand<RunnerT>? = rootCommand
    var i = 0
    while (command != null) {
        val parent = results.lastOrNull()?.command?.currentContext
        val commandResult = CommandParser(command, parent, expandedArgv, i).parse()
        i = commandResult.i
        expandedArgv = commandResult.expandedArgv
        if (commandResult.errors.isNotEmpty()) command.currentContext.errorEncountered = true

        for (e in commandResult.errors) {
            if (e is UsageError) e.context = e.context ?: command.currentContext
        }
        results += CommandInvocation(
            command,
            commandResult.optInvocations,
            commandResult.argInvocations,
            commandResult.subcommand,
            commandResult.errors
        )
        command = commandResult.subcommand
    }
    check(results.isNotEmpty())
    if (i != expandedArgv.size) {
        val lastResult = results.last()
        val error = NoSuchArgument(expandedArgv.drop(i + 1)).also {
            it.context = lastResult.command.currentContext
        }
        results[results.lastIndex] = lastResult.copy(errors = lastResult.errors + error)
    }
    return CommandLineParseResult(results, originalArgv, expandedArgv)
}

private class CommandParser<RunnerT>(
    private val command: BaseCliktCommand<RunnerT>,
    parentContext: Context?,
    argv: List<String>,
    startingIndex: Int,
) {
    private var tokens = argv
    private val context = command.currentContext
    private val aliases = command.aliases()
    private val extraSubcommands = parentContext?.selfAndAncestors()
        ?.firstOrNull { it.command.allowMultipleSubcommands }?.command
        ?._subcommands?.associate {
            // We could avoid this cast if Context was generic, but it doesn't seem worth it
            @Suppress("UNCHECKED_CAST")
            it.commandName to it as BaseCliktCommand<RunnerT>
        } ?: emptyMap()
    private val localSubcommands = command._subcommands.associateBy { it.commandName }

    // If an ancestor command allows multiple subcommands, include its subcommands so
    // that we can chain into them
    private val allSubcommands = extraSubcommands + localSubcommands
    private val optionsByName = mutableMapOf<String, Option>()
    private val numberOption = command._options.find { it.acceptsNumberValueWithoutName }
    private val prefixes = mutableSetOf<String>()
    private val longNames = mutableSetOf<String>()
    private val argumentTokens = mutableListOf<String>()
    private var subcommand: BaseCliktCommand<RunnerT>? = null
    private val optInvocations = mutableListOf<OptInvocation>()
    private val argInvocations = mutableListOf<ArgumentInvocation>()
    private val errors = mutableListOf<CliktError>()
    private var i = startingIndex
    private var minAliasI = i
    private val minArgCount = command._arguments.sumOf { it.nvalues.coerceAtLeast(0) }
    private val canParseSubcommands get() = argumentTokens.size >= minArgCount
    private var canParseOptions = true
    private var canExpandAtFiles = context.expandArgumentFiles

    fun parse(): CommandParseResult<RunnerT> {
        splitOptionPrefixes()
        if (printHelpOnEmptyArgsIfNecessary()) return makeResult()
        consumeTokens()
        parseArguments()
        return makeResult()
    }

    private fun consumeTokens() {
        while (i <= tokens.lastIndex) {
            val tok = tokens[i]
            val normTok = context.tokenTransformer(context, tok)
            val prefix = splitOptionPrefix(tok).first
            when {
                canExpandAtFiles
                        && tok.startsWith("@")
                        && normTok !in optionsByName -> {
                    if (tok.startsWith("@@")) {
                        argumentTokens += tok.drop(1)
                        i += 1
                    } else {
                        insertTokens(loadArgFile(normTok.drop(1), context))
                    }
                }

                canParseOptions
                        && tok == "--" -> {
                    i += 1
                    canParseOptions = false
                    canExpandAtFiles = false
                }

                canParseOptions && (
                        prefix.length > 1 && prefix in prefixes
                                || normTok in longNames
                                || isLongOptionWithEquals(prefix, tok)
                                || !context.allowGroupedShortOptions
                        ) -> {
                    consumeOptionParse(parseLongOpt(tok))
                }

                canParseOptions
                        && tok.length >= 2
                        && prefix.isNotEmpty()
                        && prefix in prefixes -> {
                    consumeOptionParse(parseShortOpt(tok))
                }

                i >= minAliasI && tok in aliases -> {
                    insertTokens(aliases.getValue(tok))
                }

                canParseSubcommands && normTok in allSubcommands -> {
                    subcommand = allSubcommands.getValue(normTok)
                    i += 1
                    break
                }

                else -> {
                    if (!context.allowInterspersedArgs) canParseOptions = false
                    argumentTokens += tok // arguments aren't transformed
                    i += 1
                }
            }
        }
    }

    private fun printHelpOnEmptyArgsIfNecessary(): Boolean {
        if (i > tokens.lastIndex && command.printHelpOnEmptyArgs) {
            errors += PrintHelpMessage(context, error = true)
            return true
        }
        return false
    }

    private fun splitOptionPrefixes() {
        for (option in command._options) {
            require(option.names.isNotEmpty() || option.secondaryNames.isNotEmpty()) {
                "options must have at least one name"
            }

            require(option.acceptsUnattachedValue || option.nvalues.last <= 1) {
                "acceptsUnattachedValue must be true if the option accepts more than one value"
            }

            for (name in option.names + option.secondaryNames) {
                optionsByName[name] = option
                if (name.length > 2) longNames += name
                prefixes += splitOptionPrefix(name).first
            }
        }
        prefixes.remove("")
    }

    private fun makeResult(): CommandParseResult<RunnerT> {
        val opts = optInvocations.groupBy({ it.opt }, { it.inv })
        return CommandParseResult(subcommand, i, errors, tokens, opts, argInvocations)
    }

    private fun isLongOptionWithEquals(prefix: String, token: String): Boolean {
        if ("=" !in token) return false
        if (prefix.isEmpty()) return false
        if (prefix.length > 1) return true
        if (context.tokenTransformer(
                context,
                token.substringBefore("=")
            ) in longNames
        ) return true
        return context.tokenTransformer(context, token.take(2)) !in optionsByName
    }

    private fun consumeOptionParse(result: OptParseResult) {
        argumentTokens += result.unknown
        optInvocations += result.known
        result.err?.let {
            errors += it
            context.errorEncountered = true
        }
        i += result.consumed
    }

    private fun insertTokens(newTokens: List<String>) {
        tokens = buildList(tokens.size + newTokens.size) {
            addAll(tokens.take(i))
            addAll(newTokens)
            addAll(tokens.drop(i + 1))
        }
        minAliasI = i + newTokens.size
    }

    private fun parseLongOpt(tok: String): OptParseResult {
        val equalsIndex = tok.indexOf('=')
        var (name, attachedValue) = if (equalsIndex >= 0) {
            tok.substring(0, equalsIndex) to tok.substring(equalsIndex + 1)
        } else {
            tok to null
        }
        name = context.tokenTransformer(context, name)
        val option = optionsByName[name] ?: if (command.treatUnknownOptionsAsArgs) {
            return OptParseResult(1, listOf(tok))
        } else {
            val possibilities = context.correctionSuggestor(
                name,
                optionsByName.filterNot { it.value.hidden }.keys.toList()
            )
            return OptParseResult(1, err = NoSuchOption(name, possibilities))
        }

        return parseOptValues(option, name, attachedValue)
    }

    private fun parseShortOpt(tok: String): OptParseResult {
        val prefix = tok[0].toString()

        if (numberOption != null && tok.drop(1).all { it.isDigit() }) {
            return OptParseResult(1, numberOption, "", listOf(tok.drop(1)))
        }

        val invocations = mutableListOf<OptInvocation>()

        for ((i, opt) in tok.withIndex()) {
            if (i == 0) continue // skip the dash

            val name = context.tokenTransformer(context, prefix + opt)
            val option = optionsByName[name] ?: if (command.treatUnknownOptionsAsArgs) {
                return OptParseResult(1, unknown = listOf(tok))
            } else {
                val possibilities = when {
                    prefix == "-" && "-$tok" in optionsByName -> listOf("-$tok")
                    else -> emptyList()
                }
                return OptParseResult(1, err = NoSuchOption(name, possibilities))
            }
            if (option.nvalues.last > 0) {
                val value = if (i < tok.lastIndex) tok.drop(i + 1) else null
                val result = parseOptValues(option, name, value)
                return result.copy(known = invocations + result.known)
            } else {
                invocations += OptInvocation(option, name, emptyList())
            }
        }
        return OptParseResult(1, invocations)
    }

    private fun parseOptValues(
        option: Option,
        name: String,
        attachedValue: String?,
    ): OptParseResult {
        val values = mutableListOf<String>()
        if (attachedValue != null) {
            values += attachedValue
        } else if (!option.acceptsUnattachedValue) {
            return OptParseResult(1, option, name, emptyList())
        }

        for (j in (i + 1)..tokens.lastIndex) {
            val tok = tokens[j]
            if (values.size >= option.nvalues.last) break
            if ((values.size >= option.nvalues.first) &&
                ((tok == "--")
                        || (tok in optionsByName)
                        || (tok in allSubcommands)
                        || (!command.treatUnknownOptionsAsArgs
                        && splitOptionPrefix(tok).first.isNotEmpty()))
            ) break
            values += tok
        }

        val consumed = values.size + if (attachedValue == null) 1 else 0
        if (values.size !in option.nvalues) {
            return OptParseResult(
                consumed,
                err = IncorrectOptionValueCount(option, name)
            )
        }

        return OptParseResult(consumed, option, name, values)
    }

    private fun parseArguments() {
        // The number of fixed size arguments that occur after an unlimited size argument. This
        // includes optional args, so it might be bigger than the number of provided values.
        val endSize = command._arguments.asReversed()
            .takeWhile { it.nvalues > 0 }
            .sumOf { it.nvalues }

        var tokenI = 0
        for (argument in command._arguments) {
            val remaining = argumentTokens.size - tokenI
            val consumed = when {
                argument.nvalues <= 0 -> maxOf(if (argument.required) 1 else 0, remaining - endSize)
                argument.nvalues > 0 && !argument.required && remaining == 0 -> 0
                else -> argument.nvalues
            }

            if (consumed > remaining) {
                val e = when (remaining) {
                    0 -> MissingArgument(argument)
                    else -> IncorrectArgumentValueCount(argument)
                }
                errors += e
                return
            }
            val values = argumentTokens.subList(tokenI, tokenI + consumed)
            argInvocations += ArgumentInvocation(argument, values)
            tokenI += consumed
        }

        handleExcessArgs(argumentTokens.size - tokenI)
    }

    private fun handleExcessArgs(excessCount: Int) {
        when {
            excessCount == 0 -> {}
            excessCount == 1 && localSubcommands.isNotEmpty() -> {
                val actual = argumentTokens.last()
                val possibilities = command.currentContext.correctionSuggestor(
                    actual, localSubcommands.keys.toList()
                )
                errors += NoSuchSubcommand(actual, possibilities)
            }

            else -> {
                errors += NoSuchArgument(argumentTokens.takeLast(excessCount))
            }
        }
    }
}


private fun loadArgFile(filename: String, context: Context): List<String> {
    return shlex(filename, context.argumentFileReader!!(filename), context.localization)
}

private data class CommandParseResult<RunnerT>(
    val subcommand: BaseCliktCommand<RunnerT>?,
    val i: Int,
    val errors: List<CliktError>,
    val expandedArgv: List<String>,
    val optInvocations: Map<Option, List<Invocation>>,
    val argInvocations: List<ArgumentInvocation>,
)

private data class OptInvocation(val opt: Option, val name: String, val values: List<String>) {
    val inv get() = Invocation(name, values)
}

private data class OptParseResult(
    val consumed: Int,
    val unknown: List<String> = emptyList(),
    val known: List<OptInvocation> = emptyList(),
    val err: CliktError? = null,
) {
    constructor(consumed: Int, invocations: List<OptInvocation>) : this(
        consumed,
        emptyList(),
        invocations
    )

    constructor(consumed: Int, opt: Option, name: String, values: List<String>)
            : this(consumed, emptyList(), listOf(OptInvocation(opt, name, values)))
}
