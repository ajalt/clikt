package com.github.ajalt.clikt.parameters.arguments

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parameters.options.transformAll
import com.github.ajalt.clikt.parameters.types.int
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A positional parameter to a command.
 *
 * Arguments can take any number of values.
 */
interface Argument {
    /** The metavar for this argument. */
    val name: String

    /**
     * The number of values that this argument takes. Negative [nargs] indicates a variable number
     *   of values. Cannot be 0.
     */
    val nargs: Int

    /** If true, an error will be thrown if this argument is not given on the command line. */
    val required: Boolean

    /**
     * The description of this argument.
     *
     * It's usually better to leave this null and describe options in the usage line of the command instead.
     */
    val help: String

    /** Information about this argument for the help output. */
    val parameterHelp: ParameterHelp.Argument?

    /**
     * Called after this command's argv is parsed to transform and store the argument's value.
     *
     * @param context The context for this parse
     * @param values A possibly empty list of values provided to this argument.
     */
    fun finalize(context: Context, values: List<String>)
}

/** An argument that functions as a property delegate */
interface ArgumentDelegate<out T> : Argument, ReadOnlyProperty<CliktCommand, T> {
    /** Implementations must call [CliktCommand.registerArgument] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

/**
 * A receiver for argument transformers.
 *
 * @property argument The argument that was invoked
 */
class ArgumentTransformContext(val argument: Argument) : Argument by argument {
    /** Throw an exception indicating that usage was incorrect. */
    fun fail(message: String): Nothing = throw BadParameterValue(message, argument)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    inline fun require(value: Boolean, lazyMessage: () -> String = { "invalid value" }) {
        if (!value) fail(lazyMessage())
    }
}

/** A callback that transforms a single value from a string to the value type */
typealias ArgValueTransformer<T> = ArgumentTransformContext.(String) -> T

/** A callback that transforms all the values into the final argument type */
typealias ArgCallsTransformer<AllT, EachT> = ArgumentTransformContext.(List<EachT>) -> AllT

/** A callback validates the final argument type */
typealias ArgValidator<AllT> = ArgumentTransformContext.(AllT) -> Unit

// `AllT` is deliberately not an out parameter.
@Suppress("AddVarianceModifier")
class ProcessedArgument<AllT, ValueT>(
        name: String,
        override val nargs: Int,
        override val required: Boolean,
        override val help: String,
        val transformValue: ArgValueTransformer<ValueT>,
        val transformAll: ArgCallsTransformer<AllT, ValueT>) : ArgumentDelegate<AllT> {
    init {
        require(nargs != 0) { "Arguments cannot have nargs == 0" }
    }

    override var name: String = name
        private set
    private var value: AllT by NullableLateinit("Cannot read from argument delegate before parsing command line")

    override val parameterHelp
        get() = ParameterHelp.Argument(name, help, required && nargs == 1 || nargs > 1, nargs < 0)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): AllT = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>):
            ReadOnlyProperty<CliktCommand, AllT> {
        if (name.isBlank()) name = prop.name.toUpperCase().replace("-", "_")
        thisRef.registerArgument(this)
        return this
    }

    override fun finalize(context: Context, values: List<String>) {
        val ctx = ArgumentTransformContext(this)
        value = transformAll(ctx, values.map { transformValue(ctx, it) })
    }
}

internal typealias RawArgument = ProcessedArgument<String, String>

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): ArgCallsTransformer<T, T> = { it.single() }

/**
 * Create a property delegate argument.
 *
 * The order that these delegates are created is the order that arguments must appear. By default, the
 * argument takes one value and throws an error if no value is given. The behavior can be changed with
 * functions like [int] and [optional].
 *
 * @param name The metavar for this argument. If not given, the name is inferred form the property name.
 * @param help The description of this argument for help output.
 */
@Suppress("unused")
fun CliktCommand.argument(name: String = "", help: String = ""): RawArgument {
    return ProcessedArgument(name, 1, true, help, { it }, defaultAllProcessor())
}

/**
 * Transform all values to the final argument type.
 *
 * The input is a list of values, one for each value on the command line. The values in the
 * list are the output of calls to [convert]. The input list will have a size of [nargs] if [nargs] is > 0.
 *
 * Used to implement functions like [paired] and [multiple].
 *
 * @param nargs The number of values required by this argument. A negative [nargs] indicates a variable number
 *   of values.
 * @param required If true, an error with be thrown if no values are provided to this argument.
 */
fun <AllInT, ValueT, AllOutT> ProcessedArgument<AllInT, ValueT>.transformAll(
        nargs: Int? = null,
        required: Boolean? = null,
        transform: ArgCallsTransformer<AllOutT, ValueT>): ProcessedArgument<AllOutT, ValueT> {
    return ProcessedArgument(name, nargs ?: this.nargs, required ?: this.required,
            help, transformValue, transform)
}

/** Return null instead of throwing an error if no value is given. */
fun <AllT : Any, ValueT> ProcessedArgument<AllT, ValueT>.optional()
        : ProcessedArgument<AllT?, ValueT> = transformAll(required = false) {
    if (it.isEmpty()) null else transformAll(it)
}

/** Accept any number of values to this argument. */
fun <T : Any> ProcessedArgument<T, T>.multiple(required: Boolean = false): ProcessedArgument<List<T>, T> {
    return transformAll(nargs = -1, required = required) { it }
}

/** Require exactly two values to this argument. */
fun <T : Any> ProcessedArgument<T, T>.paired(): ProcessedArgument<Pair<T, T>, T> {
    return transformAll(nargs = 2) { it[0] to it[1] }
}

/** Require exactly three values to this argument. */
fun <T : Any> ProcessedArgument<T, T>.triple(): ProcessedArgument<Triple<T, T, T>, T> {
    return transformAll(nargs = 3) { Triple(it[0], it[1], it[2]) }
}

/** If the argument is not given, use [value] instead of throwing an error. */
fun <T : Any> ProcessedArgument<T, T>.default(value: T): ArgumentDelegate<T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

/** If the argument is not given, use [value] instead of returning null. */
@JvmName("nullableDefault")
fun <T : Any> ProcessedArgument<T?, T>.default(value: T): ArgumentDelegate<T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

/**
 * Convert the argument's values.
 *
 * The [conversion] is called once for each value given. If any errors are thrown, they are caught and a
 * [BadParameterValue] is thrown with the error message. You can call `fail` to throw a [BadParameterValue]
 * manually.
 */
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

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should call
 * `fail` if the value is not valid.
 */
fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.validate(validator: ArgValidator<AllT>)
        : ArgumentDelegate<AllT> {
    return transformAll(required = false) {
        transformAll(it).also { validator(ArgumentTransformContext(argument), it) }
    }
}

