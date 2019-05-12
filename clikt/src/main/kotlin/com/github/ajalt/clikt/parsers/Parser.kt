package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.IncorrectArgumentValueCount
import com.github.ajalt.clikt.core.MissingParameter
import com.github.ajalt.clikt.core.NoSuchOption
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.UsageError
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
        var args = argv
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

        if (startingArgI > args.lastIndex && command.printHelpOnEmptyArgs) {
            throw PrintHelpMessage(command)
        }

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var subcommand: CliktCommand? = null
        var canParseOptions = true
        val invocations = mutableListOf<Pair<Option, Invocation>>()
        var minAliasI = 0
        loop@ while (i <= args.lastIndex) {
            val arg = args[i]
            val normArg = context.tokenTransformer(context, arg)
            val prefix = splitOptionPrefix(arg).first
            when {
                arg == "--" -> {
                    i += 1
                    canParseOptions = false
                }
                canParseOptions && ('=' in arg || normArg in longNames || prefix.length > 1 && prefix in prefixes) -> {
                    val (opt, result) = parseLongOpt(context, args, arg, i, optionsByName)
                    invocations += opt to result.invocation
                    i += result.consumedCount
                }
                canParseOptions && arg.length >= 2 && prefix.isNotEmpty() && prefix in prefixes -> {
                    val (count, invokes) = parseShortOpt(context, args, arg, i, optionsByName)
                    invocations += invokes
                    i += count
                }
                i >= minAliasI && arg in aliases -> {
                    args = aliases.getValue(arg) + args.slice(i + 1..args.lastIndex)
                    i = 0
                    minAliasI = aliases.getValue(arg).size
                }
                normArg in subcommands -> {
                    subcommand = subcommands.getValue(normArg)
                    break@loop
                }
                else -> {
                    if (command.context.allowInterspersedArgs) {
                        positionalArgs += args[i] // arguments aren't transformed
                        i += 1
                    } else {
                        positionalArgs += args.slice(i..args.lastIndex)
                        break@loop
                    }
                }
            }
        }

        val invocationsByOption = invocations.groupBy({ it.first }, { it.second })

        try {
            // Finalize eager options
            invocationsByOption.forEach { (o, inv) -> if (o is EagerOption) o.finalize(context, inv) }

            // Finalize remaining options that occurred on the command line
            invocationsByOption.forEach { (o, inv) -> if (o !is EagerOption) o.finalize(context, inv) }

            // Finalize options not provided on the command line so that they can apply default values etc.
            command._options.forEach { o ->
                if (o !is EagerOption && o !in invocationsByOption) o.finalize(context, emptyList())
            }

            parseArguments(positionalArgs, arguments).forEach { (it, v) -> it.finalize(context, v) }

            if (subcommand == null && subcommands.isNotEmpty() && !command.invokeWithoutSubcommand) {
                throw PrintHelpMessage(command)
            }

            command.context.invokedSubcommand = subcommand
            command.run()
        } catch (e: UsageError) {
            // Augment usage errors with the current context if they don't have one
            if (e.context == null) e.context = context
            throw e
        }

        if (subcommand != null) {
            parse(args, subcommand.context, i + 1)
        }
    }

    private fun parseLongOpt(
            context: Context,
            argv: List<String>,
            arg: String,
            index: Int,
            optionsByName: Map<String, Option>
    ): Pair<Option, ParseResult> {
        val equalsIndex = arg.indexOf('=')
        var (name, value) = if (equalsIndex >= 0) {
            arg.substring(0, equalsIndex) to arg.substring(equalsIndex + 1)
        } else {
            arg to null
        }
        name = context.tokenTransformer(context, name)
        val option = optionsByName[name] ?: throw NoSuchOption(name,
                possibilities = optionsByName.keys.filter { it.startsWith(name) })
        val result = option.parser.parseLongOpt(option, name, argv, index, value)
        return option to result
    }

    private fun parseShortOpt(
            context: Context,
            argv: List<String>,
            arg: String,
            index: Int,
            optionsByName: Map<String, Option>
    ): Pair<Int, List<Pair<Option, Invocation>>> {
        val prefix = arg[0].toString()
        val invocations = mutableListOf<Pair<Option, Invocation>>()
        for ((i, opt) in arg.withIndex()) {
            if (i == 0) continue // skip the dash

            val name = context.tokenTransformer(context, prefix + opt)
            val option = optionsByName[name] ?: throw NoSuchOption(name)
            val result = option.parser.parseShortOpt(option, name, argv, index, i)
            invocations += option to result.invocation
            if (result.consumedCount > 0) return result.consumedCount to invocations
        }
        throw IllegalStateException(
                "Error parsing short option ${argv[index]}: no parser consumed value.")
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
}
