@file:JvmMultifileClass
@file:JvmName("OptionWithValuesKt")

package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parameters.transform.HelpTransformContext
import com.github.ajalt.clikt.parameters.transform.TransformContext
import com.github.ajalt.clikt.parameters.transform.message
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parsers.Invocation
import com.github.ajalt.clikt.sources.ValueSource
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** A receiver for options transformers. */
class OptionCallTransformContext(
    /** The name that was used to invoke this option. */
    val name: String,
    /** The option that was invoked */
    val option: Option,
    /** The current command context */
    override val context: Context,
) : Option by option, TransformContext {
    override fun fail(message: String): Nothing = throw BadParameterValue(message, option, name)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    inline fun require(value: Boolean, lazyMessage: () -> String = { "" }) {
        if (!value) fail(lazyMessage())
    }
}

/** A receiver for options transformers. */
class OptionTransformContext(
    /** The option that was invoked */
    val option: Option,
    /** The current command context */
    override val context: Context,
) : Option by option, TransformContext {
    /** The terminal from the current context */
    val terminal: Terminal get() = context.terminal

    /** Throw an exception indicating that usage was incorrect. */
    override fun fail(message: String): Nothing = throw BadParameterValue(message, option)

    /** If [value] is false, call [fail] with the output of [lazyMessage] */
    inline fun require(value: Boolean, lazyMessage: () -> String = { "" }) {
        if (!value) fail(lazyMessage())
    }
}

/** A callback that transforms a single value from a string to the value type */
typealias ValueTransformer<ValueT> = ValueConverter<String, ValueT>

/** A block that converts a single value from one type to another */
typealias ValueConverter<InT, ValueT> = OptionCallTransformContext.(InT) -> ValueT

/**
 * A callback that transforms all values for each call after the individual values have been converted.
 *
 * The input list will always have a size withing the range of `nvalues`
 */
typealias ValuesTransformer<ValueT, EachT> = OptionCallTransformContext.(List<ValueT>) -> EachT

/**
 * A callback that transforms all the calls to the final option type.
 *
 * The input list will have a size equal to the number of times the option appears on the command line.
 */
typealias AllTransformer<EachT, AllT> = OptionTransformContext.(List<EachT>) -> AllT

/** A callback validates the final option type */
typealias OptionValidator<AllT> = OptionTransformContext.(AllT) -> Unit


/**
 * An [Option] that takes one or more values.
 */
// `AllT` is deliberately not an out parameter. If it was, it would allow undesirable combinations such as
// default("").int()
interface OptionWithValues<AllT, EachT, ValueT> : OptionDelegate<AllT> {
    /** The environment variable name to use. */
    val envvar: String?

    /** Called in [finalize] to transform each value provided to each invocation. */
    val transformValue: ValueTransformer<ValueT>

    /** Called in [finalize] to transform each invocation. */
    val transformEach: ValuesTransformer<ValueT, EachT>

    /** Called in [finalize] to transform all invocations into the final value. */
    val transformAll: AllTransformer<EachT, AllT>

    /** Called after all parameters have been [finalized][finalize] to validate the output of [transformAll] */
    val transformValidator: OptionValidator<AllT>

    /** The completion candidates set on this option, or `null` if no candidates have been set */
    val explicitCompletionCandidates: CompletionCandidates?

    /** A block that will return the metavar for this option, or `null` if no getter has been specified */
    val metavarGetter: (Context.() -> String?)?

    /** A block that will return the help text for this option, or `null` if no getter has been specified */
    val helpGetter: (HelpTransformContext.() -> String)?

    /** A regex to split option values on before conversion, or `null` to leave them unsplit */
    val valueSplit: Regex?

    /** Create a new option that is a copy of this one with different transforms. */
    fun <AllT, EachT, ValueT> copy(
        transformValue: ValueTransformer<ValueT>,
        transformEach: ValuesTransformer<ValueT, EachT>,
        transformAll: AllTransformer<EachT, AllT>,
        validator: OptionValidator<AllT>,
        names: Set<String> = this.names,
        metavarGetter: (Context.() -> String?)? = this.metavarGetter,
        nvalues: IntRange = this.nvalues,
        helpGetter: (HelpTransformContext.() -> String)? = this.helpGetter,
        hidden: Boolean = this.hidden,
        helpTags: Map<String, String> = this.helpTags,
        valueSourceKey: String? = this.valueSourceKey,
        envvar: String? = this.envvar,
        valueSplit: Regex? = this.valueSplit,
        completionCandidates: CompletionCandidates? = explicitCompletionCandidates,
        secondaryNames: Set<String> = this.secondaryNames,
        acceptsNumberValueWithoutName: Boolean = this.acceptsNumberValueWithoutName,
        acceptsUnattachedValue: Boolean = this.acceptsUnattachedValue,
        eager: Boolean = this.eager,
    ): OptionWithValues<AllT, EachT, ValueT>

    /** Create a new option that is a copy of this one with the same transforms. */
    fun copy(
        validator: OptionValidator<AllT> = this.transformValidator,
        names: Set<String> = this.names,
        metavarGetter: (Context.() -> String?)? = this.metavarGetter,
        nvalues: IntRange = this.nvalues,
        helpGetter: (HelpTransformContext.() -> String)? = this.helpGetter,
        hidden: Boolean = this.hidden,
        helpTags: Map<String, String> = this.helpTags,
        envvar: String? = this.envvar,
        valueSourceKey: String? = this.valueSourceKey,
        valueSplit: Regex? = this.valueSplit,
        completionCandidates: CompletionCandidates? = explicitCompletionCandidates,
        secondaryNames: Set<String> = this.secondaryNames,
        acceptsNumberValueWithoutName: Boolean = this.acceptsNumberValueWithoutName,
        acceptsUnattachedValue: Boolean = this.acceptsUnattachedValue,
        eager: Boolean = this.eager,
    ): OptionWithValues<AllT, EachT, ValueT>
}


private class OptionWithValuesImpl<AllT, EachT, ValueT>(
    names: Set<String>,
    override val metavarGetter: (Context.() -> String?)?,
    override val nvalues: IntRange,
    override val helpGetter: (HelpTransformContext.() -> String)?,
    override val hidden: Boolean,
    override val helpTags: Map<String, String>,
    override val valueSourceKey: String?,
    override val envvar: String?,
    override val valueSplit: Regex?,
    override val explicitCompletionCandidates: CompletionCandidates?,
    override val secondaryNames: Set<String>,
    override val acceptsNumberValueWithoutName: Boolean,
    override val acceptsUnattachedValue: Boolean,
    override val eager: Boolean,
    override val transformValue: ValueTransformer<ValueT>,
    override val transformEach: ValuesTransformer<ValueT, EachT>,
    override val transformAll: AllTransformer<EachT, AllT>,
    override val transformValidator: OptionValidator<AllT>,
) : OptionWithValues<AllT, EachT, ValueT> {
    override var parameterGroup: ParameterGroup? = null
    override var groupName: String? = null
    override fun metavar(context: Context) =
        (metavarGetter ?: { localization.stringMetavar() }).invoke(context)

    override var value: AllT by NullableLateinit("Cannot read from option delegate before parsing command line")
    override var names: Set<String> = names
        private set
    override val completionCandidates: CompletionCandidates
        get() = explicitCompletionCandidates ?: CompletionCandidates.None

    override fun optionHelp(context: Context): String {
        return helpGetter?.invoke(HelpTransformContext(context)) ?: ""
    }

    override fun finalize(context: Context, invocations: List<Invocation>) {
        val invs = when (val v = getFinalValue(context, invocations, envvar)) {
            is FinalValue.Parsed -> {
                when (valueSplit) {
                    null -> {
                        invocations.find { it.values.size !in nvalues }?.let {
                            throw IncorrectOptionValueCount(this, it.name)
                        }
                        invocations
                    }
                    else -> invocations.map { inv ->
                        inv.copy(values = inv.values.flatMap { it.split(valueSplit) })
                    }
                }
            }

            is FinalValue.Sourced -> {
                v.values.map { Invocation("", it.values) }
            }

            is FinalValue.Envvar -> {
                when (valueSplit) {
                    null -> listOf(Invocation(v.key, listOf(v.value)))
                    else -> listOf(Invocation(v.key, v.value.split(valueSplit)))
                }
            }
        }

        value = transformAll(OptionTransformContext(this, context), invs.map {
            val tc = OptionCallTransformContext(it.name, this, context)
            transformEach(tc, it.values.map { v -> transformValue(tc, v) })
        })
    }

    override operator fun provideDelegate(
        thisRef: ParameterHolder,
        property: KProperty<*>,
    ): ReadOnlyProperty<ParameterHolder, AllT> {
        names = inferOptionNames(names, property.name)
        thisRef.registerOption(this)
        return this
    }

    override fun postValidate(context: Context) {
        transformValidator(OptionTransformContext(this, context), value)
    }

    /** Create a new option that is a copy of this one with different transforms. */
    override fun <AllT, EachT, ValueT> copy(
        transformValue: ValueTransformer<ValueT>,
        transformEach: ValuesTransformer<ValueT, EachT>,
        transformAll: AllTransformer<EachT, AllT>,
        validator: OptionValidator<AllT>,
        names: Set<String>,
        metavarGetter: (Context.() -> String?)?,
        nvalues: IntRange,
        helpGetter: (HelpTransformContext.() -> String)?,
        hidden: Boolean,
        helpTags: Map<String, String>,
        valueSourceKey: String?,
        envvar: String?,
        valueSplit: Regex?,
        completionCandidates: CompletionCandidates?,
        secondaryNames: Set<String>,
        acceptsNumberValueWithoutName: Boolean,
        acceptsUnattachedValue: Boolean,
        eager: Boolean,
    ): OptionWithValues<AllT, EachT, ValueT> {
        return OptionWithValuesImpl(
            names = names,
            metavarGetter = metavarGetter,
            nvalues = nvalues,
            helpGetter = helpGetter,
            hidden = hidden,
            helpTags = helpTags,
            valueSourceKey = valueSourceKey,
            envvar = envvar,
            valueSplit = valueSplit,
            explicitCompletionCandidates = completionCandidates,
            secondaryNames = secondaryNames,
            acceptsNumberValueWithoutName = acceptsNumberValueWithoutName,
            acceptsUnattachedValue = acceptsUnattachedValue,
            eager = eager,
            transformValue = transformValue,
            transformEach = transformEach,
            transformAll = transformAll,
            transformValidator = validator
        )
    }

    /** Create a new option that is a copy of this one with the same transforms. */
    override fun copy(
        validator: OptionValidator<AllT>,
        names: Set<String>,
        metavarGetter: (Context.() -> String?)?,
        nvalues: IntRange,
        helpGetter: (HelpTransformContext.() -> String)?,
        hidden: Boolean,
        helpTags: Map<String, String>,
        envvar: String?,
        valueSourceKey: String?,
        valueSplit: Regex?,
        completionCandidates: CompletionCandidates?,
        secondaryNames: Set<String>,
        acceptsNumberValueWithoutName: Boolean,
        acceptsUnattachedValue: Boolean,
        eager: Boolean,
    ): OptionWithValues<AllT, EachT, ValueT> {
        return OptionWithValuesImpl(
            names = names,
            metavarGetter = metavarGetter,
            nvalues = nvalues,
            helpGetter = helpGetter,
            hidden = hidden,
            helpTags = helpTags,
            valueSourceKey = valueSourceKey,
            envvar = envvar,
            valueSplit = valueSplit,
            explicitCompletionCandidates = completionCandidates,
            secondaryNames = secondaryNames,
            acceptsNumberValueWithoutName = acceptsNumberValueWithoutName,
            acceptsUnattachedValue = acceptsUnattachedValue,
            eager = eager,
            transformValue = transformValue,
            transformEach = transformEach,
            transformAll = transformAll,
            transformValidator = validator
        )
    }

    override fun toString(): String = buildString {
        append(longestName())
        runCatching { value }.onSuccess { append("=").append(it) }
    }
}

typealias NullableOption<EachT, ValueT> = OptionWithValues<EachT?, EachT, ValueT>
typealias RawOption = NullableOption<String, String>

@PublishedApi
internal fun <T> defaultEachProcessor(): ValuesTransformer<T, T> = { it.single() }

@PublishedApi
internal fun <T> defaultAllProcessor(): AllTransformer<T, T?> = { it.lastOrNull() }

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
 * @param helpTags Extra information about this option to pass to the help formatter
 * @param completionCandidates The values to use for tab completion.
 * @param valueSourceKey The key to use when reading this option from a [ValueSource].
 * @param eager If true, this option will be parsed before other options. This is useful for options
 *   like `--version` that will stop parsing immediately by throwing a [PrintHelpMessage] or
 *   [ProgramResult] exception in their [validate] callback. If your option is a flag, you can use
 *   [eagerOption] instead.
 */
@Suppress("UnusedReceiverParameter")
fun ParameterHolder.option(
    vararg names: String,
    help: String = "",
    metavar: String? = null,
    hidden: Boolean = false,
    envvar: String? = null,
    helpTags: Map<String, String> = emptyMap(),
    completionCandidates: CompletionCandidates? = null,
    valueSourceKey: String? = null,
    eager: Boolean = false,
): RawOption = OptionWithValuesImpl(
    names = names.toSet(),
    metavarGetter = metavar?.let { { it } },
    nvalues = 1..1,
    helpGetter = { help },
    hidden = hidden,
    helpTags = helpTags,
    valueSourceKey = valueSourceKey,
    envvar = envvar,
    valueSplit = null,
    explicitCompletionCandidates = completionCandidates,
    secondaryNames = emptySet(),
    acceptsNumberValueWithoutName = false,
    acceptsUnattachedValue = true,
    transformValue = { it },
    eager = eager,
    transformEach = defaultEachProcessor(),
    transformAll = defaultAllProcessor(),
    transformValidator = defaultValidator()
)

/**
 * Set the help for this option.
 *
 * Although you can also pass the help string as an argument to [option], this function
 * can be more convenient for long help strings.
 *
 * If you want to control the help string lazily or based on the context, you can pass a lambda that
 * returns a string.
 *
 * ### Example:
 *
 * ```
 * val number by option()
 *      .int()
 *      .help("This is an option that takes a number")
 * ```
 */
fun <AllT, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.help(
    help: String,
): OptionWithValues<AllT, EachT, ValueT> {
    return help { help }
}

/**
 * Set the help for this option lazily.
 *
 * You have access to the current Context if you need the theme or other information.
 *
 * ### Example:
 *
 * ```
 * val number by option()
 *      .int()
 *      .help { theme.info("This is an option that takes a number") }
 * ```
 */
fun <AllT, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.help(
    help: HelpTransformContext.() -> String,
): OptionWithValues<AllT, EachT, ValueT> {
    return copy(helpGetter = help)
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
 * ```
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
    error: Boolean = false,
): OptionDelegate<AllT> {
    val helpTags = if (tagName.isNullOrBlank()) helpTags else helpTags + mapOf(tagName to tagValue)
    val deprecationTransform: OptionTransformContext.(List<EachT>) -> AllT = {
        if (it.isNotEmpty()) {
            val msg = when (message) {
                null -> ""
                "" -> "${if (error) "ERROR" else "WARNING"}: option ${option.longestName()} is deprecated"
                else -> message
            }
            if (error) {
                throw CliktError(msg)
            } else if (message != null) {
                message(msg)
            }
        }
        transformAll(it)
    }
    return copy(
        transformValue,
        transformEach,
        deprecationTransform,
        transformValidator,
        helpTags = helpTags
    )
}
