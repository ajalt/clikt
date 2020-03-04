package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parameters.types.valueToInt
import com.github.ajalt.clikt.parsers.FlagOptionParser
import com.github.ajalt.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * An [Option] that has no values.
 *
 * @property envvar The name of the environment variable for this option. Overrides automatic names.
 * @property transformEnvvar Called to transform string values from envvars and value sources into the option type.
 * @property transformAll Called to transform all invocations of this option into the final option type.
 */
// `T` is deliberately not an out parameter.
class FlagOption<T>(
        names: Set<String>,
        override val secondaryNames: Set<String>,
        override val help: String,
        override val hidden: Boolean,
        override val helpTags: Map<String, String>,
        val envvar: String?,
        val transformEnvvar: OptionTransformContext.(String) -> T,
        val transformAll: CallsTransformer<String, T>,
        val validator: OptionValidator<T>
) : OptionDelegate<T> {
    override var parameterGroup: ParameterGroup? = null
    override var groupName: String? = null
    override val metavar: String? = null
    override val nvalues: Int get() = 0
    override val parser = FlagOptionParser
    override var value: T by NullableLateinit("Cannot read from option delegate before parsing command line")
        private set
    override var names: Set<String> = names
        private set

    override operator fun provideDelegate(thisRef: ParameterHolder, prop: KProperty<*>): ReadOnlyProperty<ParameterHolder, T> {
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        val transformContext = OptionTransformContext(this, context)
        value = when (val v = getFinalValue(context, invocations, envvar)) {
            is FinalValue.Parsed -> transformAll(transformContext, invocations.map { it.name })
            is FinalValue.Sourced -> {
                if (v.values.size != 1 || v.values[0].values.size != 1) {
                    throw UsageError("Invalid flag value in file for option ${longestName()}", this)
                }
                transformEnvvar(transformContext, v.values[0].values[0])
            }
            is FinalValue.Envvar -> transformEnvvar(transformContext, v.value)
        }
    }

    override fun postValidate(context: Context) {
        validator(OptionTransformContext(this, context), value)
    }

    /** Create a new option that is a copy of this one with different transforms. */
    fun <T> copy(
            transformEnvvar: OptionTransformContext.(String) -> T,
            transformAll: CallsTransformer<String, T>,
            validator: OptionValidator<T>,
            names: Set<String> = this.names,
            secondaryNames: Set<String> = this.secondaryNames,
            help: String = this.help,
            hidden: Boolean = this.hidden,
            helpTags: Map<String, String> = this.helpTags,
            envvar: String? = this.envvar
    ): FlagOption<T> {
        return FlagOption(names, secondaryNames, help, hidden, helpTags, envvar, transformEnvvar, transformAll, validator)
    }

    /** Create a new option that is a copy of this one with the same transforms. */
    fun copy(
            validator: OptionValidator<T> = this.validator,
            names: Set<String> = this.names,
            secondaryNames: Set<String> = this.secondaryNames,
            help: String = this.help,
            hidden: Boolean = this.hidden,
            helpTags: Map<String, String> = this.helpTags,
            envvar: String? = this.envvar
    ): FlagOption<T> {
        return FlagOption(names, secondaryNames, help, hidden, helpTags, envvar, transformEnvvar, transformAll, validator)
    }
}

/**
 * Turn an option into a boolean flag.
 *
 * @param secondaryNames additional names for that option that cause the option value to be false. It's good
 *   practice to provide secondary names so that users can disable an option that was previously enabled.
 * @param default the value for this property if the option is not given on the command line.
 */
fun RawOption.flag(vararg secondaryNames: String, default: Boolean = false): FlagOption<Boolean> {
    return FlagOption(names, secondaryNames.toSet(), help, hidden, helpTags, envvar,
            transformEnvvar = {
                when (it.toLowerCase()) {
                    "true", "t", "1", "yes", "y", "on" -> true
                    "false", "f", "0", "no", "n", "off" -> false
                    else -> throw BadParameterValue("${readEnvvar(envvar ?: "")} is not a valid boolean", this)
                }
            },
            transformAll = {
                if (it.isEmpty()) default else it.last() !in secondaryNames
            },
            validator = {})
}

/**
 * Turn an option into a flag that counts the number of times the option occurs on the command line.
 */
fun RawOption.counted(): FlagOption<Int> {
    return FlagOption(names, emptySet(), help, hidden, helpTags, envvar,
            transformEnvvar = { valueToInt(it) },
            transformAll = { it.size },
            validator = {})
}

/**
 * Turn an option into a set of flags that each map to a value.
 *
 * ### Example:
 *
 * ```kotlin
 * option().switch(mapOf("--foo" to Foo(), "--bar" to Bar()))
 * ```
 */
fun <T : Any> RawOption.switch(choices: Map<String, T>): FlagOption<T?> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return FlagOption(choices.keys, emptySet(), help, hidden, helpTags, null,
            transformEnvvar = {
                throw UsageError("environment variables not supported for switch options", this)
            },
            transformAll = { names -> names.map { choices.getValue(it) }.lastOrNull() },
            validator = {}
    )
}

/**
 * Turn an option into a set of flags that each map to a value.
 *
 * ### Example:
 *
 * ```kotlin
 * option().switch("--foo" to Foo(), "--bar" to Bar())
 * ```
 */
fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): FlagOption<T?> = switch(mapOf(*choices))

/** Set a default value for a option. */
fun <T : Any> FlagOption<T?>.default(value: T): FlagOption<T> {
    return copy(
            transformEnvvar = { transformEnvvar(it) ?: value },
            transformAll = { transformAll(it) ?: value },
            validator = validator
    )
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should call `fail`
 * if the value is not valid. It is not called if the delegate value is null.
 */
fun <T : Any> FlagOption<T>.validate(validator: OptionValidator<T>): OptionDelegate<T> = copy(validator)

/**
 * Mark this option as deprecated in the help output.
 *
 * By default, a tag is added to the help message and a warning is printed if the option is used.
 *
 * This should be called after any validation.
 *
 * @param message The message to show in the warning or error. If null, no warning is issued.
 * @param tagName The tag to add to the help message
 * @param tagValue An extra message to add to the tag
 * @param error If true, when the option is invoked, a [CliktError] is raised immediately instead of issuing a warning.
 */
fun <T> FlagOption<T>.deprecated(
        message: String? = "",
        tagName: String? = "deprecated",
        tagValue: String = "",
        error: Boolean = false
): OptionDelegate<T> {
    val helpTags = if (tagName.isNullOrBlank()) helpTags else helpTags + mapOf(tagName to tagValue)
    return copy(transformEnvvar, deprecationTransformer(message, error, transformAll), validator, helpTags = helpTags)
}
