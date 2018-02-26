package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.options.ClicktCommand
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Argument<out T> : ReadOnlyProperty<CliktCommand, T> {
    val name: String
    val nargs: Int
    val required: Boolean
    val default: T?
    val metavar: String?
    val help: String
    val parameterHelp: ParameterHelp?
    var rawValues: List<String>

}

interface ProcessedArgument<out Tall, Tvalue> : Argument<Tall> {
    fun processValue(value: String): Tvalue
    fun processAll(values: List<Tvalue>): Tall
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall {
        return processAll(rawValues.map { processValue(it) })
    }
}

internal class ArgumentImpl(name: String?, override val help: String) : ProcessedArgument<String, String> {
    override var name: String = ""
        private set

    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, String?> {
        // TODO: better name inference
        if (name.isBlank()) name = prop.name
        return this
    }

    override val parameterHelp: ParameterHelp
        get() = ParameterHelp(listOf(name), metavar, help,
                ParameterHelp.SECTION_ARGUMENTS, required && nargs == 1 || nargs > 1, nargs < 0)
    override var rawValues: List<String> = emptyList()
    override val nargs: Int get() = 1  // TODO require this to be != 0 somewhere
    override val required: Boolean get() = default == null && nargs > 0
    override val default: String? get() = null  // TODO check that this is null when nargs < 0
    override val metavar: String? get() = null
    override fun processValue(value: String): String = value
    override fun processAll(values: List<String>): String = values.single()  // nargs are checked in the parser
}

fun CliktCommand.argument(name: String? = null, help: String = ""): ProcessedArgument<String, String> {
    return ArgumentImpl(name, help)
}
