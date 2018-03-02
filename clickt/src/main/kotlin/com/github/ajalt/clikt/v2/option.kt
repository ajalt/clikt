package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.BadParameter
import com.github.ajalt.clikt.parser.HelpFormatter
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Option {
    val metavar: String?
    val help: String
    val parser: OptionParser2
    val names: Set<String>
    val nargs: Int
    val parameterHelp: ParameterHelp?
        get() = ParameterHelp(names.toList(), metavar, help, ParameterHelp.SECTION_OPTIONS,
                false, parser.repeatableForHelp(this))
}

class EagerOption(
        override val help: String,
        override val names: Set<String>,
        val callback: (Context2, EagerOption) -> Unit) : Option {
    override val parser: OptionParser2 = FlagOptionParser2()
    override val metavar: String? get() = null
    override val nargs: Int get() = 0
}

interface OptionDelegate<out T> : Option, ReadOnlyProperty<CliktCommand, T> {
    /** Implementations must call [CliktCommand.registerOption] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

class FlagOption<out T : Any>(
        override var names: Set<String>,
        override val metavar: String?,
        override val help: String,
        private val processAll: (List<Boolean>) -> T) : OptionDelegate<T> {
    override val nargs: Int get() = 0
    override val parser = FlagOptionParser2()

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
        return processAll(parser.rawValues)
    }

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T> {
        // TODO: better name inference
        if (names.isEmpty()) names = setOf("--" + prop.name)
        thisRef.registerOption(this)
        return this
    }
}


fun RawOption.flag(default: Boolean = false): OptionDelegate<Boolean> {
    return FlagOption(names, null, help, { it.lastOrNull() ?: default })
}

fun RawOption.counted(): OptionDelegate<Int> {
    return FlagOption(names, null, help, {
        for (name in names) require("/" !in name) { "counted options cannot have off names" }
        it.size
    })
}

fun <T : Any> RawOption.convert(metavar: String? = null, conversion: ValueProcessor<T>):
        LastOccurrenceOption<T, T> {
    return OptionWithValues(names, metavar, nargs, help, parser, conversion,
            defaultEachProcessor(), defaultAllProcessor())
}

internal typealias ValueProcessor<T> = (String) -> T
internal typealias EachProcessor<Teach, Tvalue> = (List<Tvalue>) -> Teach
internal typealias AllProcessor<Tall, Teach> = (List<Teach>) -> Tall

class OptionWithValues<out Tall, Teach, Tvalue>(
        override var names: Set<String>, // TODO private setter
        override val metavar: String?,
        override val nargs: Int,
        override val help: String,
        override val parser: OptionWithValuesParser2,
        val processValue: ValueProcessor<Tvalue>,
        val processEach: EachProcessor<Teach, Tvalue>,
        val processAll: AllProcessor<Tall, Teach>) : OptionDelegate<Tall> {
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall {
        return processAll(parser.rawValues.map { processEach(it.map { processValue(it) }) })
    }

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, Tall> {
        // TODO: better name inference
        if (names.isEmpty()) names = setOf("--" + prop.name)
        thisRef.registerOption(this)
        return this
    }
}

private typealias LastOccurrenceOption<Teach, Tvalue> = OptionWithValues<Teach?, Teach, Tvalue>
private typealias RawOption = LastOccurrenceOption<String, String>

private fun <T : Any> defaultEachProcessor(): EachProcessor<T, T> = { it.single() } // TODO error message
private fun <T : Any> defaultAllProcessor(): AllProcessor<T?, T> = { it.lastOrNull() }

@Suppress("unused")
fun CliktCommand.option(vararg names: String, help: String = ""): RawOption = OptionWithValues(
        names = names.toSet(),
        metavar = null,
        nargs = 1,
        help = help,
        parser = OptionWithValuesParser2(),
        processValue = { it },
        processEach = defaultEachProcessor(),
        processAll = defaultAllProcessor())

fun RawOption.int() = convert("INT") {
    it.toIntOrNull() ?: throw BadParameter("$it is not a valid integer")
}

fun <Tall, Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.transformAll(transform: AllProcessor<Tall, Teach>)
        : OptionWithValues<Tall, Teach, Tvalue> {
    return OptionWithValues(names, metavar, nargs, help, parser, processValue, processEach, transform)
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.default(value: Teach)
        : OptionWithValues<Teach, Teach, Tvalue> = transformAll {
    it.firstOrNull() ?: value
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.multiple() = transformAll { it }

fun <Teachi : Any, Teacho : Any, Tvalue> LastOccurrenceOption<Teachi, Tvalue>.transformNargs(
        nargs: Int, transform: EachProcessor<Teacho, Tvalue>): LastOccurrenceOption<Teacho, Tvalue> {
    // TODO: error message on transform
    return OptionWithValues(names, metavar, nargs, help, OptionWithValuesParser2(),
            processValue, transform, defaultAllProcessor())
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.paired()
        : LastOccurrenceOption<Pair<Tvalue, Tvalue>, Tvalue> = transformNargs(2) { it[0] to it[1] }

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.triple()
        : LastOccurrenceOption<Triple<Tvalue, Tvalue, Tvalue>, Tvalue> = transformNargs(2) { Triple(it[0], it[1], it[2]) }
