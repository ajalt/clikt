package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parsers.OptionParser
import com.github.ajalt.clikt.parsers.OptionWithValuesParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class OptionValueInvocation(val name: String, val option: Option) {
    /** Throw an exception indicating that an invalid value was provided. */
    fun fail(message: String, paramName: String? = null): Nothing {
        if (paramName == null) throw BadParameter(message, option)
        throw BadParameter(message, paramName)
    }
}

private typealias ValueProcessor<T> = OptionValueInvocation.(String) -> T
private typealias EachProcessor<EachT, ValueT> = Option.(List<ValueT>) -> EachT
private typealias AllProcessor<AllT, EachT> = Option.(List<EachT>) -> AllT

// `AllT` is deliberately not an out parameter. If it was, it would allow undesirable combinations such as
// default("").int()
@Suppress("AddVarianceModifier")
class OptionWithValues<AllT, EachT, ValueT>(
        names: Set<String>,
        val explicitMetavar: String?,
        val defaultMetavar: String?,
        override val nargs: Int,
        override val help: String,
        override val parser: OptionWithValuesParser,
        val processValue: ValueProcessor<ValueT>,
        val processEach: EachProcessor<EachT, ValueT>,
        val processAll: AllProcessor<AllT, EachT>) : OptionDelegate<AllT> {
    override val metavar: String? get() = explicitMetavar ?: defaultMetavar
    private var value: AllT by NullableLateinit("Cannot read from option delegate before parsing command line")
    override val secondaryNames: Set<String> get() = emptySet()
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        value = processAll(invocations.map {
            processEach(it.values.map { v -> processValue(OptionValueInvocation(it.name, this), v) })
        })
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): AllT = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, AllT> {
        names = inferOptionNames(names, prop.name)
        require(secondaryNames.isEmpty()) {
            "Secondary option names are only allowed on flag options."
        }
        thisRef.registerOption(this)
        return this
    }
}

internal typealias NullableOption<EachT, ValueT> = OptionWithValues<EachT?, EachT, ValueT>
internal typealias RawOption = NullableOption<String, String>

@PublishedApi
internal fun <T : Any> defaultEachProcessor(): EachProcessor<T, T> = { it.single() }

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): AllProcessor<T?, T> = { it.lastOrNull() }

@Suppress("unused")
fun CliktCommand.option(vararg names: String, help: String = "", metavar: String? = null): RawOption = OptionWithValues(
        names = names.toSet(),
        explicitMetavar = metavar,
        defaultMetavar = "TEXT",
        nargs = 1,
        help = help,
        parser = OptionWithValuesParser,
        processValue = { it },
        processEach = defaultEachProcessor(),
        processAll = defaultAllProcessor())

fun <AllT, EachT : Any, ValueT> NullableOption<EachT, ValueT>.transformAll(transform: AllProcessor<AllT, EachT>)
        : OptionWithValues<AllT, EachT, ValueT> {
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs,
            help, parser, processValue, processEach, transform)
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.default(value: EachT)
        : OptionWithValues<EachT, EachT, ValueT> = transformAll { it.lastOrNull() ?: value }

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.multiple()
        : OptionWithValues<List<EachT>, EachT, ValueT> = transformAll { it }

fun <EachInT : Any, EachOutT : Any, ValueT> NullableOption<EachInT, ValueT>.transformNargs(
        nargs: Int, transform: EachProcessor<EachOutT, ValueT>): NullableOption<EachOutT, ValueT> {
    require(nargs != 0) { "Cannot set nargs = 0. Use flag() instead." }
    require(nargs > 0) { "Options cannot have nargs < 0" }
    require(nargs > 1) { "Cannot set nargs = 1. Use convert() instead." }
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs, help, parser,
            processValue, transform, defaultAllProcessor())
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.paired()
        : NullableOption<Pair<ValueT, ValueT>, ValueT> {
    return transformNargs(nargs = 2) { it[0] to it[1] }
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.triple()
        : NullableOption<Triple<ValueT, ValueT, ValueT>, ValueT> {
    return transformNargs(nargs = 3) { Triple(it[0], it[1], it[2]) }
}

fun <T : Any> FlagOption<T>.validate(validator: (T) -> Unit): OptionDelegate<T> {
    return FlagOption(names, secondaryNames, help) {
        processAll(it).apply { validator(this) }
    }
}

fun <AllT, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.validate(validator: (AllT) -> Unit)
        : OptionDelegate<AllT> {
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs,
            help, parser, processValue, processEach) {
        processAll(it).apply { validator(this) }
    }
}

inline fun <T : Any> RawOption.convert(metavar: String = "VALUE", crossinline conversion: ValueProcessor<T>):
        NullableOption<T, T> {
    val proc: ValueProcessor<T> = {
        try {
            conversion(it)
        } catch (err: UsageError) {
            err.option = option
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "", paramName = name)
        }
    }
    return OptionWithValues(names, explicitMetavar, metavar, nargs, help, parser, proc,
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
                    OptionValueInvocation("", this), it)))))
        } ?: throw Abort()
    }
}
