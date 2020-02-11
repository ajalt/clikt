package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.mpp.readFileIfExists
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.EagerOption
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix
import com.github.ajalt.clikt.parsers.OptionParser.Invocation
import com.github.ajalt.clikt.parsers.OptionParser.ParseResult

internal object Parser {
    fun parse(argv: List<String>, context: Context) {
        parse(argv, context, 0)
    }

    private tailrec fun parse(argv: List<String>, context: Context, startingArgI: Int) {
        var tokens = argv
        val command = context.command
        val aliases = command.aliases()
        val subcommands = command._subcommands.associateBy { it.commandName }
        val optionsByName = HashMap<String, Option>()
        val arguments = command._arguments
        val prefixes = mutableSetOf<String>()
        val longNames = mutableSetOf<String>()

        for (option in command._options) {
            for (name in option.names + option.secondaryNames) {
                optionsByName[name] = option
                if (name.length > 2) longNames += name
                prefixes += splitOptionPrefix(name).first
            }
        }
        prefixes.remove("")

        if (startingArgI > tokens.lastIndex && command.printHelpOnEmptyArgs) {
            throw PrintHelpMessage(command)
        }

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var subcommand: CliktCommand? = null
        var canParseOptions = true
        var canExpandAtFiles = context.expandArgumentFiles
        val invocations = mutableListOf<Pair<Option, Invocation>>()
        var minAliasI = 0
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
                canParseOptions && (('=' in tok && prefix.isNotEmpty()) || normTok in longNames || prefix.length > 1 && prefix in prefixes) -> {
                    val (opt, result) = parseLongOpt(context, tokens, tok, i, optionsByName)
                    invocations += opt to result.invocation
                    i += result.consumedCount
                }
                canParseOptions && tok.length >= 2 && prefix.isNotEmpty() && prefix in prefixes -> {
                    val (count, invokes) = parseShortOpt(context, tokens, tok, i, optionsByName)
                    invocations += invokes
                    i += count
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

        val invocationsByOption = invocations.groupBy({ it.first }, { it.second })
        val invocationsByGroup = invocations.groupBy { (it.first as? GroupableOption)?.parameterGroup }
        val invocationsByOptionByGroup = invocationsByGroup.mapValues { (_, invs) -> invs.groupBy({ it.first }, { it.second }).filterKeys { it !is EagerOption } }

        try {
            // Finalize eager options
            invocationsByOption.forEach { (o, inv) -> if (o is EagerOption) o.finalize(context, inv) }

            // Finalize un-grouped options that occurred on the command line
            invocationsByOptionByGroup[null]?.forEach { (o, inv) -> o.finalize(context, inv) }

            // Finalize un-grouped options not provided on the command line so that they can apply default values etc.
            command._options.forEach { o ->
                if (o !is EagerOption && o !in invocationsByOption && (o as? GroupableOption)?.parameterGroup == null) {
                    o.finalize(context, emptyList())
                }
            }

            // Finalize option groups after other options so that the groups can their values
            invocationsByOptionByGroup.forEach { (group, invocations) ->
                group?.finalize(context, invocations)
            }

            // Finalize groups with no invocations
            command._groups.forEach { if (it !in invocationsByGroup) it.finalize(context, emptyMap()) }

            parseArguments(positionalArgs, arguments).forEach { (it, v) -> it.finalize(context, v) }

            // Now that all parameters have been finalized, we can validate everything
            command._options.forEach { o -> if ((o as? GroupableOption)?.parameterGroup == null) o.postValidate(context) }
            command._groups.forEach { it.postValidate(context) }
            command._arguments.forEach { it.postValidate(context) }

            if (subcommand == null && subcommands.isNotEmpty() && !command.invokeWithoutSubcommand) {
                throw PrintHelpMessage(command)
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
        } catch (e: UsageError) {
            // Augment usage errors with the current context if they don't have one
            if (e.context == null) e.context = context
            throw e
        }

        if (subcommand != null) {
            parse(tokens, subcommand.currentContext, i + 1)
        }
    }

    private fun parseLongOpt(
            context: Context,
            tokens: List<String>,
            tok: String,
            index: Int,
            optionsByName: Map<String, Option>
    ): Pair<Option, ParseResult> {
        val equalsIndex = tok.indexOf('=')
        var (name, value) = if (equalsIndex >= 0) {
            tok.substring(0, equalsIndex) to tok.substring(equalsIndex + 1)
        } else {
            tok to null
        }
        name = context.tokenTransformer(context, name)
        val option = optionsByName[name] ?: throw NoSuchOption(name,
                possibilities = optionsByName.keys.filter { it.startsWith(name) })
        val result = option.parser.parseLongOpt(option, name, tokens, index, value)
        return option to result
    }

    private fun parseShortOpt(
            context: Context,
            tokens: List<String>,
            tok: String,
            index: Int,
            optionsByName: Map<String, Option>
    ): Pair<Int, List<Pair<Option, Invocation>>> {
        val prefix = tok[0].toString()
        val invocations = mutableListOf<Pair<Option, Invocation>>()
        for ((i, opt) in tok.withIndex()) {
            if (i == 0) continue // skip the dash

            val name = context.tokenTransformer(context, prefix + opt)
            val option = optionsByName[name] ?: throw NoSuchOption(name)
            val result = option.parser.parseShortOpt(option, name, tokens, index, i)
            invocations += option to result.invocation
            if (result.consumedCount > 0) return result.consumedCount to invocations
        }
        throw IllegalStateException(
                "Error parsing short option ${tokens[index]}: no parser consumed value.")
    }

    private fun parseArguments(
            positionalArgs: List<String>,
            arguments: List<Argument>
    ): Map<Argument, List<String>> {
        val out = linkedMapOf<Argument, List<String>>().withDefault { listOf() }
        // The number of fixed size arguments that occur after an unlimited size argument. This
        // includes optional single value args, so it might be bigger than the number of provided
        // values.
        val endSize = arguments.asReversed()
                .takeWhile { it.nvalues > 0 }
                .sumBy { it.nvalues }

        var i = 0
        for (argument in arguments) {
            val remaining = positionalArgs.size - i
            val consumed = when {
                argument.nvalues <= 0 -> maxOf(if (argument.required) 1 else 0, remaining - endSize)
                argument.nvalues > 0 && !argument.required && remaining == 0 -> 0
                else -> argument.nvalues
            }
            if (consumed > remaining) {
                if (remaining == 0) throw MissingParameter(argument)
                else throw IncorrectArgumentValueCount(argument)
            }
            out[argument] = out.getValue(argument) + positionalArgs.subList(i, i + consumed)
            i += consumed
        }

        val excess = positionalArgs.size - i
        if (excess > 0) {
            throw UsageError("Got unexpected extra argument${if (excess == 1) "" else "s"} " +
                    positionalArgs.slice(i..positionalArgs.lastIndex)
                            .joinToString(" ", limit = 3, prefix = "(", postfix = ")"))
        }
        return out
    }

    private fun loadArgFile(filename: String, context: Context): List<String> {
        val text = readFileIfExists(filename) ?: throw BadParameterValue("'$filename' is not a file", "@-file", context)
        val toks = mutableListOf<String>()
        var inQuote: Char? = null
        val sb = StringBuilder()
        var i = 0
        loop@ while (i < text.length) {
            val c = text[i]
            when {
                c == '\r' -> {
                    i += 1
                }
                c == '\n' && inQuote != null -> {
                    throw UsageError("unclosed quote in @-file")
                }
                c == '\\' -> {
                    if (i >= text.lastIndex) throw UsageError("@-file ends with \\")
                    if (text[i + 1] in "\r\n") throw UsageError("unclosed quote in @-file")
                    sb.append(text[i + 1])
                    i += 2
                }
                c == inQuote -> {
                    toks += sb.toString()
                    sb.clear()
                    inQuote = null
                    i += 1
                }
                inQuote == null && c == '#' -> {
                    i = text.indexOf('\n', i)
                    if (i < 0) break@loop
                }
                inQuote == null && c in "\"'" -> {
                    inQuote = c
                    i += 1
                }
                inQuote == null && c.isWhitespace() -> {
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
            throw UsageError("Missing closing quote in @-file")
        }

        if (sb.isNotEmpty()) {
            toks += sb.toString()
        }

        return toks
    }
}
