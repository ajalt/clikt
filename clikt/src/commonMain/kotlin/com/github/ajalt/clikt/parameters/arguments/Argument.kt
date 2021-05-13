package com.github.ajalt.clikt.parameters.arguments

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import kotlin.js.JsName
import kotlin.jvm.JvmName
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
     * The number of values that this argument takes.
     *
     * Negative [nvalues] indicates a variable number of values. Cannot be 0.
     */
    val nvalues: Int

    /** If true, an error will be thrown if this argument is not given on the command line. */
    val required: Boolean

    /**
     * The description of this argument.
     *
     * It's usually better to leave this null and describe options in the usage line of the command instead.
     */
    val argumentHelp: String

    /** Extra information about this argument to pass to the help formatter. */
    val helpTags: Map<String, String>

    /** Optional set of strings to use when the user invokes shell autocomplete on a value for this argument. */
    val completionCandidates: CompletionCandidates get() = CompletionCandidates.None

    /** Information about this argument for the help output. */
    fun parameterHelp(context: Context): ParameterHelp.Argument?

    /**
     * Called after this command's argv is parsed to transform and store the argument's value.
     *
     * You cannot refer to other parameter values during this call, since they might not have been
     * finalized yet.
     *
     * @param context The context for this parse
     * @param values A possibly empty list of values provided to this argument.
     */
    fun finalize(context: Context, values: List<String>)

    /**
     * Called after all of a command's parameters have been [finalize]d to perform validation of the final value.
     */
    fun postValidate(context: Context)
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
class ArgumentTransformContext(val argument: Argument, val context: Context) : Argument by argument {
    /** Throw an exception indicating that usage was incorrect. */
    fun fail(message: String): Nothing = throw BadParameterValue(message, argument)

    /** Issue a message that can be shown to the user */
    fun message(message: String) = context.command.issueMessage(message)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    inline fun require(value: Boolean, lazyMessage: () -> String = { "" }) {
        if (!value) fail(lazyMessage())
    }
}

/** A callback that transforms a single value from a string to the value type */
typealias ArgValueTransformer<T> = ArgValueConverter<String, T>

/** A callback that transforms a single value from one type to another */
typealias ArgValueConverter<InT, ValueT> = ArgumentTransformContext.(InT) -> ValueT

/** A callback that transforms all the values into the final argument type */
typealias ArgCallsTransformer<AllT, EachT> = ArgumentTransformContext.(List<EachT>) -> AllT

/** A callback validates the final argument type */
typealias ArgValidator<AllT> = ArgumentTransformContext.(AllT) -> Unit

/**
 * An [Argument] delegate implementation that transforms its values .
 *
 * @property transformValue Called in [finalize] to transform each value provided to the argument.
 * @property transformAll Called in [finalize] to transform the list of values to the final type.
 * @property transformValidator Called after all parameters have been [finalize]d to validate the result of [transformAll]
 */
class ProcessedArgument<AllT, ValueT> internal constructor(
        name: String,
        override val nvalues: Int,
        override val required: Boolean,
        override val argumentHelp: String,
        override val helpTags: Map<String, String>,
        val completionCandidatesWithDefault: ValueWithDefault<CompletionCandidates>,
        val transformValue: ArgValueTransformer<ValueT>,
        val transformAll: ArgCallsTransformer<AllT, ValueT>,
        val transformValidator: ArgValidator<AllT>
) : ArgumentDelegate<AllT> {
    init {
        require(nvalues != 0) { "Arguments cannot have nvalues == 0" }
    }

    override var name: String = name
        private set
    internal var value: AllT by NullableLateinit("Cannot read from argument delegate before parsing command line")
        private set
    override val completionCandidates: CompletionCandidates
        get() = completionCandidatesWithDefault.value

    override fun parameterHelp(context: Context) = ParameterHelp.Argument(name, argumentHelp, required || nvalues > 1, nvalues < 0, helpTags)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): AllT = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>):
            ReadOnlyProperty<CliktCommand, AllT> {
        if (name.isBlank()) name = prop.name.uppercase().replace("-", "_")
        thisRef.registerArgument(this)
        return this
    }

    override fun finalize(context: Context, values: List<String>) {
        val ctx = ArgumentTransformContext(this, context)
        value = transformAll(ctx, values.map { transformValue(ctx, it) })
    }

    override fun postValidate(context: Context) {
        transformValidator(ArgumentTransformContext(this, context), value)
    }

    /** Create a new argument that is a copy of this one with different transforms. */
    fun <AllT, ValueT> copy(
            transformValue: ArgValueTransformer<ValueT>,
            transformAll: ArgCallsTransformer<AllT, ValueT>,
            validator: ArgValidator<AllT>,
            name: String = this.name,
            nvalues: Int = this.nvalues,
            required: Boolean = this.required,
            help: String = this.argumentHelp,
            helpTags: Map<String, String> = this.helpTags,
            completionCandidatesWithDefault: ValueWithDefault<CompletionCandidates> = this.completionCandidatesWithDefault
    ): ProcessedArgument<AllT, ValueT> {
        return ProcessedArgument(name, nvalues, required, help, helpTags, completionCandidatesWithDefault, transformValue, transformAll, validator)
    }

    /** Create a new argument that is a copy of this one with the same transforms. */
    fun copy(
            validator: ArgValidator<AllT> = this.transformValidator,
            name: String = this.name,
            nvalues: Int = this.nvalues,
            required: Boolean = this.required,
            help: String = this.argumentHelp,
            helpTags: Map<String, String> = this.helpTags,
            completionCandidatesWithDefault: ValueWithDefault<CompletionCandidates> = this.completionCandidatesWithDefault
    ): ProcessedArgument<AllT, ValueT> {
        return ProcessedArgument(name, nvalues, required, help, helpTags, completionCandidatesWithDefault, transformValue, transformAll, validator)
    }
}

typealias RawArgument = ProcessedArgument<String, String>

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): ArgCallsTransformer<T, T> = { it.single() }

@PublishedApi
internal fun <T> defaultValidator(): ArgValidator<T> = {}

/**
 * Create a property delegate argument.
 *
 * The order that these delegates are created is the order that arguments must appear. By default, the
 * argument takes one value and throws an error if no value is given. The behavior can be changed with
 * functions like [int] and [optional].
 *
 * @param name The metavar for this argument. If not given, the name is inferred form the property name.
 * @param help The description of this argument for help output.
 * @param helpTags Extra information about this option to pass to the help formatter
 */
@Suppress("unused")
fun CliktCommand.argument(
        name: String = "",
        help: String = "",
        helpTags: Map<String, String> = emptyMap(),
        completionCandidates: CompletionCandidates? = null
): RawArgument {
    return ProcessedArgument(
            name = name,
            nvalues = 1,
            required = true,
            argumentHelp = help,
            helpTags = helpTags,
            completionCandidatesWithDefault = ValueWithDefault(completionCandidates, CompletionCandidates.None),
            transformValue = { it },
            transformAll = defaultAllProcessor(),
            transformValidator = defaultValidator()
    )
}

/**
 * Set the help for this argument.
 *
 * Although you would normally pass the help string as an argument to [argument], this function
 * can be more convenient for long help strings.
 *
 * ### Example:
 *
 * ```
 * val number by argument()
 *      .int()
 *      .help("This is an argument that takes a number")
 * ```
 */
fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.help(help: String): ProcessedArgument<AllT, ValueT> {
    return copy(help = help)
}

/**
 * Transform all values to the final argument type.
 *
 * The input is a list of values, one for each value on the command line. The values in the
 * list are the output of calls to [convert]. The input list will have a size of [nvalues] if
 * [nvalues] is > 0.
 *
 * Used to implement functions like [pair] and [multiple].
 *
 * ## Example
 *
 * ```
 * val entries by argument().transformAll { it.joinToString() }
 * ```
 *
 * @param nvalues The number of values required by this argument. A negative [nvalues] indicates a
 *   variable number of values.
 * @param required If true, an error with be thrown if no values are provided to this argument.
 */
fun <AllInT, ValueT, AllOutT> ProcessedArgument<AllInT, ValueT>.transformAll(
        nvalues: Int? = null,
        required: Boolean? = null,
        transform: ArgCallsTransformer<AllOutT, ValueT>): ProcessedArgument<AllOutT, ValueT> {
    return copy(transformValue, transform, defaultValidator(),
            nvalues = nvalues ?: this.nvalues,
            required = required ?: this.required)
}

/**
 * Return null instead of throwing an error if no value is given.
 *
 * This must be called after all other transforms.
 *
 * ### Example:
 *
 * ```
 * val arg: Int? by argument().int().optional()
 * ```
 */
fun <AllT : Any, ValueT> ProcessedArgument<AllT, ValueT>.optional(): ProcessedArgument<AllT?, ValueT> {
    return transformAll(required = false) { if (it.isEmpty()) null else transformAll(it) }
}

/**
 * Accept any number of values to this argument.
 *
 * Only one argument in a command may use this function, and the command may not have subcommands. This must
 * be called after all other transforms.
 *
 * ### Example:
 *
 * ```
 * val arg: List<Int> by argument().int().multiple()
 * ```
 */
fun <T : Any> ProcessedArgument<T, T>.multiple(required: Boolean = false): ProcessedArgument<List<T>, T> {
    return transformAll(nvalues = -1, required = required) { it }
}

/**
 * Only store unique values for this argument
 *
 * ### Example:
 *
 * ```
 * val arg: Set<Int> by argument().int().multiple().unique()
 * ```
 */
fun <T : Any> ProcessedArgument<List<T>, T>.unique(): ProcessedArgument<Set<T>, T> {
    return transformAll(nvalues = -1) { it.toSet() }
}

/**
 * Require exactly two values to this argument, and store them in a [Pair].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val arg: Pair<Int, Int> by argument().int().pair()
 * ```
 */
fun <T : Any> ProcessedArgument<T, T>.pair(): ProcessedArgument<Pair<T, T>, T> {
    return transformAll(nvalues = 2) { it[0] to it[1] }
}

/**
 * Require exactly three values to this argument, and store them in a [Triple]
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val arg: Triple<Int, Int, Int> by argument().int().triple()
 * ```
 */
fun <T : Any> ProcessedArgument<T, T>.triple(): ProcessedArgument<Triple<T, T, T>, T> {
    return transformAll(nvalues = 3) { Triple(it[0], it[1], it[2]) }
}

/**
 * If the argument is not given, use [value] instead of throwing an error.
 *
 * This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```
 * val arg: Pair<Int, Int> by argument().int().pair().default(1 to 2)
 * ```
 */
fun <T : Any> ProcessedArgument<T, T>.default(value: T): ArgumentDelegate<T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

/**
 * If the argument is not given, call [value] and use its return value instead of throwing an error.
 *
 * This must be applied after all other transforms. If the argument is given on the command line, [value] will
 * not be called.
 *
 * ### Example:
 *
 * ```
 * val arg: Pair<Int, Int> by argument().int().pair().defaultLazy { expensiveOperation() }
 * ```
 */
inline fun <T : Any> ProcessedArgument<T, T>.defaultLazy(crossinline value: () -> T): ArgumentDelegate<T> {
    return transformAll(required = false) { it.firstOrNull() ?: value() }
}

/**
 * Convert the argument's values.
 *
 * The [conversion] is called once for each value given. If any errors are thrown, they are caught and a
 * [BadParameterValue] is thrown with the error message. You can call `fail` to throw a [BadParameterValue]
 * manually.
 *
 * You can call `convert` more than once to wrap the result of the previous `convert`, but it cannot
 * be called after [transformAll] (e.g. [multiple]) or [transformValues] (e.g. [pair]).
 *
 * ## Example
 *
 * ```
 * val bd: BigDecimal by argument().convert { it.toBigDecimal() }
 * val fileText: ByteArray by argument().file().convert { it.readBytes() }
 * ```
 *
 * @param completionCandidates candidates to use when completing this argument in shell autocomplete,
 *   if no candidates are specified in [argument]
 */
inline fun <InT : Any, ValueT : Any> ProcessedArgument<InT, InT>.convert(
        completionCandidates: CompletionCandidates = completionCandidatesWithDefault.default,
        crossinline conversion: ArgValueConverter<InT, ValueT>
): ProcessedArgument<ValueT, ValueT> {
    val conv: ArgValueTransformer<ValueT> = {
        try {
            conversion(transformValue(it))
        } catch (err: UsageError) {
            err.argument = argument
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return copy(conv, defaultAllProcessor(), defaultValidator(),
            completionCandidatesWithDefault = completionCandidatesWithDefault.copy(default = completionCandidates)
    )
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should
 * call [fail][ArgumentTransformContext.fail] if the value is not valid.
 *
 * Your [validator] can also call [require][ArgumentTransformContext.require] to fail automatically
 * if an expression is false, or [message][ArgumentTransformContext.message] to show the user a
 * warning message without aborting.
 *
 * ### Example:
 *
 * ```
 * val arg by argument().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
fun <AllT : Any, ValueT> ProcessedArgument<AllT, ValueT>.validate(validator: ArgValidator<AllT>)
        : ArgumentDelegate<AllT> {
    return copy(validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should
 * call [fail][ArgumentTransformContext.fail] if the value is not valid. The [validator] is not
 * called if the delegate value is null.
 *
 * Your [validator] can also call [require][ArgumentTransformContext.require] to fail automatically
 * if an expression is false, or [message][ArgumentTransformContext.message] to show the user a
 * warning message without aborting.
 *
 * ### Example:
 *
 * ```
 * val arg by argument().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
@JvmName("nullableValidate")
@JsName("nullableValidate")
fun <AllT : Any, ValueT> ProcessedArgument<AllT?, ValueT>.validate(validator: ArgValidator<AllT>)
        : ArgumentDelegate<AllT?> {
    return copy({ if (it != null) validator(it) })
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [message] to include in the error
 * output.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val arg by argument().int().check("value must be even") { it % 2 == 0 }
 * ```
 */
inline fun <AllT : Any, ValueT> ProcessedArgument<AllT, ValueT>.check(
        message: String,
        crossinline validator: (AllT) -> Boolean
): ArgumentDelegate<AllT> {
    return check({ message }, validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [lazyMessage] the returns a message
 * to include in the error output.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val arg by argument().int().check(lazyMessage={"$it is not even"}) { it % 2 == 0 }
 * ```
 */
inline fun <AllT : Any, ValueT> ProcessedArgument<AllT, ValueT>.check(
        crossinline lazyMessage: (AllT) -> String = { it.toString() },
        crossinline validator: (AllT) -> Boolean
): ArgumentDelegate<AllT> {
    return validate { require(validator(it)) { lazyMessage(it) } }
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [message] to include in the error
 * output. The [validator] is not called if the delegate value is null.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val arg by argument().int().check("value must be even") { it % 2 == 0 }
 * ```
 */
@JvmName("nullableCheck")
@JsName("nullableCheck")
inline fun <AllT : Any, ValueT> ProcessedArgument<AllT?, ValueT>.check(
        message: String,
        crossinline validator: (AllT) -> Boolean
): ArgumentDelegate<AllT?> {
    return check({ message }, validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [lazyMessage] the returns a message
 * to include in the error output. The [validator] is not called if the delegate value is null.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val arg by argument().int().check(lazyMessage={"$it is not even"}) { it % 2 == 0 }
 * ```
 */
@JvmName("nullableLazyCheck")
@JsName("nullableLazyCheck")
inline fun <AllT : Any, ValueT> ProcessedArgument<AllT?, ValueT>.check(
        crossinline lazyMessage: (AllT) -> String = { it.toString() },
        crossinline validator: (AllT) -> Boolean
): ArgumentDelegate<AllT?> {
    return validate { require(validator(it)) { lazyMessage(it) } }
}
