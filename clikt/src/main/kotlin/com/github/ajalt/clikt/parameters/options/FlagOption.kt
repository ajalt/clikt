package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
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
    return FlagOption(names, secondaryNames.toSet(), help, hidden, envvar,
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
    return FlagOption(names, emptySet(), help, hidden, envvar,
            transformEnvvar = { valueToInt(it) },
            transformAll = { it.size })
}

/** Turn an option into a set of flags that each map to a value. */
fun <T : Any> RawOption.switch(choices: Map<String, T>): FlagOption<T?> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return FlagOption(choices.keys, emptySet(), help, hidden, null,
            transformEnvvar = {
                throw UsageError("environment variables not supported for switch options", this)
            },
            transformAll = { it.map { choices[it]!! }.lastOrNull() })
}

/** Turn an option into a set of flags that each map to a value. */
fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): FlagOption<T?> = switch(mapOf(*choices))

/** Set a default value for a option. */
fun <T : Any> FlagOption<T?>.default(value: T): FlagOption<T> {
    return FlagOption(names, secondaryNames, help, hidden, envvar,
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
    return FlagOption(names, secondaryNames, help, hidden, envvar,
            transformEnvvar = { transformEnvvar(it).also { validator(this, it) } },
            transformAll = { transformAll(it).also { validator(this, it) } }
    )
}
