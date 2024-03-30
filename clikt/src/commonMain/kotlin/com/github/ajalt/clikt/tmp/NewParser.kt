package com.github.ajalt.clikt.tmp

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix
import com.github.ajalt.clikt.parsers.*

internal fun parseArgv(command: CliktCommand, originalArgv: List<String>): CommandLineParseResult {
    val results = mutableListOf<CommandInvocation>()
    var expandedArgv = originalArgv
    val errors = mutableListOf<CliktError>()
    var i = 0
    do {
        val parent = results.lastOrNull()?.command?.currentContext
        val commandResult = parseCommand(command, parent, expandedArgv, i)
        check(commandResult.i in expandedArgv.indices) {
            "Internal error: index $i out of bounds ${expandedArgv.indices}"
        }

        i = commandResult.i
        errors += commandResult.errors
        expandedArgv = commandResult.expandedArgv
        if (commandResult.errors.isNotEmpty()) command.currentContext.errorEncountered = true

        val argResult = parseArguments(commandResult.argumentTokens, command._arguments)
        val excessArgResult = handleExcessArgs(argResult, command, i, expandedArgv, commandResult)
        i = excessArgResult.first
        errors += excessArgResult.second
        errors.forEach { e ->
            (e as? UsageError)?.let { it.context = it.context ?: command.currentContext }
        }
        results += CommandInvocation(command, commandResult.optInvocations, argResult.invocations)
    } while (commandResult.subcommand != null)
    val lastInvocation = results.lastOrNull()
    if (lastInvocation != null && i != expandedArgv.lastIndex) {
        errors += NoSuchArgument(expandedArgv.drop(i + 1)).also {
            it.context = command.currentContext
        }
    }
    return CommandLineParseResult(results, originalArgv, expandedArgv, errors)
}

private fun parseCommand(
    command: CliktCommand,
    parentContext: Context?,
    argv: List<String>,
    startingIndex: Int,
): CommandParseResult {
    var tokens = argv
    val context = command.resetContext(parentContext)
    val aliases = command.aliases()
    val subcommands = command._subcommands.associateBy { it.commandName }
    val subcommandNames = subcommands.keys
    val optionsByName = mutableMapOf<String, Option>()
    val numberOption = command._options.find { it.acceptsNumberValueWithoutName }
    val prefixes = mutableSetOf<String>()
    val longNames = mutableSetOf<String>()
    val argumentTokens = mutableListOf<String>()
    var subcommand: CliktCommand? = null
    var canParseOptions = true
    var canExpandAtFiles = context.expandArgumentFiles
    val optInvocations = mutableListOf<OptInvocation>()
    val errors = mutableListOf<CliktError>()
    var i = startingIndex
    var minAliasI = i

    fun makeResult(): CommandParseResult {
        val opts = optInvocations.groupBy({ it.opt }, { it.inv })
        return CommandParseResult(subcommand, i, errors, tokens, opts, argumentTokens)
    }

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

    if (tokens.isEmpty() && command.printHelpOnEmptyArgs) {
        errors += PrintHelpMessage(context, error = true)
        return makeResult()
    }

    fun isLongOptionWithEquals(prefix: String, token: String): Boolean {
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

    fun consumeOptionParse(result: OptParseResult) {
        argumentTokens += result.unknown
        optInvocations += result.known
        result.err?.let {
            errors += it
            context.errorEncountered = true
        }
        i += result.consumed
    }

    fun insertTokens(newTokens: List<String>) {
        tokens = buildList(tokens.size + newTokens.size) {
            addAll(tokens.take(i))
            addAll(newTokens)
            addAll(tokens.drop(i + 1))
        }
        minAliasI = i + newTokens.size
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
                consumeOptionParse(
                    parseLongOpt(
                        command.treatUnknownOptionsAsArgs,
                        context,
                        tokens,
                        tok,
                        i,
                        optionsByName,
                        subcommandNames
                    )
                )
            }

            canParseOptions
                    && tok.length >= 2
                    && prefix.isNotEmpty()
                    && prefix in prefixes -> {
                consumeOptionParse(
                    parseShortOpt(
                        command.treatUnknownOptionsAsArgs,
                        context,
                        tokens,
                        tok,
                        i,
                        optionsByName,
                        numberOption,
                        subcommandNames
                    )
                )
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

/** Returns the new argv index and any errors */
private fun handleExcessArgs(
    argResult: ArgsParseResult,
    command: CliktCommand,
    i: Int,
    expandedArgv: List<String>,
    commandResult: CommandParseResult,
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


private fun parseLongOpt(
    ignoreUnknown: Boolean,
    context: Context,
    tokens: List<String>,
    tok: String,
    index: Int,
    optionsByName: Map<String, Option>,
    subcommandNames: Set<String>,
): OptParseResult {
    val equalsIndex = tok.indexOf('=')
    var (name, attachedValue) = if (equalsIndex >= 0) {
        tok.substring(0, equalsIndex) to tok.substring(equalsIndex + 1)
    } else {
        tok to null
    }
    name = context.tokenTransformer(context, name)
    val option = optionsByName[name] ?: if (ignoreUnknown) {
        return OptParseResult(1, listOf(tok))
    } else {
        val possibilities = context.correctionSuggestor(
            name,
            optionsByName.filterNot { it.value.hidden }.keys.toList()
        )
        return OptParseResult(1, err = NoSuchOption(name, possibilities))
    }

    return parseOptValues(
        option,
        name,
        ignoreUnknown,
        tokens,
        index,
        attachedValue,
        optionsByName,
        subcommandNames
    )
}

private fun parseOptValues(
    option: Option,
    name: String,
    ignoreUnknown: Boolean,
    tokens: List<String>,
    index: Int,
    attachedValue: String?,
    optionsByName: Map<String, Option>,
    subcommandNames: Set<String>,
): OptParseResult {
    val values = mutableListOf<String>()
    if (attachedValue != null) {
        values += attachedValue
    } else if (!option.acceptsUnattachedValue) {
        return OptParseResult(1, option, name, emptyList())
    }

    for (i in (index + 1)..tokens.lastIndex) {
        val tok = tokens[i]
        if (values.size >= option.nvalues.last) break
        if (values.size >= option.nvalues.first && (tok == "--"
                    || tok in optionsByName
                    || tok in subcommandNames
                    || !ignoreUnknown && splitOptionPrefix(tok).first.isNotEmpty())
        ) {
            break
        }

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

private fun parseShortOpt(
    ignoreUnknown: Boolean,
    context: Context,
    tokens: List<String>,
    tok: String,
    index: Int,
    optionsByName: Map<String, Option>,
    numberOption: Option?,
    subcommandNames: Set<String>,
): OptParseResult {
    val prefix = tok[0].toString()

    if (numberOption != null && tok.drop(1).all { it.isDigit() }) {
        return OptParseResult(1, numberOption, "", listOf(tok.drop(1)))
    }

    val invocations = mutableListOf<OptInvocation>()

    for ((i, opt) in tok.withIndex()) {
        if (i == 0) continue // skip the dash

        val name = context.tokenTransformer(context, prefix + opt)
        val option = optionsByName[name] ?: if (ignoreUnknown) {
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
            val result = parseOptValues(
                option,
                name,
                ignoreUnknown,
                tokens,
                index,
                value,
                optionsByName,
                subcommandNames
            )
            return result.copy(known = invocations + result.known)
        } else {
            invocations += OptInvocation(option, name, emptyList())
        }
    }
    return OptParseResult(1, invocations)
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

private fun loadArgFile(filename: String, context: Context): List<String> {
    return shlex(filename, context.argumentFileReader!!(filename), context.localization)
}

private data class CommandParseResult(
    val subcommand: CliktCommand?,
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
