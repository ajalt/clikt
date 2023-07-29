package com.github.ajalt.clikt.parameters.arguments

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.transform.HelpTransformContext
import com.github.ajalt.clikt.parameters.transform.TransformContext
import com.github.ajalt.clikt.parameters.transform.message
import com.github.ajalt.clikt.parameters.types.int
import kotlin.jvm.JvmOverloads
import kotlin.properties.PropertyDelegateProvider
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
    fun getArgumentHelp(context: Context): String

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
interface ArgumentDelegate<out T> :
    Argument,
    ReadOnlyProperty<CliktCommand, T>,
    PropertyDelegateProvider<CliktCommand, ReadOnlyProperty<CliktCommand, T>> {
    /**
     * The value for this argument.
     *
     * @throws IllegalStateException if this property is accessed before [finalize] is called.
     */
    val value: T

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T = value
}

/** A receiver for argument transformers. */
class ArgumentTransformContext(
    /** The argument that was invoked */
    val argument: Argument,
    override val context: Context,
) : Argument by argument, TransformContext {
    override fun fail(message: String): Nothing = throw BadParameterValue(message, argument)

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
 */
interface ProcessedArgument<AllT, ValueT> : ArgumentDelegate<AllT> {
    /** A block that will return the help text for this argument, or `null` if no getter has been specified */
    val helpGetter: (HelpTransformContext.() -> String)?

    /** Called in [finalize] to transform each value provided to the argument. */
    val transformValue: ArgValueTransformer<ValueT>

    /** Called in [finalize] to transform the list of values to the final type. */
    val transformAll: ArgCallsTransformer<AllT, ValueT>

    /** Called after all parameters have been [finalize]d to validate the result of [transformAll] */
    val transformValidator: ArgValidator<AllT>

    /** The completion candidates set on this argument, or `null` if no candidates have been set */
    val explicitCompletionCandidates: CompletionCandidates?

    /** Create a new argument that is a copy of this one with different transforms. */
    fun <AllT, ValueT> copy(
        transformValue: ArgValueTransformer<ValueT>,
        transformAll: ArgCallsTransformer<AllT, ValueT>,
        validator: ArgValidator<AllT>,
        name: String = this.name,
        nvalues: Int = this.nvalues,
        required: Boolean = this.required,
        helpGetter: (HelpTransformContext.() -> String)? = this.helpGetter,
        helpTags: Map<String, String> = this.helpTags,
        completionCandidates: CompletionCandidates? = explicitCompletionCandidates,
    ): ProcessedArgument<AllT, ValueT>

    /** Create a new argument that is a copy of this one with the same transforms. */
    fun copy(
        validator: ArgValidator<AllT> = this.transformValidator,
        name: String = this.name,
        nvalues: Int = this.nvalues,
        required: Boolean = this.required,
        helpGetter: (HelpTransformContext.() -> String)? = this.helpGetter,
        helpTags: Map<String, String> = this.helpTags,
        completionCandidates: CompletionCandidates? = explicitCompletionCandidates,
    ): ProcessedArgument<AllT, ValueT>
}

class ProcessedArgumentImpl<AllT, ValueT> internal constructor(
    override var name: String,
    override val nvalues: Int,
    override val required: Boolean,
    override val helpGetter: (HelpTransformContext.() -> String)?,
    override val helpTags: Map<String, String>,
    override val explicitCompletionCandidates: CompletionCandidates?,
    override val transformValue: ArgValueTransformer<ValueT>,
    override val transformAll: ArgCallsTransformer<AllT, ValueT>,
    override val transformValidator: ArgValidator<AllT>,
) : ProcessedArgument<AllT, ValueT> {
    init {
        require(nvalues != 0) { "Arguments cannot have nvalues == 0" }
    }

    override fun getArgumentHelp(context: Context): String {
        return helpGetter?.invoke(HelpTransformContext(context)) ?: ""
    }

    override var value: AllT by NullableLateinit("Cannot read from argument delegate before parsing command line")
    override val completionCandidates: CompletionCandidates
        get() = explicitCompletionCandidates ?: CompletionCandidates.None

    override fun parameterHelp(context: Context): ParameterHelp.Argument {
        return ParameterHelp.Argument(
            name = name,
            help = getArgumentHelp(context),
            required = required || nvalues > 1,
            repeatable = nvalues < 0,
            tags = helpTags
        )
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): AllT = value

    override operator fun provideDelegate(thisRef: CliktCommand, property: KProperty<*>):
            ReadOnlyProperty<CliktCommand, AllT> {
        if (name.isBlank()) name = property.name.uppercase().replace("-", "_")
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
    override fun <AllT, ValueT> copy(
        transformValue: ArgValueTransformer<ValueT>,
        transformAll: ArgCallsTransformer<AllT, ValueT>,
        validator: ArgValidator<AllT>,
        name: String,
        nvalues: Int,
        required: Boolean,
        helpGetter: (HelpTransformContext.() -> String)?,
        helpTags: Map<String, String>,
        completionCandidates: CompletionCandidates?,
    ): ProcessedArgument<AllT, ValueT> {
        return ProcessedArgumentImpl(
            name = name,
            nvalues = nvalues,
            required = required,
            helpGetter = helpGetter,
            helpTags = helpTags,
            explicitCompletionCandidates = completionCandidates,
            transformValue = transformValue,
            transformAll = transformAll,
            transformValidator = validator
        )
    }

    /** Create a new argument that is a copy of this one with the same transforms. */
    override fun copy(
        validator: ArgValidator<AllT>,
        name: String,
        nvalues: Int,
        required: Boolean,
        helpGetter: (HelpTransformContext.() -> String)?,
        helpTags: Map<String, String>,
        completionCandidates: CompletionCandidates?,
    ): ProcessedArgument<AllT, ValueT> {
        return ProcessedArgumentImpl(
            name = name,
            nvalues = nvalues,
            required = required,
            helpGetter = helpGetter,
            helpTags = helpTags,
            explicitCompletionCandidates = completionCandidates,
            transformValue = transformValue,
            transformAll = transformAll,
            transformValidator = validator
        )
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
@Suppress("UnusedReceiverParameter")
fun CliktCommand.argument(
    name: String = "",
    help: String = "",
    helpTags: Map<String, String> = emptyMap(),
    completionCandidates: CompletionCandidates? = null,
): RawArgument {
    return ProcessedArgumentImpl(
        name = name,
        nvalues = 1,
        required = true,
        helpGetter = { help },
        helpTags = helpTags,
        explicitCompletionCandidates = completionCandidates,
        transformValue = { it },
        transformAll = defaultAllProcessor(),
        transformValidator = defaultValidator()
    )
}

/**
 * Set the help for this argument.
 *
 * Although you can also pass the help string as an argument to [argument], this function
 * can be more convenient for long help strings.
 *
 * If you want to control the help string lazily or based on the context, you can pass a lambda that
 * returns a string.
 *
 * ### Example:
 *
 * ```
 * val number by argument()
 *      .int()
 *      .help("This is an argument that takes a number")
 * ```
 */
fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.help(
    help: String,
): ProcessedArgument<AllT, ValueT> {
    return help { help }
}

/**
 * Set the help for this argument lazily.
 *
 * You have access to the current Context if you need the theme or other information.
 *
 * ### Example:
 *
 * ```
 * val number by argument()
 *      .int()
 *      .help { theme.info("This is an argument that takes a number") }
 * ```
 */
fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.help(
    help: HelpTransformContext.() -> String,
): ProcessedArgument<AllT, ValueT> {
    return copy(helpGetter = help)
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
    defaultForHelp: String? = null,
    transform: ArgCallsTransformer<AllOutT, ValueT>,
): ProcessedArgument<AllOutT, ValueT> {
    val tags = this.helpTags.toMutableMap()
    if (defaultForHelp != null) tags[HelpFormatter.Tags.DEFAULT] = defaultForHelp
    else tags.remove(HelpFormatter.Tags.DEFAULT)
    return copy(
        transformValue, transform, defaultValidator(),
        nvalues = nvalues ?: this.nvalues,
        required = required ?: this.required, helpTags = tags
    )
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
 *
 * @param required If true, [default] is ignored and [MissingArgument] will be thrown if no
 *   instances of the argument are present on the command line.
 * @param default The value to use if the argument is not supplied. Defaults to an empty list.
 */
fun <T : Any> ProcessedArgument<T, T>.multiple(
    required: Boolean = false,
    default: List<T> = emptyList(),
): ProcessedArgument<List<T>, T> {
    return transformAll(nvalues = -1, required = required) { it.ifEmpty { default } }
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
 * You can customize how the default is shown to the user with [defaultForHelp].
 *
 * ### Example:
 *
 * ```
 * val arg: Pair<Int, Int> by argument().int().pair().default(1 to 2)
 * ```
 */
@JvmOverloads
fun <T : Any> ProcessedArgument<T, T>.default(
    value: T,
    defaultForHelp: String = value.toString(),
): ArgumentDelegate<T> {
    return transformAll(null, false, defaultForHelp) { it.firstOrNull() ?: value }
}

/**
 * If the argument is not given, call [value] and use its return value instead of throwing an error.
 *
 * This must be applied after all other transforms. If the argument is given on the command line, [value] will
 * not be called.
 *
 * You can customize how the default is shown to the user with [defaultForHelp]. The default value
 * is an empty string, so if you have the help formatter configured to show values, you should set
 * this value manually.
 *
 * ### Example:
 *
 * ```
 * val arg: Pair<Int, Int> by argument().int().pair().defaultLazy { expensiveOperation() }
 * ```
 */
inline fun <T : Any> ProcessedArgument<T, T>.defaultLazy(
    defaultForHelp: String = "",
    crossinline value: () -> T,
): ArgumentDelegate<T> {
    return transformAll(null, false, defaultForHelp) { it.firstOrNull() ?: value() }
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
    completionCandidates: CompletionCandidates? = explicitCompletionCandidates,
    crossinline conversion: ArgValueConverter<InT, ValueT>,
): ProcessedArgument<ValueT, ValueT> {
    val conv: ArgValueTransformer<ValueT> = {
        try {
            conversion(transformValue(it))
        } catch (err: UsageError) {
            err.paramName = err.paramName ?: argument.name
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return copy(
        conv, defaultAllProcessor(), defaultValidator(),
        completionCandidates = explicitCompletionCandidates ?: completionCandidates
    )
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
fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.validate(
    validator: ArgValidator<AllT & Any>,
): ArgumentDelegate<AllT> {
    return copy({ if (it != null) validator(it) })
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
inline fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.check(
    message: String,
    crossinline validator: (AllT & Any) -> Boolean,
): ArgumentDelegate<AllT> {
    return check({ message }, validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final argument type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [lazyMessage] that returns a message
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
inline fun <AllT, ValueT> ProcessedArgument<AllT, ValueT>.check(
    crossinline lazyMessage: (AllT & Any) -> String = { it.toString() },
    crossinline validator: (AllT & Any) -> Boolean,
): ArgumentDelegate<AllT> {
    return validate { require(validator(it)) { lazyMessage(it) } }
}
