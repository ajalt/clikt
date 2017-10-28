package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.ArgumentParser
import com.github.ajalt.clikt.options.LongOptParser
import com.github.ajalt.clikt.options.ShortOptParser
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

abstract class Parameter {
    open val shortOptParsersByName: Map<String, ShortOptParser> = emptyMap()
    open val longOptParsersByName: Map<String, LongOptParser> = emptyMap()
    open val argParser: ArgumentParser? = null
    open fun getDefaultValue(context: Context): Any? = null
}

abstract class ParsedParameter<T : Any>(val required: Boolean,
                                        val default: T?,
                                        val metavar: String?) : Parameter() {
    override fun getDefaultValue(context: Context) = default
}

open class Option<T : Any>(protected val names: List<String>,
                           protected val shortOptParser: ShortOptParser, // TODO: move parsing to this class?
                           protected val longOptParser: LongOptParser,
                           required: Boolean, default: T?, metavar: String?) :
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

open class Argument<T : Any>(override val argParser: ArgumentParser,
                             required: Boolean,
                             default: T?,
                             metavar: String?=null) :
        ParsedParameter<T>(required, default, metavar)


@Suppress("AddVarianceModifier")
abstract class ParameterFactory<T : Annotation> {
    abstract fun create(anno: T, funcParam: KParameter): Parameter

    @Suppress("UNCHECKED_CAST")
    internal fun createErased(anno: Annotation, funcParam: KParameter): Parameter {
        return create(anno as T, funcParam)
    }
}


inline fun <reified T : Annotation> param(crossinline block: (T, KParameter) -> Parameter): Pair<KClass<T>, ParameterFactory<T>> {
    return T::class to object : ParameterFactory<T>() {
        override fun create(anno: T, funcParam: KParameter) = block(anno, funcParam)
    }
}

