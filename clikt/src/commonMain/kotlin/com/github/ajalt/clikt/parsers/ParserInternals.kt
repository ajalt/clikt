package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix

internal fun <RunnerT : Function<*>> parseArgv(
    rootCommand: BaseCliktCommand<RunnerT>,
    originalArgv: List<String>,
): CommandLineParseResult<RunnerT> {
    val results = mutableListOf<CommandInvocation<RunnerT>>()
    var expandedArgv = originalArgv
    var command: BaseCliktCommand<RunnerT>? = rootCommand
    val errors = mutableListOf<CliktError>()
    var i = 0
    while (command != null) {
        val parent = results.lastOrNull()?.command?.currentContext
        val commandResult = CommandParser(command, parent, expandedArgv, i).parse()
        i = commandResult.i
        errors += commandResult.errors
        expandedArgv = commandResult.expandedArgv
        if (commandResult.errors.isNotEmpty()) command.currentContext.errorEncountered = true

        val argResult = parseArguments(commandResult.argumentTokens, command._arguments)
        argResult.err?.let { errors += it }

        val excessArgResult = handleExcessArgs(argResult, command, i, expandedArgv, commandResult)
        i = excessArgResult.first
        errors += excessArgResult.second
        for (e in errors) {
            if (e is UsageError) e.context = e.context ?: command.currentContext
        }
        results += CommandInvocation(
            command, commandResult.optInvocations, argResult.invocations
        )
        command = commandResult.subcommand
    }
    val lastInvocation = results.lastOrNull()
    if (lastInvocation != null && i != expandedArgv.size) {
        errors += NoSuchArgument(expandedArgv.drop(i + 1)).also {
            it.context = rootCommand.currentContext
        }
    }
    return CommandLineParseResult(results, originalArgv, expandedArgv, errors)
}

private class CommandParser<RunnerT : Function<*>>(
    private val command: BaseCliktCommand<RunnerT>,
    parentContext: Context?,
    argv: List<String>,
    startingIndex: Int,
) {
    private var tokens = argv
    private val context = command.resetContext(parentContext)
    private val aliases = command.aliases()
    private val subcommands = command._subcommands.associateBy { it.commandName }
    private val subcommandNames = subcommands.keys
    private val optionsByName = mutableMapOf<String, Option>()
    private val numberOption = command._options.find { it.acceptsNumberValueWithoutName }
    private val prefixes = mutableSetOf<String>()
    private val longNames = mutableSetOf<String>()
    private val argumentTokens = mutableListOf<String>()
    private var subcommand: BaseCliktCommand<RunnerT>? = null
    private var canParseOptions = true
    private var canExpandAtFiles = context.expandArgumentFiles
    private val optInvocations = mutableListOf<OptInvocation>()
    private val errors = mutableListOf<CliktError>()
    private var i = startingIndex
    private var minAliasI = i

    fun parse(): CommandParseResult<RunnerT> {
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

        if (i > tokens.lastIndex && command.printHelpOnEmptyArgs) {
            errors += PrintHelpMessage(context, error = true)
            return makeResult()
        }

        loop@ while (i <= tokens.lastIndex) {
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

                normTok in subcommands -> {
                    subcommand = subcommands.getValue(normTok)
                    i += 1
                    break@loop
                }

                else -> {
                    if (!context.allowInterspersedArgs) canParseOptions = false
                    argumentTokens += tok // arguments aren't transformed
                    i += 1
                }
            }
        }

        return makeResult()
    }

    private fun makeResult(): CommandParseResult<RunnerT> {
        val opts = optInvocations.groupBy({ it.opt }, { it.inv })
        return CommandParseResult(subcommand, i, errors, tokens, opts, argumentTokens)
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
                        || (tok in subcommandNames)
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


}

private fun parseArguments(
    argumentTokens: List<String>,
    arguments: List<Argument>,
): ArgsParseResult {
    val invocations = mutableListOf<ArgumentInvocation>()
    // The number of fixed size arguments that occur after an unlimited size argument. This
    // includes optional args, so it might be bigger than the number of provided values.
    val endSize = arguments.asReversed()
        .takeWhile { it.nvalues > 0 }
        .sumOf { it.nvalues }

    var i = 0
    for (argument in arguments) {
        val remaining = argumentTokens.size - i
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
            return ArgsParseResult(0, invocations, e)
        }
        val values = argumentTokens.subList(i, i + consumed)
        invocations += ArgumentInvocation(argument, values)
        i += consumed
    }

    val excess = argumentTokens.size - i
    return ArgsParseResult(excess, invocations, null)
}

/** Returns the new argv index and any errors */
private fun <RunnerT : Function<*>> handleExcessArgs(
    argResult: ArgsParseResult,
    command: BaseCliktCommand<RunnerT>,
    i: Int,
    expandedArgv: List<String>,
    commandResult: CommandParseResult<RunnerT>,
): Pair<Int, List<CliktError>> {
    if (argResult.excessCount <= 0) return i to emptyList()
    val hasMultipleSubAncestor = command.currentContext.ancestors().any {
        it.command.allowMultipleSubcommands
    }
    if (hasMultipleSubAncestor) {
        return expandedArgv.size - argResult.excessCount to emptyList()
    } else {
        val subcommandNames = command._subcommands.map { it.commandName }
        val error = when {
            argResult.excessCount == 1 && subcommandNames.isNotEmpty() -> {
                val actual = commandResult.argumentTokens.last()
                val possibilities = command.currentContext.correctionSuggestor(
                    actual, subcommandNames
                )
                NoSuchSubcommand(actual, possibilities)
            }

            else -> {
                NoSuchArgument(commandResult.argumentTokens.takeLast(argResult.excessCount))
            }
        }
        return i to listOf(error)
    }
}


private fun loadArgFile(filename: String, context: Context): List<String> {
    return shlex(filename, context.argumentFileReader!!(filename), context.localization)
}

private data class CommandParseResult<RunnerT : Function<*>>(
    val subcommand: BaseCliktCommand<RunnerT>?,
    val i: Int,
    val errors: List<CliktError>,
    val expandedArgv: List<String>,
    val optInvocations: Map<Option, List<Invocation>>,
    val argumentTokens: List<String>,
)

private data class ArgsParseResult(
    val excessCount: Int,
    val invocations: List<ArgumentInvocation>,
    val err: CliktError?,
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
