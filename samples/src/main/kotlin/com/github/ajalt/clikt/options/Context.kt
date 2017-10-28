package com.github.ajalt.clikt.options

import java.util.HashMap
import kotlin.reflect.KFunction

class Context(var parent: Context?, val name: String, var obj: Any?,
              val defaults: Array<Any?>,
              internal val command: KFunction<*>,
              internal val longOptParsers: Map<String, LongOptParser>,
              internal val shortOptParsers: Map<String, ShortOptParser>,
              internal val subcommands: HashSet<Context>) {
    companion object {
        fun fromFunction(command: KFunction<*>): Context {
            val longOptParsers = HashMap<String, LongOptParser>()
            val shortOptParsers = HashMap<String, ShortOptParser>()
            val defaults = arrayOfNulls<Any?>(command.parameters.size)

            // Set up long options
            for (param in command.parameters) {
                for (anno in param.annotations) {
                    when (anno) {
                        is IntOption -> {
                            // TODO typechecks, check name format
                            defaults[param.index] = anno.default
                            val parser = IntOptParser(param.index)
                            longOptParsers[anno.name] = parser
                            if (anno.shortName.isNotEmpty()) {
                                shortOptParsers[anno.shortName] = parser
                            }
                        }
                        is FlagOption -> {
                            defaults[param.index] = false
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
            val name = command.name // TODO
            return Context(null, name, null, defaults, command, longOptParsers,
                    shortOptParsers, HashSet())
        }
    }
}
