package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import kotlin.reflect.KParameter

abstract class Parameter {
    open val shortOptParsersByName: Map<String, ShortOptParser> = emptyMap()
    open val longOptParsersByName: Map<String, LongOptParser> = emptyMap()
    open val argParser: ArgumentParser? = null
    open fun getDefaultValue(context: Context): Any? = null
}

abstract class ParsedParameter<out T : Any>(val required: Boolean,
                                            val default: T?,
                                            val metavar: String?) : Parameter() {
    override fun getDefaultValue(context: Context) = default
}

open class Option<out T : Any>(protected val names: List<String>,
                               protected val shortOptParser: ShortOptParser, // TODO: move parsing to this class?
                               protected val longOptParser: LongOptParser,
                               required: Boolean,
                               default: T?,
                               metavar: String?) :
        ParsedParameter<T>(required, default, metavar) {
    init {
        require(names.isNotEmpty()) // TODO messages
        for (name in names) {
            require(name.startsWith("-"))
        }
    }

    override val shortOptParsersByName: Map<String, ShortOptParser>
        get() = names.filter { !it.startsWith("--") }.associateBy({ it }, { shortOptParser })
    override val longOptParsersByName: Map<String, LongOptParser>
        get() = names.filter { it.startsWith("--") }.associateBy({ it }, { longOptParser })
}

open class Argument<T : Any>(final override val name: String,
                             final override val nargs: Int,
                             required: Boolean,
                             default: T?,
                             metavar: String?,
                             protected val type: ParamType<T>,
                             protected val commandArgIndex: Int) :
        ParsedParameter<T>(required, default, metavar), ArgumentParser {
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
}


@Suppress("AddVarianceModifier")
abstract class ParameterFactory<T : Annotation> {
    abstract fun create(anno: T, funcParam: KParameter): Parameter

    @Suppress("UNCHECKED_CAST")
    internal fun createErased(anno: Annotation, funcParam: KParameter): Parameter {
        return create(anno as T, funcParam)
    }
}

