package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parsers.OptionParser
import com.github.ajalt.clikt.parsers.OptionWithValuesParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class OptionCallTransformContext(val name: String, val option: Option) : Option by option {
    /** Throw an exception indicating that an invalid value was provided. */
    fun fail(message: String): Nothing = throw BadParameterValue(message, name)
}

class OptionTransformContext(val option: Option) : Option by option {
    /** Throw an exception indicating that usage was incorrect. */
    fun fail(message: String): Nothing = throw UsageError(message, option)
}

typealias ValueTransformer<ValueT> = OptionCallTransformContext.(String) -> ValueT
typealias ArgsTransformer<EachT, ValueT> = OptionCallTransformContext.(List<ValueT>) -> EachT
typealias CallsTransformer<AllT, EachT> = OptionTransformContext.(List<EachT>) -> AllT
typealias OptionValidator<AllT> = OptionTransformContext.(AllT) -> Unit

// `AllT` is deliberately not an out parameter. If it was, it would allow undesirable combinations such as
// default("").int()
@Suppress("AddVarianceModifier")
class OptionWithValues<AllT, EachT, ValueT>(
        names: Set<String>,
        val explicitMetavar: String?,
        val defaultMetavar: String?,
        override val nargs: Int,
        override val help: String,
        override val hidden: Boolean,
        val envvar: String?,
        val envvarSplit: Regex,
        override val parser: OptionWithValuesParser,
        val transformValue: ValueTransformer<ValueT>,
        val transformEach: ArgsTransformer<EachT, ValueT>,
        val transformAll: CallsTransformer<AllT, EachT>) : OptionDelegate<AllT> {
    override val metavar: String? get() = explicitMetavar ?: defaultMetavar
    private var value: AllT by NullableLateinit("Cannot read from option delegate before parsing command line")
    override val secondaryNames: Set<String> get() = emptySet()
    override var names: Set<String> = names
        private set

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        val env = inferEnvvar(names, envvar, context.command.autoEnvvarPrefix)
        val inv = if (invocations.isNotEmpty() || env == null || System.getenv(env) == null) {
            invocations
        } else {
            System.getenv(env).split(envvarSplit).map { OptionParser.Invocation(env, listOf(it)) }
        }

        value = transformAll(OptionTransformContext(this), inv.map {
            val tc = OptionCallTransformContext(it.name, this)
            transformEach(tc, it.values.map { v -> transformValue(tc, v) })
        })
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): AllT = value

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, AllT> {
        require(secondaryNames.isEmpty()) {
            "Secondary option names are only allowed on flag options."
        }
        names = inferOptionNames(names, prop.name)
        thisRef.registerOption(this)
        return this
    }
}

internal typealias NullableOption<EachT, ValueT> = OptionWithValues<EachT?, EachT, ValueT>
internal typealias RawOption = NullableOption<String, String>

@PublishedApi
internal fun <T : Any> defaultEachProcessor(): ArgsTransformer<T, T> = { it.single() }

@PublishedApi
internal fun <T : Any> defaultAllProcessor(): CallsTransformer<T?, T> = { it.lastOrNull() }

@Suppress("unused")
fun CliktCommand.option(vararg names: String, help: String = "", metavar: String? = null,
                        hidden: Boolean = false, envvar: String? = null): RawOption = OptionWithValues(
        names = names.toSet(),
        explicitMetavar = metavar,
        defaultMetavar = "TEXT",
        nargs = 1,
        help = help,
        hidden = hidden,
        envvar = envvar,
        envvarSplit = Regex("\\s+"),
        parser = OptionWithValuesParser,
        transformValue = { it },
        transformEach = defaultEachProcessor(),
        transformAll = defaultAllProcessor())

fun <AllT, EachT : Any, ValueT> NullableOption<EachT, ValueT>.transformAll(transform: CallsTransformer<AllT, EachT>)
        : OptionWithValues<AllT, EachT, ValueT> {
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs,
            help, hidden, envvar, envvarSplit, parser, transformValue, transformEach, transform)
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.default(value: EachT)
        : OptionWithValues<EachT, EachT, ValueT> {
    return transformAll { it.lastOrNull() ?: value }
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.required()
        : OptionWithValues<EachT, EachT, ValueT> {
    return transformAll { it.lastOrNull() ?: throw MissingParameter(option) }
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.multiple()
        : OptionWithValues<List<EachT>, EachT, ValueT> = transformAll { it }

fun <EachInT : Any, EachOutT : Any, ValueT> NullableOption<EachInT, ValueT>.transformNargs(
        nargs: Int, transform: ArgsTransformer<EachOutT, ValueT>): NullableOption<EachOutT, ValueT> {
    require(nargs != 0) { "Cannot set nargs = 0. Use flag() instead." }
    require(nargs > 0) { "Options cannot have nargs < 0" }
    require(nargs > 1) { "Cannot set nargs = 1. Use convert() instead." }
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs, help, hidden,
            envvar, envvarSplit, parser, transformValue, transform, defaultAllProcessor())
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.paired()
        : NullableOption<Pair<ValueT, ValueT>, ValueT> {
    return transformNargs(nargs = 2) { it[0] to it[1] }
}

fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.triple()
        : NullableOption<Triple<ValueT, ValueT, ValueT>, ValueT> {
    return transformNargs(nargs = 3) { Triple(it[0], it[1], it[2]) }
}

fun <AllT, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.validate(
        validator: OptionValidator<AllT>): OptionDelegate<AllT> {
    return OptionWithValues(names, explicitMetavar, defaultMetavar, nargs,
            help, hidden, envvar, envvarSplit, parser, transformValue, transformEach) {
        transformAll(it).also { validator(this, it) }
    }
}

inline fun <T : Any> RawOption.convert(metavar: String = "VALUE", envvarSplit: Regex? = null,
                                       crossinline conversion: ValueTransformer<T>): NullableOption<T, T> {
    val proc: ValueTransformer<T> = {
        try {
            conversion(it)
        } catch (err: UsageError) {
            err.paramName = name
            throw err
        } catch (err: Exception) {
            fail(err.message ?: "")
        }
    }
    return OptionWithValues(names, explicitMetavar, metavar, nargs, help, hidden, envvar,
            envvarSplit ?: this.envvarSplit, parser, proc, defaultEachProcessor(), defaultAllProcessor())
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
            val ctx = OptionCallTransformContext("", this)
            transformAll(listOf(transformEach(ctx, listOf(transformValue(ctx, it)))))
        } ?: throw Abort()
    }
}
