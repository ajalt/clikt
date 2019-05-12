package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * An optional command line parameter that takes a fixed number of values.
 *
 * Options can take any fixed number of values, including 0.
 */
interface Option {
    /** A name representing the values for this option that can be displayed to the user. */
    val metavar: String?

    /** The description of this option, usually a single line. */
    val help: String

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

    /** Information about this option for the help output. */
    val parameterHelp: HelpFormatter.ParameterHelp.Option?
        get() = when {
            hidden -> null
            else -> HelpFormatter.ParameterHelp.Option(names, secondaryNames, metavar, help, nvalues, helpTags)
        }

    /**
     * Called after this command's argv is parsed to transform and store the option's value.
     *
     * @param context The context for this parse
     * @param invocations A possibly empty list of invocations of this option.
     */
    fun finalize(context: Context, invocations: List<OptionParser.Invocation>)
}

/** An option that functions as a property delegate */
interface OptionDelegate<out T> : Option, ReadOnlyProperty<CliktCommand, T> {
    /** Implementations must call [CliktCommand.registerOption] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

internal fun inferOptionNames(names: Set<String>, propertyName: String): Set<String> {
    if (names.isNotEmpty()) {
        val invalidName = names.find { !it.matches(Regex("\\p{Punct}{1,2}[\\w-_]+")) }
        require(invalidName == null) { "Invalid option name \"$invalidName\"" }
        return names
    }
    val normalizedName = propertyName.split(Regex("(?<=[a-z])(?=[A-Z])"))
            .joinToString("-", prefix = "--") { it.toLowerCase() }
    return setOf(normalizedName)
}

internal fun inferEnvvar(names: Set<String>, envvar: String?, autoEnvvarPrefix: String?): String? {
    if (envvar != null) return envvar
    if (names.isEmpty() || autoEnvvarPrefix == null) return null
    val name = splitOptionPrefix(names.maxBy { it.length }!!).second
    if (name.isEmpty()) return null
    return autoEnvvarPrefix + "_" + name.replace(Regex("\\W"), "_").toUpperCase()
}

/** Split an option token into a pair of prefix to simple name. */
internal fun splitOptionPrefix(name: String): Pair<String, String> =
        when {
            name.length < 2 || name[0].isLetterOrDigit() -> "" to name
            name.length > 2 && name[0] == name[1] -> name.slice(0..1) to name.substring(2)
            else -> name.slice(0..0) to name.substring(1)
        }

internal fun <EachT, AllT> deprecationTransformer(
        message: String? = "",
        error: Boolean = false,
        transformAll: CallsTransformer<EachT, AllT>
): CallsTransformer<EachT, AllT> = {
    if (it.isNotEmpty()) {
        val msg = when (message) {
            null -> ""
            "" -> "${if (error) "ERROR" else "WARNING"}: option ${option.names.maxBy { o -> o.length }} is deprecated"
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
