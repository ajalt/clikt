package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.options.*
import com.github.ajalt.clikt.parser.BadParameter
import com.github.ajalt.clikt.parser.HelpFormatter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class CliktCommand(help: String? = null, version: String? = null) {
    private var subcommands: List<CliktCommand> = emptyList()
    private var options: MutableList<Option<*, *>> = mutableListOf()

    val context: Context = TODO()
    fun subcommands(vararg command: CliktCommand): CliktCommand {
        subcommands += command
        return this
    }

    open fun main(args: Array<String>): Unit = TODO()
    abstract fun run()

    fun registerOption(option: Option<*, *>) {
        options.add(option)
    }
}

interface Option<out T_prop, out T_parse> : ReadOnlyProperty<CliktCommand, T_prop> {
    val parameterHelp: HelpFormatter.ParameterHelp?
    val eager: Boolean get() = false
    val metavar: String?
    val help: String
    val parser: OptionParser2<T_parse>
    val names: List<String>
}

private typealias RawOption = OptionWithArg<String?, String>

interface OptionWithArg<out T, U> : Option<T, List<String>> {
    fun processEach(value: List<String>): U
    fun processAll(values: List<U>): T

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
        return processAll(parser.rawValues.map { processEach(it) })
    }
}


internal class OptionImpl(names: List<String>,
                          override val metavar: String?,
                          override val help: String) : RawOption {
    override var names: List<String> = names
        private set

    override val parameterHelp: HelpFormatter.ParameterHelp
        get() = HelpFormatter.ParameterHelp(names, metavar,
                help,
                HelpFormatter.ParameterHelp.SECTION_OPTIONS,
                false, parser.repeatableForHelp)

    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, String?> {
        // TODO: better name inference
        if (names.isEmpty()) names = listOf("--" + prop.name)
        return this
    }


    override val parser = OptionWithValuesParser2(1)

    override fun processEach(value: List<String>): String {
        return value.first() // TODO error messages
    }

    override fun processAll(values: List<String>): String? {
        return values.last()
    }
}

fun CliktCommand.option(vararg names: String, help: String = ""): RawOption {
    return OptionImpl(names.toList(), ""/*TODO*/, help)
}

// TODO better type param names
abstract class OptionTransformer<out Ti, out Ui, out To, out Uo>(val option: Option<Ti, Ui>) : Option<To, Uo> {
    override val eager: Boolean get() = option.eager
    override val parameterHelp: HelpFormatter.ParameterHelp? get() = option.parameterHelp
    override val metavar: String? get() = option.metavar
    override val help: String get() = option.help
    override val names: List<String> get() = option.names
}

abstract class OptionTypeTransformer<out Ti, out To, out U>(option: Option<Ti, U>) : OptionTransformer<Ti, U, To, U>(option) {
    override val parser: OptionParser2<U> get() = option.parser
}

// TODO: explicit return types
fun RawOption.flag() = object : OptionTransformer<String?, List<String>, Boolean, Boolean>(this) {
    override val parser: OptionParser2<Boolean> = FlagOptionParser2(this.names)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Boolean {
        return parser.rawValues.lastOrNull() ?: false
    }
}

fun RawOption.counted() = object : OptionTransformer<String?, List<String>, Int, Boolean>(this) {
    init {
        for (name in names) require("/" !in name) { "counted options cannot have off names" }
    }

    override val parser: OptionParser2<Boolean> = FlagOptionParser2()

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Int {
        return parser.rawValues.size
    }
}

fun RawOption.int() = object : OptionTypeTransformer<String?, Int?, List<String>>(this) {

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Int? {
        val value = this@int.getValue(thisRef, property) ?: return null
        try {
            return value.toInt()
        } catch (e: NumberFormatException) {
            throw BadParameter("$value is not a valid integer")
        }
    }
}

fun <T : Any, U> Option<T?, U>.default(value: T) = object : OptionTypeTransformer<T?, T, U>(this) {
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
        return this@default.getValue(thisRef, property) ?: value
    }
}

fun <T : Any> Option<T?, List<T>>.multiple() = object : OptionTypeTransformer<T?, List<T>, List<T>>(this) {
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): List<T> {
        return parser.rawValues
    }
}
