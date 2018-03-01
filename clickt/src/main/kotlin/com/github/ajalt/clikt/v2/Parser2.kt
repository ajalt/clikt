package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.BadArgumentUsage
import com.github.ajalt.clikt.parser.NoSuchOption
import com.github.ajalt.clikt.parser.UsageError
import java.util.*
import kotlin.collections.HashMap

internal object Parser2 {
    fun parse(argv: Array<String>, context: Context2) {
        parse(argv, context, 0)
    }

    private tailrec fun parse(argv: Array<String>, context: Context2, startingArgI: Int) {
        val command = context.command
        val subcommands = command.subcommands.associateBy { it.name }
        val optionsByName = HashMap<String, Option>()
        val arguments = command.arguments

        for (option in command.options) {
            for (name in option.names) optionsByName[name] = option
        }

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var subcommand: CliktCommand? = null
        var canParseOptions = true
        val eagerOptions = mutableListOf<EagerOption>()
        loop@ while (i <= argv.lastIndex) {
            val a = argv[i]
            when {
                a == "--" -> {
                    i += 1
                    canParseOptions = false
                }
                a.startsWith("--") && canParseOptions -> {
                    val (count, opts) = parseLongOpt(argv, a, i, optionsByName)
                    eagerOptions += opts
                    i += count
                }
                a.startsWith("-") && canParseOptions -> {
                    val (count, opts) = parseShortOpt(argv, a, i, optionsByName)
                    eagerOptions += opts
                    i += count
                }
                a in subcommands -> {
                    subcommand = subcommands[a]!!
                    break@loop
                }
                else -> {
                    if (command.allowInterspersedArgs) {
                        positionalArgs += a
                        i += 1
                    } else {
                        positionalArgs += argv.slice(i..argv.lastIndex)
                        break@loop
                    }
                }
            }
        }

        parseArguments(positionalArgs, arguments)

        for (option in eagerOptions) {
            option.callback(context, option)
        }

        command.run()

        if (subcommand != null) {
            parse(argv, subcommand.context, i + 1)
        }
    }


    private fun parseLongOpt(argv: Array<String>, arg: String, index: Int,
                             optionsByName: Map<String, Option>): Pair<Int, List<EagerOption>> {
        val equalsIndex = arg.indexOf('=')
        val (name, value) = if (equalsIndex >= 0) {
            check(equalsIndex != arg.lastIndex) // TODO exceptions
            arg.substring(0, equalsIndex) to arg.substring(equalsIndex + 1)
        } else {
            arg to null
        }
        val option = optionsByName[name] ?: throw NoSuchOption(name,
                possibilities = optionsByName.keys.filter { name.startsWith(it) })
        val result = option.parser.parseLongOpt(option, name, argv, index, value)
        val eagerOption = if (option is EagerOption) listOf(option) else emptyList()
        return result to eagerOption
    }

    private fun parseShortOpt(argv: Array<String>, arg: String, index: Int,
                              optionsByName: Map<String, Option>): Pair<Int, List<EagerOption>> {
        val prefix = arg[0].toString()
        val eagerOptions = mutableListOf<EagerOption>()
        for ((i, opt) in arg.withIndex()) {
            if (i == 0) continue // skip the dash

            val name = prefix + opt
            val option = optionsByName[name] ?: throw NoSuchOption(name)
            val result = option.parser.parseShortOpt(option, name, argv, index, i)
            if (option is EagerOption) eagerOptions += option
            if (result > 0) return result to eagerOptions
        }
        throw IllegalStateException(
                "Error parsing short option ${argv[index]}: no parser consumed value.")
    }

    private fun parseArguments(positionalArgs: List<String>, arguments: List<Argument<*>>): Int {
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
                argument.nargs == 1 && !argument.required && remaining == 0 -> 0
                else -> argument.nargs
            }
            if (consumed > remaining) {
                throw BadArgumentUsage("argument ${argument.name} takes ${argument.nargs} values")
            }
            argument.rawValues = argument.rawValues + positionalArgs.subList(i, i + consumed)
            i += consumed
        }

        val excess = positionalArgs.size - i
        if (excess > 0) {
            throw UsageError("Got unexpected extra argument${if (excess == 1) "" else "s"} " +
                    positionalArgs.joinToString(" ", limit = 3, prefix = "(", postfix = ")"))
        }
        return i
    }
}
