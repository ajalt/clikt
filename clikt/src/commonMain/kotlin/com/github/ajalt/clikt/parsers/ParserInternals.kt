package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.hasEnvvarOrSourcedValue
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix

internal fun <T : BaseCliktCommand<T>> parseArgv(
    rootCommand: T,
    originalArgv: List<String>,
): CommandLineParseResult<T> {
    rootCommand.resetContext()
    val result = parseCommandArgv(rootCommand, originalArgv, 0)
    return CommandLineParseResult(result.invocation, originalArgv, result.argv)
}

/** Parse argv for the command, and recursively for its invoked subcommands */
private fun <T : BaseCliktCommand<T>> parseCommandArgv(
    command: T,
    argv: List<String>,
    i: Int,
): CommandArgvParseResult<T> {
    val rootResult = CommandParser(command, argv, i).parse()
    val subcommands = mutableListOf<CommandInvocation<T>>()
    var nextCommand: T? = rootResult.nextCommand
    var nextArgv = rootResult.expandedArgv
    var nextI = rootResult.i
    while (nextCommand != null && nextCommand.currentContext.parent?.command == command) {
        val result = parseCommandArgv(nextCommand, nextArgv, nextI)
        nextCommand = result.nextCommand
        nextArgv = result.argv
        nextI = result.i
        subcommands += result.invocation
    }

    return CommandArgvParseResult(
        rootResult.toInvocation(subcommands), nextCommand, nextArgv, nextI
    )
}

private class CommandArgvParseResult<T : BaseCliktCommand<T>>(
    val invocation: CommandInvocation<T>,
    val nextCommand: T?,
    val argv: List<String>,
    val i: Int,
)

private class CommandParser<T : BaseCliktCommand<T>>(
    private val command: T,
    argv: List<String>,
    startingIndex: Int,
) {
    private var tokens = argv
    private val context = command.currentContext
    private val aliases = command.aliases()

    // Subcommands of an ancestor with allowMultipleSubcommands, since any of them could be next
    private val ancestorMultipleSubcommands = context.ancestors()
        .firstOrNull { it.command.allowMultipleSubcommands }
        ?.command?._subcommands?.associate {
            @Suppress("UNCHECKED_CAST")
            it.commandName to it as T
        } ?: emptyMap()
    private val localSubcommands = command._subcommands.associateBy { it.commandName }

    // If an ancestor command allows multiple subcommands, include its subcommands so
    // that we can chain into them
    private val allSubcommands = ancestorMultipleSubcommands + localSubcommands
    private val optionsByName = mutableMapOf<String, Option>()
    private val numberOption = command._options.find { it.acceptsNumberValueWithoutName }
    private val prefixes = mutableSetOf<String>()
    private val longNames = mutableSetOf<String>()
    private val argumentTokens = mutableListOf<String>()
    private var subcommand: T? = null
    private val optInvocations = mutableListOf<OptInvocation>()
    private val argInvocations = mutableListOf<ArgumentInvocation>()
    private val errors = mutableListOf<CliktError>()
    private var i = startingIndex
    private var minAliasI = i
    private val minArgCount = command._arguments.sumOf {
        if (it.required) it.nvalues.coerceAtLeast(0) else 0
    }
    private val canParseSubcommands get() = argumentTokens.size >= minArgCount
    private var canParseOptions = true
    private var canExpandAtFiles = context.expandArgumentFiles

    fun parse(): CommandParseResult<T> {
        splitOptionPrefixes()
        if (printHelpOnEmptyArgsIfNecessary()) return makeResult()
        consumeTokens()
        parseArguments()
        return makeResult()
    }

    private fun consumeTokens() {
        while (i <= tokens.lastIndex) {
            val tok = tokens[i]
            val normTok = context.transformToken(context, tok)
            val prefix = splitOptionPrefix(tok).first
            when {
                i >= minAliasI && tok in aliases -> {
                    insertTokens(aliases.getValue(tok))
                }

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
        if (i > tokens.lastIndex && command.printHelpOnEmptyArgs
            && command._options.none { it.hasEnvvarOrSourcedValue(context, emptyList()) }
        ) {
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

    private fun makeResult(): CommandParseResult<T> {
        val opts = optInvocations.groupBy({ it.opt }, { it.inv })
        for (e in errors) {
            if (e is UsageError) e.context = e.context ?: context
            context.errorEncountered = true
        }
        val nextCommand = subcommand.takeIf { errors.isEmpty() }
        return CommandParseResult(command, nextCommand, i, errors, tokens, opts, argInvocations)
    }

    private fun isLongOptionWithEquals(prefix: String, token: String): Boolean {
        if ("=" !in token) return false
        if (prefix.isEmpty()) return false
        if (prefix.length > 1) return true
        if (context.transformToken(
                context,
                token.substringBefore("=")
            ) in longNames
        ) return true
        return context.transformToken(context, token.take(2)) !in optionsByName
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
        tokens = listOf(tokens.take(i), newTokens, tokens.drop(i + 1)).flatten()
        minAliasI = i + newTokens.size
    }

    private fun parseLongOpt(tok: String): OptParseResult {
        val equalsIndex = tok.indexOf('=')
        var (name, attachedValue) = if (equalsIndex >= 0) {
            tok.substring(0, equalsIndex) to tok.substring(equalsIndex + 1)
        } else {
            tok to null
        }
        name = context.transformToken(context, name)
        val option = optionsByName[name] ?: if (command.treatUnknownOptionsAsArgs) {
            return OptParseResult(1, listOf(tok))
        } else {
            val possibilities = context.suggestTypoCorrection(
                name,
                optionsByName.filterNot { it.value.hidden }.keys.toList()
            )
            return OptParseResult(1, err = createNoSuchOption(name, possibilities))
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

            val name = context.transformToken(context, prefix + opt)
            val option = optionsByName[name] ?: if (command.treatUnknownOptionsAsArgs) {
                return OptParseResult(1, unknown = listOf(tok))
            } else {
                val possibilities = when {
                    prefix == "-" && "-$tok" in optionsByName -> listOf("-$tok")
                    else -> emptyList()
                }
                return OptParseResult(1, err = createNoSuchOption(name, possibilities))
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

    private fun createNoSuchOption(name: String, possibilities: List<String>): NoSuchOption {
        if (possibilities.isEmpty()) {
            val c = allSubcommands.values.find { it._options.any { o -> name in o.names } }
            if (c != null) return NoSuchOption(name, subcommand = c.commandName)
        }
        return NoSuchOption(name, possibilities)
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

        // This might not be enough values for the option, but we'll report that during finalization
        val consumed = values.size + if (attachedValue == null) 1 else 0
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

            val toIndex = (tokenI + consumed).coerceAtMost(argumentTokens.size)
            val values = argumentTokens.subList(tokenI, toIndex)
            argInvocations += ArgumentInvocation(argument, values)

            if (consumed > remaining) {
                // Not enough values for remaining args, we'll report it during finalization
                return
            }

            tokenI += consumed
        }

        handleExcessArgs(argumentTokens.size - tokenI)
    }

    private fun handleExcessArgs(excessCount: Int) {
        when {
            excessCount == 0 -> {}
            excessCount == 1 && localSubcommands.isNotEmpty() -> {
                val actual = argumentTokens.last()
                val possibilities = command.currentContext.suggestTypoCorrection(
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
    return shlex(filename, context.readArgumentFile!!(filename), context.localization)
}

private data class CommandParseResult<T : BaseCliktCommand<T>>(
    val command: T,
    val nextCommand: T?,
    val i: Int,
    val errors: List<CliktError>,
    val expandedArgv: List<String>,
    val optInvocations: Map<Option, List<OptionInvocation>>,
    val argInvocations: List<ArgumentInvocation>,
) {
    fun toInvocation(subcommands: List<CommandInvocation<T>>) = CommandInvocation(
        command,
        optInvocations,
        argInvocations,
        subcommands,
        errors,
    )
}

private data class OptInvocation(val opt: Option, val name: String, val values: List<String>) {
    val inv get() = OptionInvocation(name, values)
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
