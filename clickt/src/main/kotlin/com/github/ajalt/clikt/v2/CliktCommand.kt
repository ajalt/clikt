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

interface OptionWithValues<out Tall, Teach, Tvalue> : Option<Tall> {
    fun processValue(value: String): Tvalue
    fun processEach(values: List<Tvalue>): Teach
    fun processAll(values: List<Teach>): Tall
    val rawValues: List<List<String>>
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall {
        return processAll(rawValues.map { processEach(it.map { processValue(it) }) })
    }

}

interface LastOccurrenceOption<Teach : Any, Tvalue> : OptionWithValues<Teach?, Teach, Tvalue> {
    override fun processAll(values: List<Teach>): Teach? = values.lastOrNull()
}

private typealias RawOption = LastOccurrenceOption<String, String>


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

    override fun processEach(values: List<String>): String {
        return values.single() // TODO error messages
    }

    override fun processValue(value: String): String = value
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

abstract class OptionWithValuesTransformer<out Talli, Teachi, Tvaluei, out Tallo, Teacho, Tvalueo>(
        val option: OptionWithValues<Talli, Teachi, Tvaluei>) : OptionWithValues<Tallo, Teacho, Tvalueo> {
    override val eager: Boolean get() = option.eager
    override val parameterHelp: HelpFormatter.ParameterHelp? get() = option.parameterHelp
    override val metavar: String? get() = option.metavar
    override val help: String get() = option.help
    override val names: List<String> get() = option.names
    override val parser: OptionParser2 get() = option.parser
    override val rawValues: List<List<String>> get() = option.rawValues
}

fun RawOption.flag(): Option<Boolean> = object : OptionTransformer<String?, Boolean>(this) {
    override val parser: FlagOptionParser2 = FlagOptionParser2(this.names)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Boolean {
        return parser.rawValues.lastOrNull() ?: false
    }
}

fun RawOption.counted(): Option<Int> = object : OptionTransformer<String?, Int>(this) {
    init {
        for (name in names) require("/" !in name) { "counted options cannot have off names" }
    }

    override val parser: FlagOptionParser2 = FlagOptionParser2(this.names)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Int {
        return parser.rawValues.size
    }
}

// TODO: make similar functions for the other two process* methods
inline fun <T : Any> RawOption.convert(metavar: String? = null, crossinline conversion: (String) -> T):
        LastOccurrenceOption<T, T> {
    return object : OptionWithValuesTransformer<String?, String, String, T?, T, T>(this),
            LastOccurrenceOption<T, T> {
        override val metavar: String? get() = metavar ?: super.metavar
        override fun processValue(value: String): T = conversion(value)
        override fun processEach(values: List<T>): T = values.single() // TODO error message
    }
}

fun RawOption.int() = convert("INT") {
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        throw BadParameter("$it is not a valid integer")
    }
}

inline fun <Tall, Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.transformAll(
        crossinline transform: (List<Teach>) -> Tall)
        : OptionWithValues<Tall, Teach, Tvalue> {
    return object : OptionWithValuesTransformer<Teach?, Teach, Tvalue, Tall, Teach, Tvalue>(this) {
        override fun processValue(value: String): Tvalue = this@transformAll.processValue(value)
        override fun processEach(values: List<Tvalue>): Teach = this@transformAll.processEach(values)
        override fun processAll(values: List<Teach>): Tall = transform(values)
    }
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.default(value: Teach) = transformAll {
    require(it.size < 2) // TODO error message
    it.firstOrNull() ?: value
}

fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.multiple() = transformAll { it }



fun <Teach : Any, Tvalue> LastOccurrenceOption<Teach, Tvalue>.paired():
        LastOccurrenceOption<Pair<Tvalue,Tvalue>, Tvalue> {
    return object : OptionWithValuesTransformer<Teach? ,Teach, Tvalue, Pair<Tvalue, Tvalue>?, Pair<Tvalue, Tvalue>, Tvalue>(this),
            LastOccurrenceOption<Pair<Tvalue,Tvalue>, Tvalue> {
        override val parser: OptionParser2 get() = OptionWithValuesParser2(2)  // TODO: avoid new parser?
        override fun processValue(value: String): Tvalue = this@paired.processValue(value)
        override fun processEach(values: List<Tvalue>): Pair<Tvalue, Tvalue> {
            require(values.size == 2) // TODO: error message
            return values[0] to values[1]
        }
    }
}
