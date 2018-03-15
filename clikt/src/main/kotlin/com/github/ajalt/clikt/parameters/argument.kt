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
    fun finalize(context: Context) // TODO take the invocations as a param

    /** Implementations must call [CliktCommand.registerArgument] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

class ArgumentTransformContext(val argument: Argument<*>) {
    fun fail(message: String): Nothing = throw BadParameterValue(message, argument)
}

typealias ArgValueTransformer<T> = ArgumentTransformContext.(String) -> T
typealias ArgCallsTransformer<AllT, EachT> = ArgumentTransformContext.(List<EachT>) -> AllT
typealias ArgValidator<AllT> = ArgumentTransformContext.(AllT) -> Unit

// `AllT` is deliberately not an out parameter.
@Suppress("AddVarianceModifier")
class ProcessedArgument<AllT, ValueT>(
        name: String,
        override val nargs: Int,
        override val required: Boolean,
        override val help: String,
        val processValue: ArgValueTransformer<ValueT>,
        val processAll: ArgCallsTransformer<AllT, ValueT>) : Argument<AllT> {
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
        val ctx = ArgumentTransformContext(this)
        value = processAll(ctx, rawValues.map { processValue(ctx, it) })
    }
}

internal typealias RawArgument = ProcessedArgument<String, String>

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): ArgCallsTransformer<T, T> = { it.single() }

@Suppress("unused")
fun CliktCommand.argument(name: String = "", help: String = ""): RawArgument {
    return ProcessedArgument(name, 1, true, help, { it }, defaultAllProcessor())
}

fun <AllInT, ValueT, AllOutT> ProcessedArgument<AllInT, ValueT>.transformAll(
        nargs: Int? = null,
        required: Boolean? = null,
        transform: ArgCallsTransformer<AllOutT, ValueT>): ProcessedArgument<AllOutT, ValueT> {
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

fun <T : Any> ProcessedArgument<T, T>.default(value: T): Argument<T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

@JvmName("nullableDefault")
fun <T : Any> ProcessedArgument<T?, T>.default(value: T): Argument<T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

inline fun <T : Any> RawArgument.convert(crossinline conversion: ArgValueTransformer<T>): ProcessedArgument<T, T> {
    val proc: ArgValueTransformer<T> = {
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

fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.validate(validator: ArgValidator<AllT>)
        : Argument<AllT> {
    return transformAll(required = false) {
        processAll(it).also { validator(ArgumentTransformContext(this@validate), it) }
    }
}

