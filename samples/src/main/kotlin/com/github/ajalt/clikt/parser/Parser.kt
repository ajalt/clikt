package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.IntOption
import com.github.ajalt.clikt.options.LongOptParser
import com.github.ajalt.clikt.options.ParseResult
import com.github.ajalt.clikt.options.ShortOptParser
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible

class ParseError(message: String) : RuntimeException(message)

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
        } catch (e: ParseError) {
            println(e)
        }
    }

    fun parse(argv: Array<String>, cmd: KFunction<*>) {
        parse(argv, cmd, 0)
    }

    private fun parse(argv: Array<String>, command: KFunction<*>, startingArgI: Int) {
        command.isAccessible = true
        val context = contexts[command] ?: Context.fromFunction(command)
        val commandArgs = context.defaults.copyOf()
        val subcommands = context.subcommands.associateBy { it.name }

        // parse
        val positionalArgs = ArrayList<String>()
        var i = startingArgI
        while (i <= argv.lastIndex) {
            val a = argv[i]
            when {
            // TODO multiple calls to the same flag
                a.startsWith("--") -> {
                    val result = parseLongOpt(argv, a, i, context.longOptParsers)
                    for (it in result.valuesByCommandArgIndex) {
                        commandArgs[it.key] = it.value
                    }
                    i += result.consumedCount
                }
                a.startsWith("-") -> {
                    val result = parseShortOpt(argv, a, i, context.shortOptParsers)
                    for (it in result.valuesByCommandArgIndex) {
                        commandArgs[it.key] = it.value
                    }
                    i += result.consumedCount
                }
                a in subcommands -> {
                    command.call(*commandArgs)
                    parse(argv, subcommands[a]!!.command, i + 1)
                    return
                }
                else -> throw ParseError("unknown option $a")
            }
        }
        require(subcommands.isEmpty()) // TODO
        command.call(*commandArgs)
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

            result += optParsers[prefix + opt]!!.parseShortOpt(argv, index, i) // TODO exceptions
            if (result.consumedCount > 0) {
                return result
            }
        }
        // TODO Should this be an error?
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
