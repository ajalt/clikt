package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible

class Context(parent: Context?, val name: String, var obj: Any?,
              val defaults: Array<Any?>, val allowInterspersedArgs: Boolean,
              internal val command: KFunction<*>,
              internal val longOptParsers: Map<String, LongOptParser>,
              internal val shortOptParsers: Map<String, ShortOptParser>,
              internal val argParsers: List<ArgumentParser>,
              internal val subcommands: HashSet<Context>,
              private val customAnnos: Map<Int, Annotation>) {
    var parent: Context? = parent
        internal set

    fun invoke(args: Array<Any?>) {
        command.isAccessible = true

        for ((i, anno) in customAnnos) {
            if (args[i] != null) continue  // TODO decide if this is the behavior we want

            when (anno) {
                is com.github.ajalt.clikt.options.PassContext -> args[i] = this
            }
        }
        command.call(*args)
    }

    companion object {
        fun fromFunction(command: KFunction<*>): Context {
            val longOptParsers = HashMap<String, LongOptParser>()
            val shortOptParsers = HashMap<String, ShortOptParser>()
            val defaults = arrayOfNulls<Any?>(command.parameters.size)
            val argParsers = ArrayList<ArgumentParser>()
            val customAnnos = HashMap<Int, Annotation>()

            fun registerOptNames(shortParser: ShortOptParser, longParser: LongOptParser, vararg names: String) {
                for (name in names) {
                    when {
                        name.isEmpty() -> Unit
                        name.startsWith("--") -> longOptParsers[name] = longParser
                        name.startsWith("-") -> shortOptParsers[name] = shortParser
                        else -> throw IllegalArgumentException("Invalid option name: $name")
                    }
                }
            }

            // Set up long options
            for (param in command.parameters) {
                for (anno in param.annotations) {
                    when (anno) {
                        is IntOption -> {
                            // TODO typechecks, check name format, check that names are unique
                            defaults[param.index] = anno.default
                            val parser = OptionParser(param.index, IntParamType)
                            registerOptNames(parser, parser, *getOptionNames(anno.names, param))
                        }
                        is FlagOption -> {
                            defaults[param.index] = false
                            val parser = FlagOptionParser(param.index)
                            registerOptNames(parser, parser, *getOptionNames(anno.names, param))
                        }
                        is IntArgument -> {
                            require(anno.nargs != 0) // TODO exceptions, check that param is a list if nargs != 1
                            if (anno.nargs == 1 && !anno.required) defaults[param.index] = anno.default
                            val name = if (anno.name.isBlank()) param.name ?: "ARGUMENT" else anno.name
                            argParsers.add(TypedArgumentParser(name, anno.nargs,
                                    anno.required, param.index, IntParamType))

                        }
                        is com.github.ajalt.clikt.options.PassContext -> {
                            customAnnos[param.index] = anno
                        }
                        else -> TODO()
                    }
                }
            }
            val name = command.name // TODO allow customization
            return Context(null, name, null, defaults, true, command, longOptParsers,
                    shortOptParsers, argParsers, HashSet(), customAnnos)
        }

        private fun getOptionNames(names: Array<out String>, param: KParameter) =
                if (names.isNotEmpty()) names
                else {
                    require(!param.name.isNullOrEmpty()) { "Cannot infer option name; specify it explicitly." }
                    arrayOf("--" + param.name)
                }
    }
}
