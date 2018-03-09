package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.ExplicitLazy
import com.github.ajalt.clikt.parsers.OptionWithValuesParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


private typealias ValueProcessor<T> = OptionWithValuesParser.Invocation.(String) -> T
private typealias EachProcessor<Teach, Tvalue> = Option.(List<Tvalue>) -> Teach
private typealias AllProcessor<Tall, Teach> = Option.(List<Teach>) -> Tall

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
    override val secondaryNames: Set<String> get() = emptySet()
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context) {
        value = processAll(parser.rawValues.map { processEach(it.values.map { v -> processValue(it, v) }) })
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, Tall> {
        names = inferOptionNames(names, prop.name)
        require(secondaryNames.isEmpty()) {
            "Secondary option names are only allowed on flag options."
        }
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
        : OptionWithValues<Teach, Teach, Tvalue> = transformAll { it.lastOrNull() ?: value }

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
    return FlagOption(names, secondaryNames, help) {
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

fun <T : Any> RawOption.convert(metavar: String = "VALUE", conversion: ValueProcessor<T>):
        NullableOption<T, T> {
    return OptionWithValues(names, explicitMetavar, metavar, nargs, help, parser, conversion,
            defaultEachProcessor(), defaultAllProcessor())
}


fun <T : Any> NullableOption<T, T>.prompt(
        text: String? = null,
        default: String? = null,
        hideInput: Boolean = false,
        requireConfirmation: Boolean = false,
        confirmationPrompt: String = "Repeat for confirmation: ",
        promptSuffix: String = ": ",
        showDefault: Boolean = true): OptionWithValues<T, T, T> = transformAll {
    val promptText = text ?: names.maxBy { it.length }
            ?.replace(Regex("^--?"), "")
            ?.replace("-", " ")?.capitalize() ?: "Value"

    val provided = it.lastOrNull()
    if (provided != null) provided
    else {
        TermUi.prompt(promptText, default, hideInput, requireConfirmation,
                confirmationPrompt, promptSuffix, showDefault) {
            processAll(listOf(processEach(listOf(processValue(
                    OptionWithValuesParser.Invocation("", listOf(it)), it)))))
        } ?: throw Abort()
    }
}
