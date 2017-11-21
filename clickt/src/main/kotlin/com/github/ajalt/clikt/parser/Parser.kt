package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.Context
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.jvm.isAccessible
import kotlin.system.exitProcess

object Parser {
    fun main(argv: Array<String>, context: Context) {
        try {
            parse(argv, context)
        } catch (e: CliktError) {
            when (e) {
                is PrintHelpMessage -> {
                    println(e.command.getFormattedHelp())
                    exitProcess(0)
                }
                is PrintMessage -> {
                    println(e.message)
                    exitProcess(0)
                }
                is UsageError -> {
                    println(e.formatMessage(context))
                    exitProcess(1)
                }
                else -> {
                    println(e.message)
                    exitProcess(1)
                }
            }
        }
    }

    fun parse(argv: Array<String>, context: Context) {
        parse(argv, context, 0)
    }

    private tailrec fun parse(argv: Array<String>, context: Context, startingArgI: Int) {
        val command = context.command
        val subcommands = command.subcommands.associateBy { it.name }
        val optionsByName = HashMap<String, Option>()
        val arguments = ArrayList<Argument<*>>()
        val parsedValuesByParameter = LinkedHashMap<Parameter, MutableList<Any?>>(command.parameters.size)

        for (param in command.parameters) {
            parsedValuesByParameter[param] = ArrayList()
            when (param) {
                is Option -> param.names.associateByTo(optionsByName, { it }, { param })
                is Argument<*> -> arguments.add(param)
            }
        }

        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        var subcommand: Command? = null
        var canParseOptions = true
        loop@ while (i <= argv.lastIndex) {
            val a = argv[i]
            when {
                a == "--" -> {
                    i += 1
                    canParseOptions = false
                }
                a.startsWith("--") && canParseOptions -> {
                    i += parseLongOpt(argv, a, i, optionsByName, parsedValuesByParameter)
                }
                a.startsWith("-") && canParseOptions -> {
                    i += parseShortOpt(argv, a, i, optionsByName, parsedValuesByParameter)
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

        parseArguments(positionalArgs, arguments, parsedValuesByParameter)

        val commandArgs = processValues(context, parsedValuesByParameter)
        invoke(command, commandArgs)

        if (subcommand != null) {
            parse(argv, subcommand.makeContext(context), i + 1)
        }
    }


    private fun invoke(command: Command, args: Array<Any?>) {
        command.function.isAccessible = true
        command.function.call(*args)
    }

    private fun parseLongOpt(argv: Array<String>, arg: String, index: Int,
                             optionsByName: Map<String, Option>,
                             parsedValuesByParameter: HashMap<Parameter, MutableList<Any?>>): Int {
        val equalsIndex = arg.indexOf('=')
        val (name, value) = if (equalsIndex >= 0) {
            check(equalsIndex != arg.lastIndex) // TODO exceptions
            arg.substring(0, equalsIndex) to arg.substring(equalsIndex + 1)
        } else {
            arg to null
        }
        val option = optionsByName[name] ?: throw NoSuchOption(name,
                possibilities = optionsByName.keys.filter { name.startsWith(it) })
        val result = option.parser.parseLongOpt(name, argv, index, value)
        parsedValuesByParameter[option]!!.add(result.value)
        return result.consumedCount
    }

    private fun parseShortOpt(argv: Array<String>, arg: String, index: Int,
                              optionsByName: Map<String, Option>,
                              parsedValuesByParameter: HashMap<Parameter, MutableList<Any?>>): Int {
        val prefix = arg[0].toString()
        for ((i, opt) in arg.withIndex()) {
            if (i == 0) continue // skip the dash

            val name = prefix + opt
            val option = optionsByName[name] ?: throw NoSuchOption(name)
            val result = option.parser.parseShortOpt(name, argv, index, i)
            parsedValuesByParameter[option]!!.add(result.value)
            if (result.consumedCount > 0) {
                return result.consumedCount
            }
        }
        throw IllegalStateException(
                "Error parsing short option ${argv[index]}: no parser consumed value.")
    }

    private fun parseArguments(positionalArgs: List<String>, arguments: List<Argument<*>>,
                               parsedValuesByParameter: HashMap<Parameter, MutableList<Any?>>): Int {
        // The number of fixed size arguments that occur after an unlimited size argument. This
        // includes optional single value args, so it might be bigger than the number of provided
        // values.
        val endSize = arguments.asReversed()
                .takeWhile { it.nargs > 0 }
                .sumBy { it.nargs }

        var i = 0
        for (parser in arguments) {
            val remaining = positionalArgs.size - i
            val consumed = when {
                parser.nargs <= 0 -> maxOf(0, remaining - endSize)
                parser.nargs == 1 && !parser.required && remaining == 0 -> 0
                else -> parser.nargs
            }
            if (consumed > remaining) {
                throw BadArgumentUsage("argument ${parser.name} takes ${parser.nargs} values")
            }
            val value = parser.parse(positionalArgs.subList(i, i + consumed))
            parsedValuesByParameter[parser]!!.add(value)
            i += consumed
        }

        val excess = positionalArgs.size - i
        if (excess > 0) {
            throw UsageError("Got unexpected extra argument${if (excess == 1) "" else "s"} " +
                    positionalArgs.joinToString(" ", limit = 3, prefix = "(", postfix = ")"))
        }
        return i
    }

    private fun processValues(command: Context,
                              parsedValuesByParameter: HashMap<Parameter, MutableList<Any?>>): Array<Any?> {
        val commandArgs = arrayOfNulls<Any?>(parsedValuesByParameter.count { it.key.exposeValue })
        val indexes = parsedValuesByParameter
                .filter { it.key.exposeValue }
                .keys.withIndex()
                .associateBy({ it.value }, { it.index })
        val process: (Map.Entry<Parameter, MutableList<Any?>>) -> Unit = {
            val result = it.key.processValues(command, it.value)
            indexes[it.key]?.let { commandArgs[it] = result }
        }
        parsedValuesByParameter.filter { it.key.eager }.forEach(process)
        parsedValuesByParameter.filterNot { it.key.eager }.forEach(process)
        return commandArgs
    }
}
