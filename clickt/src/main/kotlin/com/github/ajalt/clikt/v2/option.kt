package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.BadParameter
import com.github.ajalt.clikt.parser.HelpFormatter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface Option<out T> : ReadOnlyProperty<CliktCommand, T> {
    val parameterHelp: HelpFormatter.ParameterHelp? // TODO: extract to parameter interface
    val eager: Boolean get() = false
    val metavar: String?
    val help: String
    val parser: OptionParser2
    val names: List<String>
}

class FlagOption<out T : Any>(
        override var names: List<String>,
        override val metavar: String?,
        override val help: String,
        override val parser: FlagOptionParser2,
        private val processAll: (List<Boolean>) -> T) : Option<T> {
    override val parameterHelp
        get() = HelpFormatter.ParameterHelp(names, metavar, help,
                HelpFormatter.ParameterHelp.SECTION_OPTIONS,
                false, parser.repeatableForHelp)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
        return processAll(parser.rawValues)
    }

    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T> {
        // TODO: better name inference
        if (names.isEmpty()) names = listOf("--" + prop.name)
        thisRef.registerOption(this)
        return this
    }
}


fun RawOption.flag(default: Boolean = false): Option<Boolean> {
    return FlagOption(names, null, help, FlagOptionParser2(names), { it.lastOrNull() ?: default })
}

fun RawOption.counted(): Option<Int> {
    for (name in names) require("/" !in name) { "counted options cannot have off names" }
    return FlagOption(names, null, help, FlagOptionParser2(names), { parser.rawValues.size })
}

fun <T : Any> RawOption.convert(metavar: String? = null, conversion: ValueProcessor<T>):
        LastOccurrenceOption<T, T> {
    return OptionWithValues(names, metavar, help, parser, conversion,
            defaultEachProcessor(), defaultAllProcessor())
}

private typealias ValueProcessor<T> = (String) -> T
private typealias EachProcessor<Teach, Tvalue> = (List<Tvalue>) -> Teach
private typealias AllProcessor<Tall, Teach> = (List<Teach>) -> Tall

class OptionWithValues<out Tall, Teach, Tvalue>(
        override var names: List<String>,
        override val metavar: String?,
        override val help: String,
        override val parser: OptionWithValuesParser2,
        val processValue: ValueProcessor<Tvalue>,
        val processEach: EachProcessor<Teach, Tvalue>,
        val processAll: AllProcessor<Tall, Teach>) : Option<Tall> {
    override val parameterHelp
        get() = HelpFormatter.ParameterHelp(names, metavar, help,
                HelpFormatter.ParameterHelp.SECTION_OPTIONS,
                false, parser.repeatableForHelp)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall {
        return processAll(parser.rawValues.map { processEach(it.map { processValue(it) }) })
    }

    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, Tall> {
        // TODO: better name inference
        if (names.isEmpty()) names = listOf("--" + prop.name)
        thisRef.registerOption(this)
        return this
    }
}

private typealias LastOccurrenceOption<Teach, Tvalue> = OptionWithValues<Teach?, Teach, Tvalue>
private typealias RawOption = LastOccurrenceOption<String, String>

private fun <T : Any> defaultEachProcessor(): EachProcessor<T, T> = { it.single() } // TODO error message
private fun <T : Any> defaultAllProcessor(): AllProcessor<T?, T> = { it.lastOrNull() }

fun CliktCommand.option(vararg names: String, help: String = ""): RawOption = OptionWithValues(
        names = names.toList(),
        metavar = null,
        help = help,
        parser = OptionWithValuesParser2(1),
        processValue = { it },
        processEach = defaultEachProcessor(),
        processAll = defaultAllProcessor())

fun RawOption.int() = convert("INT") {
    it.toIntOrNull() ?: throw BadParameter("$it is not a valid integer")
}

fun <Tall, Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.transformAll(transform: AllProcessor<Tall, Teach>)
        : OptionWithValues<Tall, Teach, Tvalue> {
    return OptionWithValues(names, metavar, help, parser, processValue, processEach, transform)
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.default(value: Teach) = transformAll {
    require(it.size < 2) // TODO error message
    it.firstOrNull() ?: value
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.multiple() = transformAll { it }

fun <Teachi : Any, Teacho : Any, Tvalue> LastOccurrenceOption<Teachi, Tvalue>.transformNargs(
        nargs: Int, transform: EachProcessor<Teacho, Tvalue>): LastOccurrenceOption<Teacho, Tvalue> {
    // TODO: error message on transform
    return OptionWithValues(names, metavar, help, OptionWithValuesParser2(nargs),
            processValue, transform, defaultAllProcessor())
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.paired()
        : LastOccurrenceOption<Pair<Tvalue, Tvalue>, Tvalue> = transformNargs(2) { it[0] to it[1] }

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.triple()
        : LastOccurrenceOption<Triple<Tvalue, Tvalue, Tvalue>, Tvalue> = transformNargs(2) { Triple(it[0], it[1], it[2]) }
