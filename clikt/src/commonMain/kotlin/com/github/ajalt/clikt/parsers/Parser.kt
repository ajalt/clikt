package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.internal.finalizeOptions
import com.github.ajalt.clikt.mpp.readFileIfExists
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix

/** [i] is the argv index of the token that caused the error */
private data class Err(val e: UsageError, val i: Int, val includeInMulti: Boolean = true)

private data class ArgsParseResult(val excessCount: Int, val args: Map<Argument, List<String>>, val err: Err?)

private data class OptInvocation(val opt: Option, val name: String, val values: List<String>) {
    val inv get() = Invocation(name, values)
}

private data class OptParseResult(
    val consumed: Int,
    val unknown: List<String> = emptyList(),
    val known: List<OptInvocation> = emptyList(),
    val err: Err? = null,
) {
    constructor(consumed: Int, invocations: List<OptInvocation>) : this(consumed, emptyList(), invocations)
    constructor(consumed: Int, opt: Option, name: String, values: List<String>)
            : this(consumed, emptyList(), listOf(OptInvocation(opt, name, values)))
}

internal object Parser {
    fun parse(argv: List<String>, context: Context) {
        parse(argv, context, true)
    }

    private fun parse(
        argv: List<String>,
        context: Context,
        canRun: Boolean,
    ): List<String> {
        var tokens = argv
        val command = context.command
        val aliases = command.aliases()
        val subcommands = command._subcommands.associateBy { it.commandName }
        val subcommandNames = subcommands.keys
        val optionsByName = mutableMapOf<String, Option>()
        val numberOption = command._options.find { it.acceptsNumberValueWithoutName }
        val arguments = command._arguments
        val prefixes = mutableSetOf<String>()
        val longNames = mutableSetOf<String>()
        val hasMultipleSubAncestor = context.ancestors().any { it.command.allowMultipleSubcommands }
        val positionalArgs = mutableListOf<Pair<Int, String>>()
        var subcommand: CliktCommand? = null
        var canParseOptions = true
        var canExpandAtFiles = context.expandArgumentFiles
        val invocations = mutableListOf<OptInvocation>()
        val errors = mutableListOf<Err>()
        var i = 0
        var minAliasI = 0

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
            throw PrintHelpMessage(command, error = true)
        }

        fun isLongOptionWithEquals(prefix: String, token: String): Boolean {
            if ("=" !in token) return false
            if (prefix.isEmpty()) return false
            if (prefix.length > 1) return true
            if (context.tokenTransformer(context, token.substringBefore("=")) in longNames) return true
            if (context.tokenTransformer(context, token.take(2)) in optionsByName) return false
            return true
        }

        fun consumeParse(tokenIndex: Int, result: OptParseResult) {
            positionalArgs += result.unknown.map { tokenIndex to it }
            invocations += result.known
            result.err?.let { errors += it }
            i += result.consumed
        }

        loop@ while (i <= tokens.lastIndex) {
            val tok = tokens[i]
            val normTok = context.tokenTransformer(context, tok)
            val prefix = splitOptionPrefix(tok).first
            when {
                canExpandAtFiles && tok.startsWith("@") && normTok !in optionsByName -> {
                    if (tok.startsWith("@@")) {
                        positionalArgs += i to tok.drop(1)
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
                    consumeParse(
                        i,
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

                canParseOptions && tok.length >= 2 && prefix.isNotEmpty() && prefix in prefixes -> {
                    consumeParse(
                        i,
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
                    tokens = aliases.getValue(tok) + tokens.slice(i + 1..tokens.lastIndex)
                    i = 0
                    minAliasI = aliases.getValue(tok).size
                }

                normTok in subcommands -> {
                    subcommand = subcommands.getValue(normTok)
                    i += 1
                    break@loop
                }

                else -> {
                    if (!context.allowInterspersedArgs) canParseOptions = false
                    positionalArgs += i to tokens[i] // arguments aren't transformed
                    i += 1
                }
            }
        }

        // Group options for finalization
        val invocationsByOption = invocations.groupBy({ it.opt }, { it.inv })
        val invocationsByGroup = invocations.groupBy { (it.opt as? GroupableOption)?.parameterGroup }
        val invocationsByOptionByGroup = invocationsByGroup
            .mapValues { (_, invocations) ->
                invocations.groupBy({ it.opt }, { it.inv })
                    .filterKeys { !it.eager }
            }

        // Finalize and validate everything as long as we aren't resuming a parse for multiple subcommands
        try {
            if (canRun) {
                // Finalize eager options
                invocationsByOption.forEach { (o, inv) -> if (o.eager) o.finalize(context, inv) }

                // Parse arguments
                val argsParseResult = parseArguments(i, positionalArgs, arguments, context)
                argsParseResult.err?.let { errors += it }

                val excessResult = handleExcessArguments(
                    argsParseResult.excessCount,
                    hasMultipleSubAncestor,
                    i,
                    tokens,
                    subcommands,
                    positionalArgs,
                    context
                )
                excessResult.second?.let { errors += it }

                val usageErrors = errors
                    .filter { it.includeInMulti }.ifEmpty { errors }
                    .sortedBy { it.i }.mapTo(mutableListOf()) { it.e }

                i = excessResult.first

                // Finalize arguments before options, so that options can reference them
                val (retries, argErrs) = finalizeArguments(argsParseResult.args, context)
                usageErrors += argErrs

                // Finalize un-grouped options
                gatherErrors(usageErrors) {
                    finalizeOptions(
                        context,
                        command._options.filter { !it.eager && (it as? GroupableOption)?.parameterGroup == null },
                        invocationsByOptionByGroup[null] ?: emptyMap()
                    )
                }

                // Since groups like ChoiceGroup may depend on an ungrouped option's value, we can't finalize groups if
                // any options failed to validate, so throw errors now
                MultiUsageError.buildOrNull(usageErrors)?.let { throw it }
                usageErrors.clear()

                // Finalize groups after ungrouped options so that the groups can use their values
                for ((group, invs) in invocationsByOptionByGroup) {
                    gatherErrors(usageErrors) { group?.finalize(context, invs) }
                }

                // Finalize groups with no invocations
                for (g in command._groups) {
                    if (g !in invocationsByGroup) gatherErrors(usageErrors) {
                        g.finalize(context, emptyMap())
                    }
                }

                // We can't validate a param that didn't finalize successfully, and we don't keep track of which
                // ones are finalized, so throw any errors now
                MultiUsageError.buildOrNull(usageErrors)?.let { throw it }
                usageErrors.clear()

                // Retry any failed args now that they can reference option values
                retries.forEach { (arg, v) -> gatherErrors(usageErrors) { arg.finalize(context, v) } }

                // Now that all parameters have been finalized, we can validate everything
                command._options.forEach { o ->
                    if ((o as? GroupableOption)?.parameterGroup == null) gatherErrors(usageErrors) {
                        o.postValidate(context)
                    }
                }
                command._groups.forEach { gatherErrors(usageErrors) { it.postValidate(context) } }
                command._arguments.forEach { gatherErrors(usageErrors) { it.postValidate(context) } }

                MultiUsageError.buildOrNull(usageErrors)?.let { throw it }

                if (subcommand == null && subcommands.isNotEmpty() && !command.invokeWithoutSubcommand) {
                    throw PrintHelpMessage(command, error = true)
                }

                command.currentContext.invokedSubcommand = subcommand
                if (command.currentContext.printExtraMessages) {
                    val t = command.currentContext.terminal.forStdErr()
                    for (warning in command.messages) {
                        t.warning(warning)
                    }
                }

                command.run()
            } else if (subcommand == null && positionalArgs.isNotEmpty()) {
                // If we're resuming a parse with multiple subcommands, there can't be any args after the last
                // subcommand is parsed
                throw excessArgsError(positionalArgs, positionalArgs.size, context)
            }
        } catch (e: UsageError) {
            // Augment usage errors with the current context if they don't have one
            e.context = context
            throw e
        }

        if (subcommand != null) {
            val nextTokens = parse(tokens.drop(i), subcommand.currentContext, true)
            if (command.allowMultipleSubcommands && nextTokens.isNotEmpty()) {
                parse(nextTokens, context, false)
            }
            return nextTokens
        }

        return tokens.drop(i)
    }

    /** Returns either the new argv index, or an error */
    private fun handleExcessArguments(
        excess: Int,
        hasMultipleSubAncestor: Boolean,
        i: Int,
        tokens: List<String>,
        subcommands: Map<String, CliktCommand>,
        positionalArgs: List<Pair<Int, String>>,
        context: Context,
    ): Pair<Int, Err?> {
        if (excess > 0) {
            return when {
                hasMultipleSubAncestor -> tokens.size - excess to null
                excess == 1 && subcommands.isNotEmpty() -> {
                    val actual = positionalArgs.last().second
                    throw NoSuchSubcommand(
                        actual, context.correctionSuggestor(actual, subcommands.keys.toList()), context
                    )
                }

                else -> -1 to Err(excessArgsError(positionalArgs, excess, context), i, includeInMulti = false)
            }
        }
        return i to null
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

        val consumed = values.size + if (attachedValue == null) 1 else 0
        if (values.size !in option.nvalues) {
            return OptParseResult(consumed, err = Err(IncorrectOptionValueCount(option, name), index))
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
        argvIndex: Int,
        positionalArgs: List<Pair<Int, String>>,
        arguments: List<Argument>,
        context: Context,
    ): ArgsParseResult {
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
                val e = when (remaining) {
                    0 -> MissingArgument(argument).also { it.context = context }
                    else -> IncorrectArgumentValueCount(argument).also { it.context = context }
                }
                return ArgsParseResult(0, out, Err(e, positionalArgs.getOrNull(i)?.first ?: argvIndex))
            }
            out[argument] = out.getValue(argument) + positionalArgs.subList(i, i + consumed).map { it.second }
            i += consumed
        }

        val excess = positionalArgs.size - i
        return ArgsParseResult(excess, out, null)
    }

    /** Returns map of arguments that need retries to their values */
    private fun finalizeArguments(
        parsedArgs: Map<Argument, List<String>>,
        context: Context,
    ): Pair<Map<Argument, List<String>>, List<UsageError>> {
        val retries = mutableMapOf<Argument, List<String>>()
        val usageErrors = mutableListOf<UsageError>()
        for ((it, v) in parsedArgs) {
            try {
                it.finalize(context, v)
            } catch (e: IllegalStateException) {
                retries[it] = v
            } catch (e: UsageError) {
                usageErrors += e
            }
        }
        return retries to usageErrors
    }


    private fun loadArgFile(filename: String, context: Context): List<String> {
        val text = readFileIfExists(filename) ?: throw FileNotFound(filename)
        return shlex(filename, text, context)
    }

    private fun excessArgsError(positionalArgs: List<Pair<Int, String>>, excess: Int, context: Context): UsageError {
        val actual = positionalArgs.takeLast(excess)
            .joinToString(" ", limit = 3, prefix = "(", postfix = ")") { it.second }
        val message = when (excess) {
            1 -> context.localization.extraArgumentOne(actual)
            else -> context.localization.extraArgumentMany(actual, excess)
        }
        return UsageError(message, context = context)
    }
}

private inline fun gatherErrors(errors: MutableList<UsageError>, block: () -> Unit) {
    try {
        block()
    } catch (e: UsageError) {
        errors += e
    }
}
