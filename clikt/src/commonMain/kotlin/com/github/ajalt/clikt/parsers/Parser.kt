package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.internal.finalizeOptions
import com.github.ajalt.clikt.mpp.readFileIfExists
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix

private data class OptInvocation(val opt: Option, val name: String, val values: List<String>) {
    val inv get() = Invocation(name, values)
}

private data class OptParseResult(val consumed: Int, val unknown: List<String>, val known: List<OptInvocation>) {
    constructor(consumed: Int, invocations: List<OptInvocation>) : this(consumed, emptyList(), invocations)
    constructor(consumed: Int, opt: Option, name: String, values: List<String>)
            : this(consumed, emptyList(), listOf(OptInvocation(opt, name, values)))
}

internal object Parser {
    fun parse(argv: List<String>, context: Context) {
        parse(argv, context, 0, true)
    }

    private fun parse(
        argv: List<String>,
        context: Context,
        startingArgI: Int,
        canRun: Boolean,
    ): Pair<List<String>, Int> {
        var tokens = argv
        val command = context.command
        val aliases = command.aliases()
        val subcommands = command._subcommands.associateBy { it.commandName }
        val subcommandNames = subcommands.keys
        val optionsByName = HashMap<String, Option>()
        val numberOption = command._options.find { it.acceptsNumberValueWithoutName }
        val arguments = command._arguments
        val prefixes = mutableSetOf<String>()
        val longNames = mutableSetOf<String>()
        val hasMultipleSubAncestor =
            generateSequence(context.parent) { it.parent }.any { it.command.allowMultipleSubcommands }

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

        if (startingArgI > tokens.lastIndex && command.printHelpOnEmptyArgs) {
            throw PrintHelpMessage(command, error = true)
        }

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var subcommand: CliktCommand? = null
        var canParseOptions = true
        var canExpandAtFiles = context.expandArgumentFiles
        val invocations = mutableListOf<OptInvocation>()
        var minAliasI = 0

        fun isLongOptionWithEquals(prefix: String, token: String): Boolean {
            if ("=" !in token) return false
            if (prefix.isEmpty()) return false
            if (prefix.length > 1) return true
            if (context.tokenTransformer(context, token.substringBefore("=")) in longNames) return true
            if (context.tokenTransformer(context, token.take(2)) in optionsByName) return false
            return true
        }

        fun consumeParse(result: OptParseResult) {
            positionalArgs += result.unknown
            invocations += result.known
            i += result.consumed
        }

        loop@ while (i <= tokens.lastIndex) {
            val tok = tokens[i]
            val normTok = context.tokenTransformer(context, tok)
            val prefix = splitOptionPrefix(tok).first
            when {
                canExpandAtFiles && tok.startsWith("@") && normTok !in optionsByName -> {
                    if (tok.startsWith("@@")) {
                        positionalArgs += tok.drop(1)
                        i += 1
                    } else {
                        tokens = loadArgFile(normTok.drop(1), context) + tokens.slice(i + 1..tokens.lastIndex)
                        i = 0
                        minAliasI = 0
                    }
                }
                canParseOptions && tok == "--" -> {
                    i += 1
                    canParseOptions = false
                    canExpandAtFiles = false
                }
                canParseOptions && (
                        prefix.length > 1 && prefix in prefixes
                                || normTok in longNames
                                || isLongOptionWithEquals(prefix, tok)
                        ) -> {
                    consumeParse(parseLongOpt(
                        command.treatUnknownOptionsAsArgs,
                        context,
                        tokens,
                        tok,
                        i,
                        optionsByName,
                        subcommandNames
                    ))
                }
                canParseOptions && tok.length >= 2 && prefix.isNotEmpty() && prefix in prefixes -> {
                    consumeParse(parseShortOpt(
                        command.treatUnknownOptionsAsArgs,
                        context,
                        tokens,
                        tok,
                        i,
                        optionsByName,
                        numberOption,
                        subcommandNames
                    ))
                }
                i >= minAliasI && tok in aliases -> {
                    tokens = aliases.getValue(tok) + tokens.slice(i + 1..tokens.lastIndex)
                    i = 0
                    minAliasI = aliases.getValue(tok).size
                }
                normTok in subcommands -> {
                    subcommand = subcommands.getValue(normTok)
                    break@loop
                }
                else -> {
                    if (!context.allowInterspersedArgs) canParseOptions = false
                    positionalArgs += tokens[i] // arguments aren't transformed
                    i += 1
                }
            }
        }

        // Group options for finalization
        val invocationsByOption = invocations.groupBy({ it.opt }, { it.inv })
        val invocationsByGroup = invocations.groupBy { (it.opt as? GroupableOption)?.parameterGroup }
        val invocationsByOptionByGroup = invocationsByGroup
            .mapValues { (_, invs) ->
                invs.groupBy({ it.opt }, { it.inv })
                    .filterKeys { !it.eager }
            }

        // Finalize and validate everything as long as we aren't resuming a parse for multiple subcommands
        try {
            if (canRun) {
                // Finalize eager options
                invocationsByOption.forEach { (o, inv) -> if (o.eager) o.finalize(context, inv) }

                // Parse arguments
                val (excess, parsedArgs) = parseArguments(positionalArgs, arguments, context)

                // Finalize arguments before options, so that options can reference them
                val retries = finalizeArguments(parsedArgs, context)
                i = handleExcessArguments(
                    excess,
                    hasMultipleSubAncestor,
                    i,
                    tokens,
                    subcommands,
                    positionalArgs,
                    context
                )

                // Finalize un-grouped options
                finalizeOptions(
                    context,
                    command._options.filter { !it.eager && (it as? GroupableOption)?.parameterGroup == null },
                    invocationsByOptionByGroup[null] ?: emptyMap()
                )

                // Finalize groups after ungrouped options so that the groups can use their values
                invocationsByOptionByGroup.forEach { (group, invocations) -> group?.finalize(context, invocations) }

                // Finalize groups with no invocations
                command._groups.forEach { if (it !in invocationsByGroup) it.finalize(context, emptyMap()) }

                // Retry any failed args now that they can reference option values
                retries.forEach { (arg, v) -> arg.finalize(context, v) }

                // Now that all parameters have been finalized, we can validate everything
                command._options.forEach { o ->
                    if ((o as? GroupableOption)?.parameterGroup == null) o.postValidate(context)
                }
                command._groups.forEach { it.postValidate(context) }
                command._arguments.forEach { it.postValidate(context) }

                if (subcommand == null && subcommands.isNotEmpty() && !command.invokeWithoutSubcommand) {
                    throw PrintHelpMessage(command, error = true)
                }

                command.currentContext.invokedSubcommand = subcommand
                if (command.currentContext.printExtraMessages) {
                    val console = command.currentContext.console
                    for (warning in command.messages) {
                        console.print(warning, error = true)
                        console.print(console.lineSeparator, error = true)
                    }
                }

                command.run()
            } else if (subcommand == null && positionalArgs.isNotEmpty()) {
                // If we're resuming a parse with multiple subcommands, there can't be any args after the last
                // subcommand is parsed
                throwExcessArgsError(positionalArgs, positionalArgs.size, context)
            }
        } catch (e: UsageError) {
            // Augment usage errors with the current context if they don't have one
            e.context = context
            throw e
        }

        if (subcommand != null) {
            val (nextTokens, nextArgI) = parse(tokens, subcommand.currentContext, i + 1, true)
            if (command.allowMultipleSubcommands && nextTokens.size - nextArgI > 0) {
                parse(nextTokens, context, nextArgI, false)
            }
            return nextTokens to nextArgI
        }

        return tokens to i
    }

    private fun handleExcessArguments(
        excess: Int,
        hasMultipleSubAncestor: Boolean,
        i: Int,
        tokens: List<String>,
        subcommands: Map<String, CliktCommand>,
        positionalArgs: ArrayList<String>,
        context: Context,
    ): Int {
        if (excess > 0) {
            if (hasMultipleSubAncestor) {
                return tokens.size - excess
            } else if (excess == 1 && subcommands.isNotEmpty()) {
                val actual = positionalArgs.last()
                throw NoSuchSubcommand(actual, context.correctionSuggestor(actual, subcommands.keys.toList()), context)
            } else {
                throwExcessArgsError(positionalArgs, excess, context)
            }
        }
        return i
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
            return OptParseResult(1, listOf(tok), emptyList())
        } else {
            val possibilities = context.correctionSuggestor(name,
                optionsByName.filterNot { it.value.hidden }.keys.toList())
            throw NoSuchOption(name, possibilities).also { it.context = context }
        }

        return parseOptValues(option, name, ignoreUnknown, tokens, index, attachedValue, optionsByName, subcommandNames)
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

        if (values.size !in option.nvalues) {
            throw IncorrectOptionValueCount(option, name)
        }

        val consumed = values.size + if (attachedValue == null) 1 else 0
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
            val option = optionsByName[name] ?: if (ignoreUnknown && tok.length == 2) {
                return OptParseResult(1, listOf(tok), emptyList())
            } else {
                val possibilities = when {
                    prefix == "-" && "-$tok" in optionsByName -> listOf("-$tok")
                    else -> emptyList()
                }
                throw NoSuchOption(name, possibilities).also { it.context = context }
            }
            if (option.nvalues.last > 0) {
                val value = if (i < tok.lastIndex) tok.drop(i + 1) else null
                val result = parseOptValues(
                    option, name, ignoreUnknown, tokens, index, value, optionsByName, subcommandNames
                )
                return result.copy(known = invocations + result.known)
            } else {
                invocations += OptInvocation(option, name, emptyList())
            }
        }
        return OptParseResult(1, invocations)
    }

    private fun parseArguments(
        positionalArgs: List<String>,
        arguments: List<Argument>,
        context: Context,
    ): Pair<Int, Map<Argument, List<String>>> {
        val out = linkedMapOf<Argument, List<String>>().withDefault { listOf() }
        // The number of fixed size arguments that occur after an unlimited size argument. This
        // includes optional single value args, so it might be bigger than the number of provided
        // values.
        val endSize = arguments.asReversed()
            .takeWhile { it.nvalues > 0 }
            .sumOf { it.nvalues }

        var i = 0
        for (argument in arguments) {
            val remaining = positionalArgs.size - i
            val consumed = when {
                argument.nvalues <= 0 -> maxOf(if (argument.required) 1 else 0, remaining - endSize)
                argument.nvalues > 0 && !argument.required && remaining == 0 -> 0
                else -> argument.nvalues
            }
            if (consumed > remaining) {
                if (remaining == 0) throw MissingArgument(argument).also { it.context = context }
                else throw IncorrectArgumentValueCount(argument).also { it.context = context }
            }
            out[argument] = out.getValue(argument) + positionalArgs.subList(i, i + consumed)
            i += consumed
        }

        val excess = positionalArgs.size - i
        return excess to out
    }

    /** Returns map of argument that need retries to their values */
    private fun finalizeArguments(
        parsedArgs: Map<Argument, List<String>>,
        context: Context,
    ): Map<Argument, List<String>> {
        val retries = mutableMapOf<Argument, List<String>>()
        for ((it, v) in parsedArgs) {
            try {
                it.finalize(context, v)
            } catch (e: IllegalStateException) {
                retries[it] = v
            }
        }
        return retries
    }


    private fun loadArgFile(filename: String, context: Context): List<String> {
        val text = readFileIfExists(filename) ?: throw FileNotFound(filename)
        val toks = mutableListOf<String>()
        var inQuote: Char? = null
        val sb = StringBuilder()
        var i = 0
        fun err(msg: String): Nothing {
            throw InvalidFileFormat(filename, msg, text.take(i).count { it == '\n' }, context)
        }
        loop@ while (i < text.length) {
            val c = text[i]
            when {
                c in "\r\n" && inQuote != null -> {
                    sb.append(c)
                    i += 1
                }
                c == '\\' -> {
                    if (i >= text.lastIndex) err(context.localization.fileEndsWithSlash())
                    if (text[i + 1] in "\r\n") {
                        do {
                            i += 1
                        } while (i <= text.lastIndex && text[i].isWhitespace())
                    } else {
                        sb.append(text[i + 1])
                        i += 2
                    }
                }
                c == inQuote -> {
                    toks += sb.toString()
                    sb.clear()
                    inQuote = null
                    i += 1
                }
                c == '#' && inQuote == null -> {
                    i = text.indexOf('\n', i)
                    if (i < 0) break@loop
                }
                c in "\"'" && inQuote == null -> {
                    inQuote = c
                    i += 1
                }
                c.isWhitespace() && inQuote == null -> {
                    if (sb.isNotEmpty()) {
                        toks += sb.toString()
                        sb.clear()
                    }
                    i += 1
                }
                else -> {
                    sb.append(c)
                    i += 1
                }
            }
        }

        if (inQuote != null) {
            err(context.localization.unclosedQuote())
        }

        if (sb.isNotEmpty()) {
            toks += sb.toString()
        }

        return toks
    }

    private fun throwExcessArgsError(positionalArgs: List<String>, excess: Int, context: Context): Nothing {
        val actual = positionalArgs.takeLast(excess).joinToString(" ", limit = 3, prefix = "(", postfix = ")")
        val message = when (excess) {
            1 -> context.localization.extraArgumentOne(actual)
            else -> context.localization.extraArgumentMany(actual, excess)
        }
        throw UsageError(message, context = context)
    }
}

