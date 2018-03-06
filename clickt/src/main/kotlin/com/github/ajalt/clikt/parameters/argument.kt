package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Argument<out T> : ReadOnlyProperty<CliktCommand, T> {
    val name: String
    val nargs: Int
    val required: Boolean
    val metavar: String?
    val help: String
    val parameterHelp: ParameterHelp?
    var rawValues: List<String>
    /** Implementations must call [CliktCommand.registerArgument] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>

    fun finalize(context: Context)

}

class ProcessedArgument<out Tall, Tvalue>(
        override var name: String,
        override val nargs: Int,
        override val required: Boolean,
        override val metavar: String?,
        override val help: String,
        val processValue: ValueProcessor<Tvalue>,
        val processAll: AllProcessor<Tall, Tvalue>) : Argument<Tall> {
    init {
        require(nargs != 0) { "Arguments cannot have nargs == 0" }
    }

    private var value: Tall by ExplicitLazy("Cannot read from argument delegate before parsing command line")

    override var rawValues: List<String> = emptyList()
    override val parameterHelp
        get() = ParameterHelp(listOf(name), metavar, help,
                ParameterHelp.SECTION_ARGUMENTS,
                required && nargs == 1 || nargs > 1, nargs < 0)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>):
            ReadOnlyProperty<CliktCommand, Tall> {
        // TODO: better name inference
        if (name.isBlank()) name = prop.name
        thisRef.registerArgument(this)
        return this
    }

    override fun finalize(context: Context) {
        value = processAll(rawValues.map { processValue(it) })
    }
}

internal typealias RawArgument = ProcessedArgument<String, String>

private fun <T : Any> defaultAllProcessor(): AllProcessor<T, T> = { it.single() }

fun CliktCommand.argument(name: String = "", help: String = ""): RawArgument {
    return ProcessedArgument(name, 1, true, "TEXT", help, { it }, defaultAllProcessor())
}

fun <Talli, Tvalue, Tallo> ProcessedArgument<Talli, Tvalue>.transformAll(
        nargs: Int? = null,
        required: Boolean? = null,
        transform: AllProcessor<Tallo, Tvalue>): ProcessedArgument<Tallo, Tvalue> {
    return ProcessedArgument(name, nargs ?: this.nargs, required
            ?: this.required,
            metavar, help, processValue, transform)
}

fun <Tall : Any, Tvalue> ProcessedArgument<Tall, Tvalue>.optional()
        : ProcessedArgument<Tall?, Tvalue> = transformAll(required = false) {
    if (it.isEmpty()) null else processAll(it)
}

fun <T : Any> ProcessedArgument<T, T>.multiple(required: Boolean = false): ProcessedArgument<List<T>, T> {
    return transformAll(nargs = -1, required = required) { it }
}

fun <T : Any> ProcessedArgument<T, T>.paired(): ProcessedArgument<Pair<T, T>, T> {
    return transformAll(nargs = 2) { it[0] to it[1] }
}

fun <T : Any> ProcessedArgument<T, T>.triple(): ProcessedArgument<Triple<T, T, T>, T> {
    return transformAll(nargs = 3) { Triple(it[0], it[1], it[2]) }
}

fun <T : Any> ProcessedArgument<T, T>.default(value: T): ProcessedArgument<T, T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

@JvmName("nullableDefault")
fun <T : Any> ProcessedArgument<T?, T>.default(value: T): ProcessedArgument<T, T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

fun <T : Any> RawArgument.convert(metavar: String? = null, conversion: (String) -> T): ProcessedArgument<T, T> {
    return ProcessedArgument(name, nargs, required, metavar, help, conversion, defaultAllProcessor())
}

fun <T : Any> ProcessedArgument<T, T>.validate(validator: (T) -> Unit): ProcessedArgument<T, T> {
    return transformAll(required = false) { processAll(it).apply { validator(this) } }
}

