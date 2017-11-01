package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.ArgumentParser
import com.github.ajalt.clikt.options.OptionParser
import com.github.ajalt.clikt.options.ParamType
import com.github.ajalt.clikt.options.ParseResult
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import kotlin.reflect.KParameter

abstract class Parameter {
    open val parsersByName: Map<String, OptionParser> = emptyMap()
    open val argParser: ArgumentParser? = null
    open fun getDefaultValue(context: Context): Any? = null
    abstract val parameterHelp: ParameterHelp?
}

abstract class ParsedParameter<out T : Any>(val required: Boolean,
                                            val default: T?,
                                            val metavar: String?,
                                            val help: String) : Parameter() {
    override fun getDefaultValue(context: Context) = default
}

open class Option<out T : Any>(protected val names: List<String>,
                               protected val parser: OptionParser, // TODO: move parsing to this class?
                               required: Boolean,
                               default: T?,
                               metavar: String?,
                               help: String) :
        ParsedParameter<T>(required, default, metavar, help) {
    init {
        require(names.isNotEmpty()) // TODO messages
        for (name in names) {
            require(name.startsWith("-"))
        }
    }

    override val parsersByName: Map<String, OptionParser>
        get() = names.associateBy({ it }, { parser })

    override val parameterHelp: ParameterHelp
        get() = ParameterHelp(names, metavar,
                help,
                ParameterHelp.SECTION_OPTIONS,
                required, false) // TODO: repeatable
}

open class Argument<T : Any>(final override val name: String,
                             final override val nargs: Int,
                             required: Boolean,
                             default: T?,
                             metavar: String?,
                             protected val type: ParamType<T>,
                             protected val commandArgIndex: Int, help: String) :
        ParsedParameter<T>(required, default, metavar, help), ArgumentParser {
    init {
        require(nargs != 0)
        require(commandArgIndex >= 0)
    }

    override val argParser get() = this

    override fun parse(args: List<String>): ParseResult {
        if (nargs == 1 && !required && args.isEmpty()) return ParseResult.EMPTY // TODO is there a better way to do this?
        val value: Any? = if (nargs == 1) type.convert(args[0]) else args.map { type.convert(it) }
        return ParseResult(0, value, commandArgIndex)
    }

    override val parameterHelp: ParameterHelp
        get() = ParameterHelp(listOf(name), metavar, help,
                ParameterHelp.SECTION_ARGUMENTS, required && nargs == 1 || nargs > 1, nargs < 0)
}


@Suppress("AddVarianceModifier")
abstract class ParameterFactory<T : Annotation> {
    abstract fun create(anno: T, funcParam: KParameter): Parameter

    @Suppress("UNCHECKED_CAST")
    internal fun createErased(anno: Annotation, funcParam: KParameter): Parameter {
        return create(anno as T, funcParam)
    }
}

