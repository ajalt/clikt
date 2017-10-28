package com.github.ajalt.clikt.samples

import com.github.ajalt.clikt.options.*
import java.util.*
import kotlin.reflect.KFunction

private typealias SubcmdParser = (Array<String>, Int) -> List<ParsedCommand>


@Suppress("ArrayInDataClass")
private data class ParsedCommand(val function: KFunction<*>, val arguments: Array<Any?>)

class ParseError(message: String) : RuntimeException(message)


class Parser {
    private val subcommands = HashMap<KFunction<*>, HashSet<KFunction<*>>>()
    fun addCommand(parent: KFunction<*>, child: KFunction<*>) {
        subcommands[parent] = (subcommands[parent] ?: HashSet()).apply { add(child) }
    }

    fun main(argv: Array<String>, cmd: KFunction<*>) {
        try {
            // Click actually executes parent commands before parsing the argv for subcommands
            for ((f, args) in parse(argv, cmd, 0)) {
                f.call(*args)
            }
        } catch (e: ParseError) {
            println(e)
        }
    }

    private fun parse(argv: Array<String>, cmd: KFunction<*>, currentArgI: Int): List<ParsedCommand> {
        val longOptParsers = HashMap<String, LongOptParser>()
        val shortOptParsers = HashMap<String, ShortOptParser>()
        val commandArgs = arrayOfNulls<Any?>(cmd.parameters.size)

        // Set up long options
        for (param in cmd.parameters) {
            for (anno in param.annotations) {
                when (anno) {
                    is IntOption -> {
                        // TODO typechecks, check name format
                        commandArgs[param.index] = anno.default
                        val parser = IntOptParser(param.index)
                        longOptParsers[anno.name] = parser
                        if (anno.shortName.isNotEmpty()) {
                            shortOptParsers[anno.shortName] = parser
                        }
                    }
                    is FlagOption -> {
                        commandArgs[param.index] = false
                        val parser = FlagOptionParser(param.index)
                        longOptParsers[anno.name] = parser
                        if (anno.shortName.isNotEmpty()) {
                            shortOptParsers[anno.shortName] = parser
                        }
                    }
                    else -> TODO()
                }
            }
        }

        // Set up sub commands
        val subParsers: Map<String, SubcmdParser> = if (cmd !in subcommands) {
            emptyMap()
        } else {
            subcommands[cmd]!!.associateBy({ it.name }, {
                { a: Array<String>, i: Int -> parse(a, it, i) }
            })
        }

        val commands = arrayListOf(ParsedCommand(cmd, commandArgs))

        // parse
        var i = currentArgI
        while (i <= argv.lastIndex) {
            val a = argv[i]
            when {
            // TODO multiple calls to the same flag
                a.startsWith("--") -> {
                    val result = parseLongOpt(argv, a, i, longOptParsers)
                    for (it in result.valuesByCommandArgIndex) {
                        commandArgs[it.key] = it.value
                    }
                    i += result.consumedCount
                }
                a.startsWith("-") -> {
                    val result = parseShortOpt(argv, a, i, shortOptParsers)
                    for (it in result.valuesByCommandArgIndex) {
                        commandArgs[it.key] = it.value
                    }
                    i += result.consumedCount
                }
                a in subParsers -> {
                    return commands.apply { addAll(subParsers[a]!!.invoke(argv, i + 1)) }
                }
                else -> throw ParseError("unknown option $a")
            }
        }

        return commands
    }

    private fun parseLongOpt(argv: Array<String>, arg: String, index: Int, optParsers: Map<String, LongOptParser>): ParseResult {
        val equalsIndex = arg.indexOf('=')
        val (name, value) = if (equalsIndex >= 0) {
            check(equalsIndex != arg.lastIndex) // TODO exceptions
            arg.substring(0, equalsIndex) to arg.substring(equalsIndex + 1)
        } else {
            arg to null
        }
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
        // This should be an error, right?
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
    parser.main(argv, ::run)

//    println((ff.parameters[0].annotations[0] as Foo).bar)
//    ff.parameters[0].type.isMarkedNullable
}
