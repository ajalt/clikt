package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parsers.FlagOptionParser
import com.github.ajalt.clikt.parsers.OptionParser
import com.github.ajalt.clikt.parsers.OptionWithValuesParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Option {
    val metavar: String?
    val help: String
    val parser: OptionParser
    val names: Set<String>
    val nargs: Int
    val parameterHelp: ParameterHelp.Option?
        get() = ParameterHelp.Option(names.toList(), metavar, help, parser.repeatableForHelp(this))

    fun finalize(context: Context)
}

class EagerOption(
        override val help: String,
        override val names: Set<String>,
        private val callback: (Context, EagerOption) -> Unit) : Option {
    override val parser: OptionParser = FlagOptionParser()
    override val metavar: String? get() = null
    override val nargs: Int get() = 0
    override fun finalize(context: Context) {
        callback(context, this)
    }
}


interface OptionDelegate<out T> : Option, ReadOnlyProperty<CliktCommand, T> {
    /** Implementations must call [CliktCommand.registerOption] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

private fun inferOptionNames(names: Set<String>, propertyName: String): Set<String> {
    if (names.isNotEmpty()) return names
    val normalizedName = propertyName.split(Regex("(?<=[a-z])(?=[A-Z])"))
            .joinToString("-", prefix = "--") { it.toLowerCase() }
    return setOf(normalizedName)
}

class FlagOption<out T : Any>(
        names: Set<String>,
        override val help: String,
        val processAll: (List<Boolean>) -> T) : OptionDelegate<T> {
    override val metavar: String? = null
    override val nargs: Int get() = 0
    override val parser = FlagOptionParser()
    private var value: T by ExplicitLazy("Cannot read from option delegate before parsing command line")
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context) {
        value = processAll(parser.rawValues)
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T> {
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }
}


fun RawOption.flag(default: Boolean = false): FlagOption<Boolean> {
    return FlagOption(names, help, { it.lastOrNull() ?: default })
}

fun RawOption.counted(): FlagOption<Int> {
    return FlagOption(names, help) {
        for (name in names) require("/" !in name) { "counted parameters cannot have off names" }
        it.size
    }
}

fun <T : Any> RawOption.convert(metavar: String = "VALUE", conversion: ValueProcessor<T>):
        NullableOption<T, T> {
    return OptionWithValues(names, explicitMetavar, metavar, nargs, help, parser, conversion,
            defaultEachProcessor(), defaultAllProcessor())
}

internal typealias ValueProcessor<T> = (String) -> T
internal typealias EachProcessor<Teach, Tvalue> = (List<Tvalue>) -> Teach
internal typealias AllProcessor<Tall, Teach> = (List<Teach>) -> Tall

class OptionWithValues<out Tall, Teach, Tvalue>(
        names: Set<String>,
        val explicitMetavar: String?,
        val defaultMetavar: String?,
        override val nargs: Int,
        override val help: String,
        override val parser: OptionWithValuesParser,
        val processValue: ValueProcessor<Tvalue>,
        val processEach: EachProcessor<Teach, Tvalue>,
        val processAll: AllProcessor<Tall, Teach>) : OptionDelegate<Tall> {
    override val metavar: String? get() = explicitMetavar ?: defaultMetavar
    private var value: Tall by ExplicitLazy("Cannot read from option delegate before parsing command line")
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context) {
        value = processAll(parser.rawValues.map { processEach(it.map { processValue(it) }) })
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, Tall> {
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }
}

internal typealias NullableOption<Teach, Tvalue> = OptionWithValues<Teach?, Teach, Tvalue>
internal typealias RawOption = NullableOption<String, String>

private fun <T : Any> defaultEachProcessor(): EachProcessor<T, T> = { it.single() }
private fun <T : Any> defaultAllProcessor(): AllProcessor<T?, T> = { it.lastOrNull() }

@Suppress("unused")
fun CliktCommand.option(vararg names: String, help: String = "", metavar: String? = null): RawOption = OptionWithValues(
        names = names.toSet(),
        explicitMetavar = metavar,
        defaultMetavar = "TEXT",
        nargs = 1,
        help = help,
        parser = OptionWithValuesParser(),
        processValue = { it },
        processEach = defaultEachProcessor(),
        processAll = defaultAllProcessor())

fun <Tall, Teach : Any, Tvalue> NullableOption<Teach, Tvalue>.transformAll(transform: AllProcessor<Tall, Teach>)
        : OptionWithValues<Tall, Teach, Tvalue> {
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs,
            help, parser, processValue, processEach, transform)
}

fun <Teach : Any, Tvalue> NullableOption<Teach, Tvalue>.default(value: Teach)
        : OptionWithValues<Teach, Teach, Tvalue> = transformAll { it.firstOrNull() ?: value }

fun <Teach : Any, Tvalue> NullableOption<Teach, Tvalue>.multiple()
        : OptionWithValues<List<Teach>, Teach, Tvalue> = transformAll { it }

fun <Teachi : Any, Teacho : Any, Tvalue> NullableOption<Teachi, Tvalue>.transformNargs(
        nargs: Int, transform: EachProcessor<Teacho, Tvalue>): NullableOption<Teacho, Tvalue> {
    require(nargs != 0) { "Cannot set nargs = 0. Use flag() instead." }
    require(nargs > 0) { "Options cannot have nargs < 0" }
    require(nargs > 1) { "Cannot set nargs = 1. Use convert() instead." }
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs, help, OptionWithValuesParser(),
            processValue, transform, defaultAllProcessor())
}

fun <Teach : Any, Tvalue> NullableOption<Teach, Tvalue>.paired()
        : NullableOption<Pair<Tvalue, Tvalue>, Tvalue> {
    return transformNargs(nargs = 2) { it[0] to it[1] }
}

fun <Teach : Any, Tvalue> NullableOption<Teach, Tvalue>.triple()
        : NullableOption<Triple<Tvalue, Tvalue, Tvalue>, Tvalue> {
    return transformNargs(nargs = 3) { Triple(it[0], it[1], it[2]) }
}

fun <T : Any> FlagOption<T>.validate(validator: (T) -> Unit): OptionDelegate<T> {
    return FlagOption(names, help) {
        processAll(it).apply { validator(this) }
    }
}

fun <Tall, Teach, Tvalue> OptionWithValues<Tall, Teach, Tvalue>.validate(validator: (Tall) -> Unit)
        : OptionDelegate<Tall> {
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs,
            help, parser, processValue, processEach) {
        processAll(it).apply { validator(this) }
    }
}
