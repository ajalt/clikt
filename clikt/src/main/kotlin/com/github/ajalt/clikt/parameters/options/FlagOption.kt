package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parsers.FlagOptionParser
import com.github.ajalt.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FlagOption<out T>(
        names: Set<String>,
        override val secondaryNames: Set<String>,
        override val help: String,
        val processAll: Option.(List<String>) -> T) : OptionDelegate<T> {
    override val metavar: String? = null
    override val nargs: Int get() = 0
    override val parser = FlagOptionParser
    private var value: T by NullableLateinit("Cannot read from option delegate before parsing command line")
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        value = processAll(invocations.map { it.name })
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T> {
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }
}

fun RawOption.flag(vararg secondaryNames: String, default: Boolean = false): FlagOption<Boolean> {
    return FlagOption(names, secondaryNames.toSet(), help, {
        if (it.isEmpty()) default else it.last() !in secondaryNames
    })
}

fun RawOption.counted(): FlagOption<Int> {
    return FlagOption(names, secondaryNames, help) { it.size }
}

fun <T : Any> RawOption.switch(choices: Map<String, T>): FlagOption<T?> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return FlagOption(choices.keys, secondaryNames, help) { it.map { choices[it]!! }.lastOrNull() }
}

fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): FlagOption<T?> = switch(mapOf(*choices))

fun <T : Any> FlagOption<T?>.default(value: T): FlagOption<T> {
    return FlagOption(names, secondaryNames, help) { processAll(it) ?: value }
}
