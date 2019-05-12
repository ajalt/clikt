package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.*
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
 * @property transformEnvvar Called to transform string values from envvars into the option type.
 * @property transformAll Called to transform all invocations of this option into the final option type.
 */
// `T` is deliberately not an out parameter.
@Suppress("AddVarianceModifier")
class FlagOption<T>(
        names: Set<String>,
        override val secondaryNames: Set<String>,
        override val help: String,
        override val hidden: Boolean,
        override val helpTags: Map<String, String>,
        val envvar: String?,
        val transformEnvvar: OptionTransformContext.(String) -> T,
        val transformAll: CallsTransformer<String, T>) : OptionDelegate<T> {
    override val metavar: String? = null
    override val nvalues: Int get() = 0
    override val parser = FlagOptionParser
    private var value: T by NullableLateinit("Cannot read from option delegate before parsing command line")
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        val env = inferEnvvar(names, envvar, context.autoEnvvarPrefix)
        value = if (invocations.isNotEmpty() || env == null || System.getenv(env) == null) {
            transformAll(OptionTransformContext(this, context), invocations.map { it.name })
        } else {
            transformEnvvar(OptionTransformContext(this, context), System.getenv(env))
        }
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T> {
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
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
                    else -> throw BadParameterValue("${System.getenv(envvar)} is not a valid boolean", this)
                }
            },
            transformAll = {
                if (it.isEmpty()) default else it.last() !in secondaryNames
            })
}

/**
 * Turn an option into a flag that counts the number of times the option occurs on the command line.
 */
fun RawOption.counted(): FlagOption<Int> {
    return FlagOption(names, emptySet(), help, hidden, helpTags, envvar,
            transformEnvvar = { valueToInt(it) },
            transformAll = { it.size })
}

/** Turn an option into a set of flags that each map to a value. */
fun <T : Any> RawOption.switch(choices: Map<String, T>): FlagOption<T?> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return FlagOption(choices.keys, emptySet(), help, hidden, helpTags, null,
            transformEnvvar = {
                throw UsageError("environment variables not supported for switch options", this)
            },
            transformAll = { it.map { choices[it]!! }.lastOrNull() })
}

/** Turn an option into a set of flags that each map to a value. */
fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): FlagOption<T?> = switch(mapOf(*choices))

/** Set a default value for a option. */
fun <T : Any> FlagOption<T?>.default(value: T): FlagOption<T> {
    return FlagOption(names, secondaryNames, help, hidden, helpTags, envvar,
            transformEnvvar = { transformEnvvar(it) ?: value },
            transformAll = { transformAll(it) ?: value })
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should call `fail`
 * if the value is not valid. It is not called if the delegate value is null.
 */
fun <T : Any> FlagOption<T>.validate(validator: OptionValidator<T>): OptionDelegate<T> {
    return FlagOption(names, secondaryNames, help, hidden, helpTags, envvar,
            transformEnvvar = { transformEnvvar(it).also { validator(this, it) } },
            transformAll = { transformAll(it).also { validator(this, it) } }
    )
}

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
    return FlagOption(names, secondaryNames, help, hidden, helpTags, envvar, transformEnvvar, deprecationTransformer(message, error, transformAll))
}
