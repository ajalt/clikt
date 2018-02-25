package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.options.Context
import com.github.ajalt.clikt.parser.BadParameter
import com.github.ajalt.clikt.parser.HelpFormatter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class CliktCommand(help: String? = null, version: String? = null) {
    private var subcommands: List<CliktCommand> = emptyList()
    private var options: MutableList<Option<*>> = mutableListOf()

    val context: Context = TODO()
    fun subcommands(vararg command: CliktCommand): CliktCommand {
        subcommands += command
        return this
    }

    open fun main(args: Array<String>): Unit = TODO()
    abstract fun run()

    fun registerOption(option: Option<*>) {
        options.add(option)
    }
}

interface Option<out T_prop> : ReadOnlyProperty<CliktCommand, T_prop> {
    val parameterHelp: HelpFormatter.ParameterHelp?
    val eager: Boolean get() = false
    val metavar: String?
    val help: String
    val parser: OptionParser2
    val names: List<String>
}

private typealias RawOption = OptionWithSingleValue<String>

interface OptionWithValues<out Tall, Teach> : Option<Tall> {
    fun processEach(value: List<String>): Teach
    fun processAll(values: List<Teach>): Tall
    val rawValues: List<List<String>>

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall {
        return processAll(rawValues.map { processEach(it) })
    }
}

interface OptionWithSingleValue<T> : OptionWithValues<T?, T> {
    override fun processAll(values: List<T>): T? = values.lastOrNull()
}

interface OptionWithMultipleValues<T> : OptionWithValues<List<T>, T> {
    override fun processAll(values: List<T>): List<T> = values
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
    override val rawValues: List<List<String>> get() = parser.rawValues

    override fun processEach(value: List<String>): String {
        return value.single() // TODO error messages
    }
}

fun CliktCommand.option(vararg names: String, help: String = ""): RawOption {
    return OptionImpl(names.toList(), "STRING", help)
}

// TODO better type param names
abstract class OptionTransformer<out Ti, out To>(val option: Option<Ti>) : Option<To> {
    override val eager: Boolean get() = option.eager
    override val parameterHelp: HelpFormatter.ParameterHelp? get() = option.parameterHelp
    override val metavar: String? get() = option.metavar
    override val help: String get() = option.help
    override val names: List<String> get() = option.names
    override val parser: OptionParser2 get() = option.parser
}

abstract class OptionWithValuesTransformer<out Teachi, Talli, out Teacho, Tallo>(
        val option: OptionWithValues<Teachi, Talli>) : OptionWithValues<Teacho, Tallo> {
    override val eager: Boolean get() = option.eager
    override val parameterHelp: HelpFormatter.ParameterHelp? get() = option.parameterHelp
    override val metavar: String? get() = option.metavar
    override val help: String get() = option.help
    override val names: List<String> get() = option.names
    override val parser: OptionParser2 get() = option.parser
    override val rawValues: List<List<String>> get() = option.rawValues
}

inline fun <T> RawOption.convert(metavar: String? = null, crossinline conversion: (String) -> T): OptionWithSingleValue<T> {
    return object : OptionWithValuesTransformer<String?, String, T?, T>(this), OptionWithSingleValue<T> {
        override val metavar: String? get() = metavar ?: super.metavar
        override fun processEach(value: List<String>): T {
            return conversion(value.single())
        }
    }
}

// TODO: explicit return types
fun RawOption.flag() = object : OptionTransformer<String?, Boolean>(this) {
    override val parser: FlagOptionParser2 = FlagOptionParser2(this.names)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Boolean {
        return parser.rawValues.lastOrNull() ?: false
    }
}

fun RawOption.counted() = object : OptionTransformer<String?, Int>(this) {
    init {
        for (name in names) require("/" !in name) { "counted options cannot have off names" }
    }

    override val parser: FlagOptionParser2 = FlagOptionParser2(this.names)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Int {
        return parser.rawValues.size
    }
}

fun RawOption.int() = convert("INT") {
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        throw BadParameter("$it is not a valid integer")
    }
}

fun <T : Any> OptionWithSingleValue<T>.default(value: T): OptionWithValues<T, T> {
    return object : OptionWithValuesTransformer<T?, T, T, T>(this) {
        override fun processEach(value: List<String>): T = this@default.processEach(value)
        override fun processAll(values: List<T>): T = values.lastOrNull() ?: value
    }
}

fun <T : Any> OptionWithSingleValue<T>.multiple(): OptionWithMultipleValues<T> {
    return object : OptionWithValuesTransformer<T?, T, List<T>, T>(this), OptionWithMultipleValues<T> {
        override fun processEach(value: List<String>): T = option.processEach(value)
    }
}
