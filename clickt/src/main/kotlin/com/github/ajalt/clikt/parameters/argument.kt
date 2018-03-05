package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.BadParameter
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.core.CliktCommand
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
    /** Implementations must call [CliktCommand.registerArgument] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

class ProcessedArgument<out Tall, Tvalue>(
        override var name: String,
        override val nargs: Int,
        override val required: Boolean,
        override val metavar: String?,
        override val help: String,
        val processValue: ValueProcessor<Tvalue>,
        val processAll: AllProcessor<Tall, Tvalue>) : Argument<Tall> {
    init {
        require(nargs != 0) // TODO error message
    }

    override var rawValues: List<String> = emptyList()
    override val parameterHelp
        get() = ParameterHelp(listOf(name), metavar, help,
                ParameterHelp.SECTION_ARGUMENTS,
                required && nargs == 1 || nargs > 1, nargs < 0)

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): Tall {
        return processAll(rawValues.map { processValue(it) })
    }

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>):
            ReadOnlyProperty<CliktCommand, Tall> {
        // TODO: better name inference
        if (name.isBlank()) name = prop.name
        thisRef.registerArgument(this)
        return this
    }
}

private typealias RawArgument = ProcessedArgument<String, String>

private fun <T : Any> defaultAllProcessor(): AllProcessor<T, T> = { it.single() }

fun CliktCommand.argument(name: String = "", help: String = ""): RawArgument {
    return ProcessedArgument(name, 1, true, "STRING", help, { it }, defaultAllProcessor())
}

fun <Talli, Tvalue, Tallo> ProcessedArgument<Talli, Tvalue>.transformAll(
        nargs: Int? = null,
        required: Boolean? = null,
        transform: AllProcessor<Tallo, Tvalue>): ProcessedArgument<Tallo, Tvalue> {
    return ProcessedArgument(name, nargs ?: this.nargs, required
            ?: this.required,
            metavar, help, processValue, transform)
}

fun <Tall : Any, Tvalue> ProcessedArgument<Tall, Tvalue>.optional()
        : ProcessedArgument<Tall?, Tvalue> = transformAll(required = false) {
    if (it.isEmpty()) null else processAll(it)
}

fun <T : Any> ProcessedArgument<T, T>.multiple(required: Boolean = false): ProcessedArgument<List<T>, T> {
    return transformAll(nargs = -1, required = required) { it }
}

fun <T : Any> ProcessedArgument<T, T>.paired(): ProcessedArgument<Pair<T, T>, T> {
    return transformAll(nargs = 2) { it[0] to it[1] }
}

fun <T : Any> ProcessedArgument<T, T>.triple(): ProcessedArgument<Triple<T, T, T>, T> {
    return transformAll(nargs = 2) { Triple(it[0], it[1], it[2]) }
}

fun <T : Any> ProcessedArgument<T, T>.default(value: T): ProcessedArgument<T, T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

@JvmName("nullableDefault")
fun <T : Any> ProcessedArgument<T?, T>.default(value: T): ProcessedArgument<T, T> {
    return transformAll(required = false) { it.firstOrNull() ?: value }
}

fun <T : Any> RawArgument.convert(metavar: String? = null, conversion: (String) -> T): ProcessedArgument<T, T> {
    return ProcessedArgument(name, nargs, required, metavar, help, conversion, defaultAllProcessor())
}

fun RawArgument.int() = convert("INT") {
    // TODO extract conversions to common location
    it.toIntOrNull() ?: throw BadParameter("$it is not a valid integer")
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
