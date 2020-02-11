package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parsers.OptionParser.Invocation
import com.github.ajalt.clikt.parsers.OptionWithValuesParser
import kotlin.js.JsName
import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A receiver for options transformers.
 *
 * @property name The name that was used to invoke this option.
 * @property option The option that was invoked
 */
class OptionCallTransformContext(
        val name: String,
        val option: Option,
        val context: Context
) : Option by option {
    /** Throw an exception indicating that an invalid value was provided. */
    fun fail(message: String): Nothing = throw BadParameterValue(message, name)

    /** Issue a message that can be shown to the user */
    fun message(message: String) = context.command.issueMessage(message)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    inline fun require(value: Boolean, lazyMessage: () -> String = { "invalid value" }) {
        if (!value) fail(lazyMessage())
    }
}

/**
 * A receiver for options transformers.
 *
 * @property option The option that was invoked
 */
class OptionTransformContext(val option: Option, val context: Context) : Option by option {
    /** Throw an exception indicating that usage was incorrect. */
    fun fail(message: String): Nothing = throw UsageError(message, option)

    /** Issue a message that can be shown to the user */
    fun message(message: String) = context.command.issueMessage(message)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    inline fun require(value: Boolean, lazyMessage: () -> String = { "invalid value" }) {
        if (!value) fail(lazyMessage())
    }
}

/** A callback that transforms a single value from a string to the value type */
typealias ValueTransformer<ValueT> = OptionCallTransformContext.(String) -> ValueT

/**
 * A callback that transforms all the values for a call to the call type.
 *
 * The input list will always have a size equal to `nvalues`
 */
typealias ArgsTransformer<ValueT, EachT> = OptionCallTransformContext.(List<ValueT>) -> EachT

/**
 * A callback that transforms all of the calls to the final option type.
 *
 * The input list will have a size equal to the number of times the option appears on the command line.
 */
typealias CallsTransformer<EachT, AllT> = OptionTransformContext.(List<EachT>) -> AllT

/** A callback validates the final option type */
typealias OptionValidator<AllT> = OptionTransformContext.(AllT) -> Unit

/**
 * An [Option] that takes one or more values.
 *
 * @property metavarWithDefault The metavar to use. Specified at option creation.
 * @property envvar The environment variable name to use.
 * @property envvarSplit The pattern to split envvar values on. If the envvar splits into multiple values,
 *   each one will be treated like a separate invocation of the option.
 * @property valueSplit The pattern to split values from the command line on. By default, values are
 *   split on whitespace.
 * @property transformValue Called in [finalize] to transform each value provided to each invocation.
 * @property transformEach Called in [finalize] to transform each invocation.
 * @property transformAll Called in [finalize] to transform all invocations into the final value.
 * @property transformValidator Called after all parameters have been [finalize]d to validate the output of [transformAll]
 */
// `AllT` is deliberately not an out parameter. If it was, it would allow undesirable combinations such as
// default("").int()
class OptionWithValues<AllT, EachT, ValueT>(
        names: Set<String>,
        val metavarWithDefault: ValueWithDefault<String?>,
        override val nvalues: Int,
        override val help: String,
        override val hidden: Boolean,
        override val helpTags: Map<String, String>,
        val envvar: String?,
        val envvarSplit: ValueWithDefault<Regex>,
        val valueSplit: Regex?,
        override val parser: OptionWithValuesParser,
        val completionCandidatesWithDefault: ValueWithDefault<CompletionCandidates>,
        val transformValue: ValueTransformer<ValueT>,
        val transformEach: ArgsTransformer<ValueT, EachT>,
        val transformAll: CallsTransformer<EachT, AllT>,
        val transformValidator: OptionValidator<AllT>
) : OptionDelegate<AllT>, GroupableOption {
    override var parameterGroup: ParameterGroup? = null
    override var groupName: String? = null
    override val metavar: String? get() = metavarWithDefault.value
    override var value: AllT by NullableLateinit("Cannot read from option delegate before parsing command line")
        private set
    override val secondaryNames: Set<String> get() = emptySet()
    override var names: Set<String> = names
        private set
    override val completionCandidates: CompletionCandidates
        get() = completionCandidatesWithDefault.value

    override fun finalize(context: Context, invocations: List<Invocation>) {
        val env = inferEnvvar(names, envvar, context.autoEnvvarPrefix)
        val inv = if (invocations.isNotEmpty() || env == null || readEnvvar(env) == null) {
            when (valueSplit) {
                null -> invocations
                else -> invocations.map { inv -> inv.copy(values = inv.values.flatMap { it.split(valueSplit) }) }
            }
        } else {
            readEnvvar(env)?.split(envvarSplit.value)?.map { Invocation(env, listOf(it)) } ?: emptyList()
        }

        value = transformAll(OptionTransformContext(this, context), inv.map {
            val tc = OptionCallTransformContext(it.name, this, context)
            transformEach(tc, it.values.map { v -> transformValue(tc, v) })
        })
    }

    override operator fun provideDelegate(thisRef: ParameterHolder, prop: KProperty<*>): ReadOnlyProperty<ParameterHolder, AllT> {
        require(secondaryNames.isEmpty()) {
            "Secondary option names are only allowed on flag options."
        }
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }

    override fun postValidate(context: Context) {
        transformValidator(OptionTransformContext(this, context), value)
    }

    /** Create a new option that is a copy of this one with different transforms. */
    fun <AllT, EachT, ValueT> copy(
            transformValue: ValueTransformer<ValueT>,
            transformEach: ArgsTransformer<ValueT, EachT>,
            transformAll: CallsTransformer<EachT, AllT>,
            validator: OptionValidator<AllT>,
            names: Set<String> = this.names,
            metavarWithDefault: ValueWithDefault<String?> = this.metavarWithDefault,
            nvalues: Int = this.nvalues,
            help: String = this.help,
            hidden: Boolean = this.hidden,
            helpTags: Map<String, String> = this.helpTags,
            envvar: String? = this.envvar,
            envvarSplit: ValueWithDefault<Regex> = this.envvarSplit,
            valueSplit: Regex? = this.valueSplit,
            parser: OptionWithValuesParser = this.parser,
            completionCandidatesWithDefault: ValueWithDefault<CompletionCandidates> = this.completionCandidatesWithDefault
    ): OptionWithValues<AllT, EachT, ValueT> {
        return OptionWithValues(names, metavarWithDefault, nvalues, help, hidden,
                helpTags, envvar, envvarSplit, valueSplit, parser, completionCandidatesWithDefault,
                transformValue, transformEach, transformAll, validator)
    }

    /** Create a new option that is a copy of this one with the same transforms. */
    fun copy(
            validator: OptionValidator<AllT> = this.transformValidator,
            names: Set<String> = this.names,
            metavarWithDefault: ValueWithDefault<String?> = this.metavarWithDefault,
            nvalues: Int = this.nvalues,
            help: String = this.help,
            hidden: Boolean = this.hidden,
            helpTags: Map<String, String> = this.helpTags,
            envvar: String? = this.envvar,
            envvarSplit: ValueWithDefault<Regex> = this.envvarSplit,
            valueSplit: Regex? = this.valueSplit,
            parser: OptionWithValuesParser = this.parser,
            completionCandidatesWithDefault: ValueWithDefault<CompletionCandidates> = this.completionCandidatesWithDefault
    ): OptionWithValues<AllT, EachT, ValueT> {
        return OptionWithValues(names, metavarWithDefault, nvalues, help, hidden,
                helpTags, envvar, envvarSplit, valueSplit, parser, completionCandidatesWithDefault,
                transformValue, transformEach, transformAll, validator)
    }
}

typealias NullableOption<EachT, ValueT> = OptionWithValues<EachT?, EachT, ValueT>
typealias RawOption = NullableOption<String, String>

@PublishedApi
internal fun <T : Any> defaultEachProcessor(): ArgsTransformer<T, T> = { it.single() }

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): CallsTransformer<T, T?> = { it.lastOrNull() }

@PublishedApi
internal fun <T> defaultValidator(): OptionValidator<T> = { }

/**
 * Create a property delegate option.
 *
 * By default, the property will return null if the option does not appear on the command line. If the option
 * is invoked multiple times, the value from the last invocation will be used The option can be modified with
 * functions like [int], [pair], and [multiple].
 *
 * @param names The names that can be used to invoke this option. They must start with a punctuation character.
 *   If not given, a name is inferred from the property name.
 * @param help The description of this option, usually a single line.
 * @param metavar A name representing the values for this option that can be displayed to the user.
 *   Automatically inferred from the type.
 * @param hidden Hide this option from help outputs.
 * @param envvar The environment variable that will be used for the value if one is not given on the command
 *   line.
 * @param envvarSplit The pattern to split the value of the [envvar] on. Defaults to whitespace,
 *   although some conversions like `file` change the default.
 * @param helpTags Extra information about this option to pass to the help formatter
 */
@Suppress("unused")
fun ParameterHolder.option(
        vararg names: String,
        help: String = "",
        metavar: String? = null,
        hidden: Boolean = false,
        envvar: String? = null,
        envvarSplit: Regex? = null,
        helpTags: Map<String, String> = emptyMap(),
        completionCandidates: CompletionCandidates? = null
): RawOption = OptionWithValues(
        names = names.toSet(),
        metavarWithDefault = ValueWithDefault(metavar, "TEXT"),
        nvalues = 1,
        help = help,
        hidden = hidden,
        helpTags = helpTags,
        envvar = envvar,
        envvarSplit = ValueWithDefault(envvarSplit, Regex("\\s+")),
        valueSplit = null,
        parser = OptionWithValuesParser,
        completionCandidatesWithDefault = ValueWithDefault(completionCandidates, CompletionCandidates.None),
        transformValue = { it },
        transformEach = defaultEachProcessor(),
        transformAll = defaultAllProcessor(),
        transformValidator = defaultValidator()
)

/**
 * Transform all calls to the option to the final option type.
 *
 * The input is a list of calls, one for each time the option appears on the command line. The values in the
 * list are the output of calls to [transformValues]. If the option does not appear from any source (command
 * line or envvar), this will be called with an empty list.
 *
 * Used to implement functions like [default] and [multiple].
 *
 * @param defaultForHelp The help text for this option's default value if the help formatter is
 *   configured to show them, or null if this option has no default or the default value should not be
 *   shown.This does not affect behavior outside of help formatting.
 * @param showAsRequired Tell the help formatter that this option should be marked as required. This
 *   does not affect behavior outside of help formatting.
 */
fun <AllT, EachT : Any, ValueT> NullableOption<EachT, ValueT>.transformAll(
        defaultForHelp: String? = this.helpTags[HelpFormatter.Tags.DEFAULT],
        showAsRequired: Boolean = HelpFormatter.Tags.REQUIRED in this.helpTags,
        transform: CallsTransformer<EachT, AllT>
): OptionWithValues<AllT, EachT, ValueT> {
    val tags = this.helpTags.toMutableMap()

    if (showAsRequired) tags[HelpFormatter.Tags.REQUIRED] = ""
    else tags.remove(HelpFormatter.Tags.REQUIRED)

    if (defaultForHelp != null) tags[HelpFormatter.Tags.DEFAULT] = defaultForHelp
    else tags.remove(HelpFormatter.Tags.DEFAULT)

    return copy(transformValue, transformEach, transform, defaultValidator(), helpTags = tags)
}

/**
 * If the option is not called on the command line (and is not set in an envvar), use [value] for the option.
 *
 * This must be applied after all other transforms.
 *
 * You can customize how the default is shown to the user with [defaultForHelp].
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: Pair<Int, Int> by option().int().pair().default(1 to 2)
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.default(value: EachT, defaultForHelp: String = value.toString())
        : OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(defaultForHelp) { it.lastOrNull() ?: value }
}

/**
 * If the option is not called on the command line (and is not set in an envvar), call the [value] and use its
 * return value for the option.
 *
 * This must be applied after all other transforms. If the option is given on the command line, [value] will
 * not be called.
 *
 * You can customize how the default is shown to the user with [defaultForHelp]. The default value
 * is an empty string, so if you have the help formatter configured to show values, you should set
 * this value manually.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: Pair<Int, Int> by option().int().pair().defaultLazy { expensiveOperation() }
 * ```
 */
inline fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.defaultLazy(
        defaultForHelp: String = "",
        crossinline value: () -> EachT
): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(defaultForHelp) { it.lastOrNull() ?: value() }
}

/**
 * If the option is not called on the command line (and is not set in an envvar), throw a [MissingParameter].
 *
 * This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: Pair<Int, Int> by option().int().pair().required()
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.required(): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(showAsRequired = true) { it.lastOrNull() ?: throw MissingParameter(option) }
}

/**
 * Make the option return a list of calls; each item in the list is the value of one call.
 *
 * If the option is never called, the list will be empty. This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: List<Pair<Int, Int>> by option().int().pair().multiple()
 * ```
 *
 * @param default The value to use if the option is not supplied. Defaults to an empty list.
 * @param required If true, [default] is ignored and [MissingParameter] will be thrown if no
 *   instances of the option are present on the command line.
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.multiple(
        default: List<EachT> = emptyList(),
        required: Boolean = false
): OptionWithValues<List<EachT>, EachT, ValueT> {
    return transformAll {
        when {
            it.isEmpty() && required -> throw MissingParameter(option)
            it.isEmpty() && !required -> default
            else -> it
        }
    }
}

/**
 * Make the [multiple] option return a unique set of calls
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: Set<Int> by option().int().multiple().unique()
 * ```
 */
fun <EachT : Any, ValueT> OptionWithValues<List<EachT>, EachT, ValueT>.unique(): OptionWithValues<Set<EachT>, EachT, ValueT> {
    return copy(transformValue, transformEach, { transformAll(it).toSet() }, defaultValidator())
}

/**
 * Change the number of values that this option takes.
 *
 * The input will be a list of size [nvalues], with each item in the list being the output of a call to
 * [convert]. [nvalues] must be 2 or greater, since options cannot take a variable number of values, and
 * [option] has [nvalues] = 1 by default. If you want to change the type of an option with one value, use
 * [convert] instead.
 *
 * Used to implement functions like [pair] and [triple]. This must be applied before any other transforms.
 */
fun <EachInT : Any, EachOutT : Any, ValueT> NullableOption<EachInT, ValueT>.transformValues(
        nvalues: Int,
        transform: ArgsTransformer<ValueT, EachOutT>
): NullableOption<EachOutT, ValueT> {
    require(nvalues != 0) { "Cannot set nvalues = 0. Use flag() instead." }
    require(nvalues > 0) { "Options cannot have nvalues < 0" }
    require(nvalues > 1) { "Cannot set nvalues = 1. Use convert() instead." }
    return copy(transformValue, transform, defaultAllProcessor(), defaultValidator(), nvalues = nvalues)
}

/**
 * Change to option to take two values, held in a [Pair].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: Pair<Int, Int>? by option().int().pair()
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.pair()
        : NullableOption<Pair<ValueT, ValueT>, ValueT> {
    return transformValues(nvalues = 2) { it[0] to it[1] }
}

/**
 * Change to option to take three values, held in a [Triple].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: Triple<Int, Int, Int>? by option().int().triple()
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.triple()
        : NullableOption<Triple<ValueT, ValueT, ValueT>, ValueT> {
    return transformValues(nvalues = 3) { Triple(it[0], it[1], it[2]) }
}

/**
 * Change to option to take any number of values, separated by a [regex].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: List<Int>? by option().int().split(Regex(","))
 * ```
 *
 * Which can be called like this:
 *
 * ```
 * ./program --opt 1,2,3
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.split(regex: Regex)
        : OptionWithValues<List<ValueT>?, List<ValueT>, ValueT> {
    return copy(
            transformValue = transformValue,
            transformEach = { it },
            transformAll = defaultAllProcessor(),
            validator = defaultValidator(),
            nvalues = 1,
            valueSplit = regex
    )
}

/**
 * Change to option to take any number of values, separated by a string [delimiter].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt: List<Int>? by option().int().split(Regex(","))
 * ```
 *
 * Which can be called like this:
 *
 * ```
 * ./program --opt 1,2,3
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.split(delimiter: String)
        : OptionWithValues<List<ValueT>?, List<ValueT>, ValueT> {
    return split(Regex.fromLiteral(delimiter))
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should call `fail`
 * if the value is not valid. It is not called if the delegate value is null.
 *
 * You can also call `require` to fail automatically if an expression is false.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.validate(
        validator: OptionValidator<AllT>
): OptionDelegate<AllT> {
    return copy(transformValue, transformEach, transformAll, validator)
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should call `fail`
 * if the value is not valid. It is not called if the delegate value is null.
 *
 * You can also call `require` to fail automatically if an expression is false, or `warn` to show
 * the user a warning message without aborting.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
@JvmName("nullableValidate")
@JsName("nullableValidate")
fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT?, EachT, ValueT>.validate(
        validator: OptionValidator<AllT>
): OptionDelegate<AllT?> {
    return copy(transformValue, transformEach, transformAll, { if (it != null) validator(it) })
}

/**
 * Mark this option as deprecated in the help output.
 *
 * By default, a tag is added to the help message and a warning is printed if the option is used.
 *
 * This should be called after any conversion and validation.
 *
 * ### Example:
 *
 * ```kotlin
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 *    .deprecated("WARNING: --opt is deprecated, use --new-opt instead")
 * ```
 *
 * @param message The message to show in the warning or error. If null, no warning is issued.
 * @param tagName The tag to add to the help message
 * @param tagValue An extra message to add to the tag
 * @param error If true, when the option is invoked, a [CliktError] is raised immediately instead of issuing a warning.
 */
fun <AllT, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.deprecated(
        message: String? = "",
        tagName: String? = "deprecated",
        tagValue: String = "",
        error: Boolean = false
): OptionDelegate<AllT> {
    val helpTags = if (tagName.isNullOrBlank()) helpTags else helpTags + mapOf(tagName to tagValue)
    return copy(transformValue, transformEach, deprecationTransformer(message, error, transformAll), transformValidator, helpTags = helpTags)
}

/**
 * Convert the option's value type.
 *
 * The [conversion] is called once for each value in each invocation of the option. If any errors are thrown,
 * they are caught and a [BadParameterValue] is thrown with the error message. You can call `fail` to throw a
 * [BadParameterValue] manually.
 *
 * @param metavar The metavar for the type. Overridden by a metavar passed to [option].
 * @param envvarSplit If the value is read from an envvar, the pattern to split the value on. The default
 *   splits on whitespace. This value is can be overridden by passing a value to the [option] function.
 * @param completionCandidates candidates to use when completing this option in shell autocomplete,
 *   if no candidates are specified in [option]
 */
inline fun <T : Any> RawOption.convert(
        metavar: String = "VALUE",
        envvarSplit: Regex = this.envvarSplit.default,
        completionCandidates: CompletionCandidates = completionCandidatesWithDefault.default,
        crossinline conversion: ValueTransformer<T>
): NullableOption<T, T> {
    val proc: ValueTransformer<T> = {
        try {
            conversion(it)
        } catch (err: UsageError) {
            err.paramName = name
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return copy(proc, defaultEachProcessor(), defaultAllProcessor(), defaultValidator(),
            metavarWithDefault = metavarWithDefault.copy(default = metavar),
            envvarSplit = this.envvarSplit.copy(default = envvarSplit),
            completionCandidatesWithDefault = completionCandidatesWithDefault.copy(default = completionCandidates))
}

@Deprecated(
        "Cannot wrap an option that isn't converted",
        replaceWith = ReplaceWith("this.convert(wrapper)"),
        level = DeprecationLevel.ERROR
)
@JvmName("rawWrapValue")
@JsName("rawWrapValue")
@Suppress("UNUSED_PARAMETER")
fun RawOption.wrapValue(wrapper: (String) -> Any): RawOption = this

/**
 * Wrap the option's values after a conversion is applied.
 *
 * This can be useful if you want to use different option types wrapped in a sealed class for
 * [mutuallyExclusiveOptions].
 *
 * This can only be called on an option after [convert] or a conversion function like [int].
 *
 * If you just want to perform checks on the value without converting it to another type, use
 * [validate] instead.
 *
 * ## Example
 *
 * ```
 * sealed class GroupTypes {
 *   data class FileType(val file: File) : GroupTypes()
 *   data class StringType(val string: String) : GroupTypes()
 * }
 *
 * val group by mutuallyExclusiveOptions<GroupTypes>(
 *   option("-f").file().wrapValue(::FileType),
 *   option("-s").convert { StringType(it) }
 * )
 * ```
 */
inline fun <T1 : Any, T2 : Any> NullableOption<T1, T1>.wrapValue(
        crossinline wrapper: (T1) -> T2
): NullableOption<T2, T2> {
    val proc: ValueTransformer<T2> = {
        try {
            wrapper(transformValue(it))
        } catch (err: UsageError) {
            err.paramName = name
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return copy(proc, defaultEachProcessor(), defaultAllProcessor(), defaultValidator())
}

/**
 * If the option isn't given on the command line, prompt the user for manual input.
 *
 * @param text The text to prompt the user with
 * @param default The default value to use if no input is given. If null, the prompt will be repeated until
 *   input is given.
 * @param hideInput If true, user input will not be shown on the screen. Useful for passwords and sensitive
 *   input.
 * @param requireConfirmation If true, the user will be required to enter the same value twice before it is
 *   accepted.
 * @param confirmationPrompt If [requireConfirmation] is true, this will be used to ask for input again.
 * @param promptSuffix Text to display directly after [text]. Defaults to ": ".
 * @param showDefault Show [default] to the user in the prompt.
 */
fun <T : Any> NullableOption<T, T>.prompt(
        text: String? = null,
        default: String? = null,
        hideInput: Boolean = false,
        requireConfirmation: Boolean = false,
        confirmationPrompt: String = "Repeat for confirmation: ",
        promptSuffix: String = ": ",
        showDefault: Boolean = true): OptionWithValues<T, T, T> = transformAll {
    val promptText = text ?: longestName()?.let { splitOptionPrefix(it).second }
            ?.replace(Regex("\\W"), " ")?.capitalize() ?: "Value"

    when (val provided = it.lastOrNull()) {
        null -> TermUi.prompt(promptText, default, hideInput, requireConfirmation,
                confirmationPrompt, promptSuffix, showDefault, context.console) {
            val ctx = OptionCallTransformContext("", this, context)
            transformAll(listOf(transformEach(ctx, listOf(transformValue(ctx, it)))))
        }
        else -> provided
    } ?: throw Abort()
}
