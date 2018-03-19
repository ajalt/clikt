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

// `T` is deliberately not an out parameter.
@Suppress("AddVarianceModifier")
class FlagOption<T>(
        names: Set<String>,
        override val secondaryNames: Set<String>,
        override val help: String,
        override val hidden: Boolean,
        val envvar: String?,
        val transformEnvvar: OptionTransformContext.(String) -> T,
        val transformAll: CallsTransformer<T, String>) : OptionDelegate<T> {
    override val metavar: String? = null
    override val nargs: Int get() = 0
    override val parser = FlagOptionParser
    private var value: T by NullableLateinit("Cannot read from option delegate before parsing command line")
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        val env = inferEnvvar(names, envvar, context.command.autoEnvvarPrefix)
        value = if (invocations.isNotEmpty() || env == null || System.getenv(env) == null) {
            transformAll(OptionTransformContext(this), invocations.map { it.name })
        } else {
            transformEnvvar(OptionTransformContext(this), System.getenv(env))
        }
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T> {
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }
}

fun RawOption.flag(vararg secondaryNames: String, default: Boolean = false): FlagOption<Boolean> {
    return FlagOption(names, secondaryNames.toSet(), help, hidden, envvar,
            transformEnvvar = {
                when (it) {
                    "true", "t", "1", "yes", "y" -> true
                    "false", "f", "0", "no", "n" -> false
                    else -> throw BadParameterValue("${System.getenv(envvar)} is not a valid boolean", this)
                }
            },
            transformAll = {
                if (it.isEmpty()) default else it.last() !in secondaryNames
            })
}

fun RawOption.counted(): FlagOption<Int> {
    return FlagOption(names, secondaryNames, help, hidden, envvar,
            transformEnvvar = { valueToInt(it) },
            transformAll = { it.size })
}

fun <T : Any> RawOption.switch(choices: Map<String, T>): FlagOption<T?> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return FlagOption(choices.keys, secondaryNames, help, hidden, null,
            transformEnvvar = {
                throw UsageError("environment variables not supported for switch options", this)
            },
            transformAll = { it.map { choices[it]!! }.lastOrNull() })
}

fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): FlagOption<T?> = switch(mapOf(*choices))

fun <T : Any> FlagOption<T?>.default(value: T): FlagOption<T> {
    return FlagOption(names, secondaryNames, help, hidden, envvar,
            transformEnvvar = { transformEnvvar(it) ?: value },
            transformAll = { transformAll(it) ?: value })
}

fun <T : Any> FlagOption<T>.validate(validator: OptionValidator<T>): OptionDelegate<T> {
    return FlagOption(names, secondaryNames, help, hidden, envvar,
            transformEnvvar = { transformEnvvar(it).also { validator(this, it) } },
            transformAll = { transformAll(it).also { validator(this, it) } }
    )
}
