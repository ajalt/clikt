package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.options.ClicktCommand
import com.github.ajalt.clikt.parser.BadParameter
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Argument<out T> : ReadOnlyProperty<CliktCommand, T> {
    val name: String
    val nargs: Int
    val required: Boolean
    val metavar: String?
    val help: String
    val parameterHelp: ParameterHelp?
    var rawValues: List<String>

}

interface ProcessedArgument<out Tall, Tvalue> : Argument<Tall> {
    fun processValue(value: String): Tvalue
    fun processAll(values: List<Tvalue>): Tall
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall {
        return processAll(rawValues.map { processValue(it) })
    }
}

private typealias RawArgument = ProcessedArgument<String, String>

internal class ArgumentImpl(name: String?, override val help: String) : RawArgument {
    override var name: String = ""
        private set

    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, String?> {
        // TODO: better name inference
        if (name.isBlank()) name = prop.name
        return this
    }

    override val parameterHelp: ParameterHelp
        get() = ParameterHelp(listOf(name), metavar, help,
                ParameterHelp.SECTION_ARGUMENTS, required && nargs == 1 || nargs > 1, nargs < 0)
    override var rawValues: List<String> = emptyList()
    override val nargs: Int get() = 1  // TODO require this to be != 0 somewhere
    override val required: Boolean get() = false
    //    override val default: String? get() = null  // TODO check that this is null when nargs < 0
    override val metavar: String? get() = null

    override fun processValue(value: String): String = value
    override fun processAll(values: List<String>): String = values.single()  // nargs are checked in the parser
}

fun CliktCommand.argument(name: String? = null, help: String = ""): RawArgument {
    return ArgumentImpl(name, help)
}

abstract class ArgumentTransformer<Talli, Tvaluei, Tallo, Tvalueo>(
        val argument: ProcessedArgument<Talli, Tvaluei>) : ProcessedArgument<Tallo, Tvalueo> {
    override val name: String get() = argument.name
    override val nargs: Int get() = argument.nargs
    override val required: Boolean get() = argument.required
    override val metavar: String? get() = argument.metavar
    override val help: String get() = argument.help
    override val parameterHelp: ParameterHelp? get() = argument.parameterHelp
    override var rawValues: List<String>
        get() = argument.rawValues
        set(value) {
            argument.rawValues = value
        }
}

inline fun <Talli, Tvalue, Tallo> ProcessedArgument<Talli, Tvalue>.transformAll(
        nargs: Int? = null,
        required: Boolean? = null,
        crossinline transform: (List<Tvalue>) -> Tallo): ProcessedArgument<Tallo, Tvalue> {
    return object : ArgumentTransformer<Talli, Tvalue, Tallo, Tvalue>(this) {
        override val nargs: Int get() = nargs ?: super.nargs
        override val required: Boolean get() = required ?: super.required
        override fun processValue(value: String): Tvalue = argument.processValue(value)
        override fun processAll(values: List<Tvalue>): Tallo = transform(values)
    }
}

fun <Tvalue : Any> ProcessedArgument<Tvalue, Tvalue>.optional(): ProcessedArgument<Tvalue?, Tvalue> {
    return transformAll(required = false) { it.firstOrNull() }
}

inline fun <T : Any> RawArgument.convert(
        metavar: String? = null, crossinline conversion: (String) -> T):
        ProcessedArgument<T, T> {
    return object : ArgumentTransformer<String, String, T, T>(this) {
        override val metavar: String? get() = metavar
        override fun processValue(value: String): T = conversion(value)
        override fun processAll(values: List<T>): T = values.single()
    }
}

fun RawArgument.int() = convert("INT") {
    // TODO extract conversions to common location
    try {
        it.toInt()
    } catch (e: NumberFormatException) {
        throw BadParameter("$it is not a valid integer")
    }
}

fun RawArgument.file(exists: Boolean = false,
                     fileOkay: Boolean = true,
                     folderOkay: Boolean = true,
                     writable: Boolean = false,
                     readable: Boolean = false): ProcessedArgument<File, File> {
    val name = when {
        fileOkay && !folderOkay -> "File"
        !fileOkay && folderOkay -> "Directory"
        else -> "Path"
    }
    return convert(name.toUpperCase()) {
        File(it).also {
            if (exists && !it.exists()) throw BadParameter("$name \"$it\" does not exist.")
            if (!fileOkay && it.isFile) throw BadParameter("$name \"$it\" is a file")
            if (!folderOkay && it.isDirectory) throw BadParameter("$name \"$it\" is a directory.")
            if (writable && !it.canWrite()) throw BadParameter("$name \"$it\" is not writable.")
            if (readable && !it.canRead()) throw BadParameter("$name \"$it\" is not readable.")
        }
    }
}

fun <T> ProcessedArgument<T, T>.multiple(required: Boolean = false): ProcessedArgument<List<T>, T> {
    return transformAll(nargs = -1, required = required) { it }
}

fun <T> ProcessedArgument<T, T>.paired(): ProcessedArgument<Pair<T, T>, T> {
    return transformAll(nargs = 2) { it[0] to it[1] }
}

fun <T> ProcessedArgument<T, T>.triple(): ProcessedArgument<Triple<T, T, T>, T> {
    return transformAll(nargs = 2) { Triple(it[0], it[1], it[2]) }
}
