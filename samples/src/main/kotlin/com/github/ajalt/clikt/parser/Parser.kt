package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import java.util.*
import kotlin.reflect.KFunction

class Parser {
    private val contexts = HashMap<KFunction<*>, Context>()

    fun addCommand(parent: KFunction<*>, child: KFunction<*>) {
        val parentContext = contexts.getOrPut(parent) { Context.fromFunction(parent) }
        val childContext = contexts.getOrPut(child) { Context.fromFunction(child) }
        require(childContext.parent == null) // TODO

        parentContext.subcommands.add(childContext)
        childContext.parent = parentContext
    }

    fun main(argv: Array<String>, cmd: KFunction<*>) {
        try {
            parse(argv, cmd)
        } catch (e: CliktError) {
            println(e)
        }
    }

    fun parse(argv: Array<String>, cmd: KFunction<*>) {
        parse(argv, cmd, 0)
    }

    private fun parse(argv: Array<String>, command: KFunction<*>, startingArgI: Int) {
        val context = contexts[command] ?: Context.fromFunction(command)
        val commandArgs = context.defaults.copyOf()
        val subcommands = context.subcommands.associateBy { it.name }

        // parse
        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        loop@ while (i <= argv.lastIndex) {
            val a = argv[i]
            when {
            // TODO multiple calls to the same flag
                a.startsWith("--") -> {
                    val result = parseLongOpt(argv, a, i, context.longOptParsers)
                    applyParseResult(result, commandArgs)
                    i += result.consumedCount
                }
                a.startsWith("-") -> {
                    val result = parseShortOpt(argv, a, i, context.shortOptParsers)
                    applyParseResult(result, commandArgs)
                    i += result.consumedCount
                }
                a in subcommands -> {
                    context.invoke(commandArgs)
                    parse(argv, subcommands[a]!!.command, i + 1)
                    return
                }
                else -> {
                    if (context.allowInterspersedArgs) {
                        positionalArgs += a
                        i += 1
                    } else {
                        positionalArgs += argv.slice(i..argv.lastIndex)
                        break@loop
                    }
                }
            }
        }
        require(subcommands.isEmpty()) // TODO: exceptions, optional subcommands
        applyParseResult(parseArguments(positionalArgs, context.argParsers), commandArgs)
        context.invoke(commandArgs)
    }

    private fun applyParseResult(result: ParseResult, commandArgs: Array<Any?>) {
        for (it in result.valuesByCommandArgIndex) {
            commandArgs[it.key] = it.value
        }
    }

    private fun parseLongOpt(argv: Array<String>, arg: String, index: Int, optParsers: Map<String, LongOptParser>): ParseResult {
        val equalsIndex = arg.indexOf('=')
        val (name, value) = if (equalsIndex >= 0) {
            check(equalsIndex != arg.lastIndex) // TODO exceptions
            arg.substring(0, equalsIndex) to arg.substring(equalsIndex + 1)
        } else {
            arg to null
        }
        if (name !in optParsers) throw NoSuchOption(name)
        return optParsers[name]!!.parseLongOpt(argv, index, value)
    }

    private fun parseShortOpt(argv: Array<String>, arg: String, index: Int, optParsers: Map<String, ShortOptParser>): ParseResult {
        val prefix = arg[0].toString()
        var result = ParseResult.EMPTY
        for ((i, opt) in arg.withIndex()) {
            if (i == 0) continue

            val name = prefix + opt
            if (name !in optParsers) throw NoSuchOption(name)
            result += optParsers[name]!!.parseShortOpt(argv, index, i)
            if (result.consumedCount > 0) {
                return result
            }
        }
        // TODO Should this be an error?
        return result
    }

    private fun parseArguments(positionalArgs: List<String>, argParsers: List<ArgumentParser>): ParseResult {
        // The number of fixed size arguments that occur after an unlimited size argument. This
        // includes optional single value args, so it might be bigger than the number of provided
        // values.
        val endSize = argParsers.asReversed()
                .takeWhile { it.nargs > 0 }
                .sumBy { it.nargs }
        var result = ParseResult.EMPTY

        var i = 0
        for (parser in argParsers) {
            val remaining = positionalArgs.size - i
            val consumed = when {
                parser.nargs <= 0 -> maxOf(0, remaining - endSize)
                parser.nargs == 1 && !parser.required && remaining == 0 -> 0
                else -> parser.nargs
            }
            if (consumed > remaining) {
                throw BadArgumentUsage("argument ${parser.name} takes ${parser.nargs} values")
            }
            result += parser.parse(positionalArgs.subList(i, i + consumed))
            i += consumed
        }

        val excess = positionalArgs.size - i
        if (excess > 0) {
            throw UsageError("Got unexpected extra argument${if (excess == 1) "" else "s"} " +
                    positionalArgs.joinToString(" ", limit = 3, prefix = "(", postfix = ")"))
        }
        return result
    }
}


fun run(@IntOption("--x", "-x") x: Int) {
    println("run x=$x")
}

fun sub(@IntOption("--y") y: Int) {
    println("sub y=$y")
}

fun main(args: Array<String>) {
    val parser = Parser()
    parser.addCommand(::run, ::sub)
    val argv = arrayOf("--x", "313", "sub", "--y", "456")
    parser.parse(argv, ::run)

//    ff.parameters[0].type.isMarkedNullable
}
