package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Argument<out T> : ReadOnlyProperty<CliktCommand, T> {
    val name: String
    val nargs: Int
    val required: Boolean
    val help: String
    val parameterHelp: ParameterHelp?
    var rawValues: List<String>
    fun finalize(context: Context)

    /** Implementations must call [CliktCommand.registerArgument] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

class ArgumentValueInvocation(val argument: Argument<*>) {
    fun fail(message: String): Nothing = throw BadParameterValue(message, argument)
}

private typealias ArgValueProcessor<T> = ArgumentValueInvocation.(String) -> T
private typealias ArgAllProcessor<AllT, EachT> = Argument<*>.(List<EachT>) -> AllT

// `AllT` is deliberately not an out parameter.
@Suppress("AddVarianceModifier")
class ProcessedArgument<AllT, ValueT>(
        name: String,
        override val nargs: Int,
        override val required: Boolean,
        override val help: String,
        val processValue: ArgValueProcessor<ValueT>,
        val processAll: ArgAllProcessor<AllT, ValueT>) : Argument<AllT> {
    init {
        require(nargs != 0) { "Arguments cannot have nargs == 0" }
    }

    override var name: String = name
        private set
    private var value: AllT by NullableLateinit("Cannot read from argument delegate before parsing command line")

    override var rawValues: List<String> = emptyList()
    override val parameterHelp
        get() = ParameterHelp.Argument(name, help, required && nargs == 1 || nargs > 1, nargs < 0)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): AllT = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>):
            ReadOnlyProperty<CliktCommand, AllT> {
        if (name.isBlank()) name = prop.name.toUpperCase().replace("-", "_")
        thisRef.registerArgument(this)
        return this
    }

    override fun finalize(context: Context) {
        value = processAll(rawValues.map { processValue(ArgumentValueInvocation(this), it) })
    }
}

internal typealias RawArgument = ProcessedArgument<String, String>

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): ArgAllProcessor<T, T> = { it.single() }

@Suppress("unused")
fun CliktCommand.argument(name: String = "", help: String = ""): RawArgument {
    return ProcessedArgument(name, 1, true, help, { it }, defaultAllProcessor())
}

fun <AllInT, ValueT, AllOutT> ProcessedArgument<AllInT, ValueT>.transformAll(
        nargs: Int? = null,
        required: Boolean? = null,
        transform: ArgAllProcessor<AllOutT, ValueT>): ProcessedArgument<AllOutT, ValueT> {
    return ProcessedArgument(name, nargs ?: this.nargs, required ?: this.required,
            help, processValue, transform)
}

fun <AllT : Any, ValueT> ProcessedArgument<AllT, ValueT>.optional()
        : ProcessedArgument<AllT?, ValueT> = transformAll(required = false) {
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

inline fun <T : Any> RawArgument.convert(crossinline conversion: ArgValueProcessor<T>): ProcessedArgument<T, T> {
    val proc: ArgValueProcessor<T> = {
        try {
            conversion(it)
        } catch (err: UsageError) {
            err.argument = argument
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return ProcessedArgument(name, nargs, required, help, proc, defaultAllProcessor())
}

fun <T : Any> ProcessedArgument<T, T>.validate(validator: (T) -> Unit): Argument<T> {
    return transformAll(required = false) { processAll(it).apply { validator(this) } }
}

