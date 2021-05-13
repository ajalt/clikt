package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.mpp.isLetterOrDigit
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parsers.OptionParser
import com.github.ajalt.clikt.sources.ValueSource
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * An optional command line parameter that takes a fixed number of values.
 *
 * Options can take any fixed number of values, including 0.
 */
interface Option {
    /** A name representing the values for this option that can be displayed to the user. */
    fun metavar(context: Context): String?

    /** The description of this option, usually a single line. */
    val optionHelp: String

    /** The parser for this option's values. */
    val parser: OptionParser

    /** The names that can be used to invoke this option. They must start with a punctuation character. */
    val names: Set<String>

    /** Names that can be used for a secondary purpose, like disabling flag options. */
    val secondaryNames: Set<String>

    /** The number of values that must be given to this option. */
    val nvalues: Int

    /** If true, this option should not appear in help output. */
    val hidden: Boolean

    /** Extra information about this option to pass to the help formatter. */
    val helpTags: Map<String, String>

    /** Optional set of strings to use when the user invokes shell autocomplete on a value for this option. */
    val completionCandidates: CompletionCandidates get() = CompletionCandidates.None

    /** Optional explicit key to use when looking this option up from a [ValueSource] */
    val valueSourceKey: String?

    /** Information about this option for the help output. */
    fun parameterHelp(context: Context): HelpFormatter.ParameterHelp.Option? = when {
        hidden -> null
        else -> HelpFormatter.ParameterHelp.Option(names, secondaryNames, metavar(context), optionHelp, nvalues, helpTags,
                groupName = (this as? StaticallyGroupedOption)?.groupName
                        ?: (this as? GroupableOption)?.parameterGroup?.groupName
        )
    }

    /**
     * Called after this command's argv is parsed to transform and store the option's value.
     *
     * You cannot refer to other parameter values during this call, since they might not have been
     * finalized yet.
     *
     * @param context The context for this parse
     * @param invocations A possibly empty list of invocations of this option.
     */
    fun finalize(context: Context, invocations: List<OptionParser.Invocation>)

    /**
     * Called after all of a command's parameters have been [finalize]d to perform validation of the final value.
     */
    fun postValidate(context: Context)
}

/** An option that functions as a property delegate */
interface OptionDelegate<T> : GroupableOption, ReadOnlyProperty<ParameterHolder, T> {
    /**
     * The value for this option.
     *
     * An exception should be thrown if this property is accessed before [finalize] is called.
     */
    val value: T

    /** Implementations must call [ParameterHolder.registerOption] */
    operator fun provideDelegate(thisRef: ParameterHolder, prop: KProperty<*>): ReadOnlyProperty<ParameterHolder, T>

    override fun getValue(thisRef: ParameterHolder, property: KProperty<*>): T = value
}

internal fun inferOptionNames(names: Set<String>, propertyName: String): Set<String> {
    if (names.isNotEmpty()) {
        val invalidName = names.find { !it.matches(Regex("""[\-@/+]{1,2}[\w\-_]+""")) }
        require(invalidName == null) { "Invalid option name \"$invalidName\"" }
        return names
    }
    val normalizedName = "--" + propertyName.replace(Regex("""[a-z][A-Z]""")) {
        "${it.value[0]}-${it.value[1]}"
    }.lowercase()
    return setOf(normalizedName)
}

internal fun inferEnvvar(names: Set<String>, envvar: String?, autoEnvvarPrefix: String?): String? {
    if (envvar != null) return envvar
    if (names.isEmpty() || autoEnvvarPrefix == null) return null
    val name = splitOptionPrefix(names.maxByOrNull { it.length }!!).second
    if (name.isEmpty()) return null
    return autoEnvvarPrefix + "_" + name.replace(Regex("\\W"), "_").uppercase()
}

/** Split an option token into a pair of prefix to simple name. */
internal fun splitOptionPrefix(name: String): Pair<String, String> =
        when {
            name.length < 2 || isLetterOrDigit(name[0]) -> "" to name
            name.length > 2 && name[0] == name[1] -> name.slice(0..1) to name.substring(2)
            else -> name.substring(0, 1) to name.substring(1)
        }

internal fun <EachT, AllT> deprecationTransformer(
        message: String? = "",
        error: Boolean = false,
        transformAll: CallsTransformer<EachT, AllT>
): CallsTransformer<EachT, AllT> = {
    if (it.isNotEmpty()) {
        val msg = when (message) {
            null -> ""
            "" -> "${if (error) "ERROR" else "WARNING"}: option ${option.longestName()} is deprecated"
            else -> message
        }
        if (error) {
            throw CliktError(msg)
        } else if (message != null) {
            message(msg)
        }
    }
    transformAll(it)
}

internal fun Option.longestName(): String? = names.maxByOrNull { it.length }

internal sealed class FinalValue {
    data class Parsed(val values: List<OptionParser.Invocation>) : FinalValue()
    data class Sourced(val values: List<ValueSource.Invocation>) : FinalValue()
    data class Envvar(val key: String, val value: String) : FinalValue()
}

internal fun Option.getFinalValue(
        context: Context,
        invocations: List<OptionParser.Invocation>,
        envvar: String?
): FinalValue {
    return when {
        invocations.isNotEmpty() -> FinalValue.Parsed(invocations)
        context.readEnvvarBeforeValueSource -> {
            readEnvVar(context, envvar) ?: readValueSource(context)
        }
        else -> {
            readValueSource(context) ?: readEnvVar(context, envvar)
        }
    } ?: FinalValue.Parsed(emptyList())
}

private fun Option.readValueSource(context: Context): FinalValue? {
    return context.valueSource?.getValues(context, this)?.ifEmpty { null }
            ?.let { FinalValue.Sourced(it) }
}

private fun Option.readEnvVar(context: Context, envvar: String?): FinalValue? {
    val env = inferEnvvar(names, envvar, context.autoEnvvarPrefix) ?: return null
    return readEnvvar(env)?.let { FinalValue.Envvar(env, it) }
}
