package com.github.ajalt.clikt.samples

import java.util.*
import kotlin.reflect.KFunction

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntOption(val name: String, val shortName: String = "", val default: Int = 0)

private typealias CmdParser = (Array<String>, Int) -> Int
private typealias SubcmdParser = (Array<String>, Int) -> List<ParsedCommand>

@Suppress("ArrayInDataClass")
private data class ParsedCommand(val function: KFunction<*>, val arguments: Array<Any?>)

class ParseError(message: String) : RuntimeException(message)

class Context(val parent: Context, val info_name: String, var obj: Any?)

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
        val longOptParsers = HashMap<String, CmdParser>()
        val args = arrayOfNulls<Any?>(cmd.parameters.size)

        // Set up long options
        for (param in cmd.parameters) {
            for (anno in param.annotations) {
                when (anno) {
                    is IntOption -> {
                        args[param.index] = anno.default
                        longOptParsers[anno.name] = { a, i ->
                            // This currently assumes an arg in the form `--foo 123`
                            args[param.index] = a[i + 1].toInt()
                            1
                        }
                    }
                }
                // fail if no match
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

        val commands = arrayListOf(ParsedCommand(cmd, args))

        // parse
        var i = currentArgI
        while (i <= argv.lastIndex) {
            val a = argv[i]
            when {
                a.startsWith("--") -> i += longOptParsers[a]!!.invoke(argv, i)
                a.startsWith("-") -> TODO()
                a in subParsers -> {
                    return commands.apply { addAll(subParsers[a]!!.invoke(argv, i + 1)) }
                }
                else -> throw ParseError("unknown option $a")
            }
            i += 1
        }

        return commands
    }
}


fun run(@IntOption("--x") x: Int) {
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
