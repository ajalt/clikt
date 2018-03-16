package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.Argument
import com.github.ajalt.clikt.parameters.options.EagerOption
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parsers.OptionParser.Invocation
import com.github.ajalt.clikt.parsers.OptionParser.ParseResult
import java.util.*
import kotlin.collections.HashMap

internal object Parser {
    fun parse(argv: Array<String>, context: Context) {
        parse(argv, context, 0)
    }

    private tailrec fun parse(argv: Array<String>, context: Context, startingArgI: Int) {
        val command = context.command
        val subcommands = command.subcommands.associateBy { it.name }
        val optionsByName = HashMap<String, Option>()
        val arguments = command.arguments
        val prefixes = mutableSetOf<String>()
        val longNames = mutableSetOf<String>()

        for (option in command.options) {
            for (name in option.names + option.secondaryNames) {
                optionsByName[name] = option
                if (name.length > 2) longNames += name
                prefixes += prefix(name)
            }
        }
        prefixes.remove("")

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var subcommand: CliktCommand? = null
        var canParseOptions = true
        val invocations = mutableListOf<Pair<Option, Invocation>>()
        loop@ while (i <= argv.lastIndex) {
            val tok = context.tokenTransformer(argv[i])
            val prefix = prefix(tok)
            when {
                tok == "--" -> {
                    i += 1
                    canParseOptions = false
                }
                canParseOptions && ('=' in tok || tok in longNames || prefix.length > 1) -> {
                    val (opt, result) = parseLongOpt(argv, tok, i, optionsByName)
                    invocations += opt to result.invocation
                    i += result.consumedCount
                }
                canParseOptions && tok.length >= 2 && prefix.isNotEmpty() -> {
                    val (count, invokes) = parseShortOpt(argv, tok, i, optionsByName)
                    invocations += invokes
                    i += count
                }
                tok in subcommands -> {
                    subcommand = subcommands[tok]!!
                    break@loop
                }
                else -> {
                    if (command.context.allowInterspersedArgs) {
                        positionalArgs += argv[i] // arguments aren't transformed
                        i += 1
                    } else {
                        positionalArgs += argv.slice(i..argv.lastIndex)
                        break@loop
                    }
                }
            }
        }

        val invocationsByOption = invocations.groupBy({ it.first }, { it.second })

        invocationsByOption.forEach { (o, inv) -> if (o is EagerOption) o.finalize(context, inv) }
        invocationsByOption.forEach { (o, inv) -> if (o !is EagerOption) o.finalize(context, inv) }

        // Finalize the options with values so that they can apply default values etc.
        for (o in command.options) {
            if (o !is EagerOption && o !in invocationsByOption) o.finalize(context, emptyList())
        }

        parseArguments(positionalArgs, arguments)

        command.arguments.forEach { it.finalize(context) }

        if (subcommand == null && subcommands.isNotEmpty() && !command.invokeWithoutSubcommand) {
            throw PrintHelpMessage(command)
        }

        command.context.invokedSubcommand = subcommand
        command.run()

        if (subcommand != null) {
            parse(argv, subcommand.context, i + 1)
        }
    }

    private fun parseLongOpt(argv: Array<String>, arg: String, index: Int,
                             optionsByName: Map<String, Option>): Pair<Option, ParseResult> {
        val equalsIndex = arg.indexOf('=')
        val (name, value) = if (equalsIndex >= 0) {
            arg.substring(0, equalsIndex) to arg.substring(equalsIndex + 1)
        } else {
            arg to null
        }
        val option = optionsByName[name] ?: throw NoSuchOption(name,
                possibilities = optionsByName.keys.filter { it.startsWith(name) })
        val result = option.parser.parseLongOpt(option, name, argv, index, value)
        return option to result
    }

    private fun parseShortOpt(argv: Array<String>, arg: String, index: Int,
                              optionsByName: Map<String, Option>): Pair<Int, List<Pair<Option, Invocation>>> {
        val prefix = arg[0].toString()
        val invocations = mutableListOf<Pair<Option, Invocation>>()
        for ((i, opt) in arg.withIndex()) {
            if (i == 0) continue // skip the dash

            val name = prefix + opt
            val option = optionsByName[name] ?: throw NoSuchOption(name)
            val result = option.parser.parseShortOpt(option, name, argv, index, i)
            invocations += option to result.invocation
            if (result.consumedCount > 0) return result.consumedCount to invocations
        }
        throw IllegalStateException(
                "Error parsing short option ${argv[index]}: no parser consumed value.")
    }

    private fun parseArguments(positionalArgs: List<String>, arguments: List<Argument>): Int {
        // The number of fixed size arguments that occur after an unlimited size argument. This
        // includes optional single value args, so it might be bigger than the number of provided
        // values.
        val endSize = arguments.asReversed()
                .takeWhile { it.nargs > 0 }
                .sumBy { it.nargs }

        var i = 0
        for (argument in arguments) {
            val remaining = positionalArgs.size - i
            val consumed = when {
                argument.nargs <= 0 -> maxOf(0, remaining - endSize)
                argument.nargs > 0 && !argument.required && remaining == 0 -> 0
                else -> argument.nargs
            }
            if (consumed > remaining) {
                if (remaining == 0) throw MissingParameter(argument)
                else throw IncorrectArgumentNargs(argument)
            }
            argument.rawValues = argument.rawValues + positionalArgs.subList(i, i + consumed)
            i += consumed
        }

        val excess = positionalArgs.size - i
        if (excess > 0) {
            throw UsageError("Got unexpected extra argument${if (excess == 1) "" else "s"} " +
                    positionalArgs.slice(i..positionalArgs.lastIndex)
                            .joinToString(" ", limit = 3, prefix = "(", postfix = ")"))
        }
        return i
    }

    private fun prefix(name: String) =
            when {
                name.isEmpty() || name[0].isLetterOrDigit() -> ""
                name.length > 2 && name[0] == name[1] -> name.slice(0..1)
                else -> name.slice(0..0)
            }
}
