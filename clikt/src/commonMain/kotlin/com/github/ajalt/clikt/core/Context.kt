package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.*
import com.github.ajalt.clikt.sources.ChainedValueSource
import com.github.ajalt.clikt.sources.ValueSource
import kotlin.properties.ReadOnlyProperty

typealias TypoSuggestor = (enteredValue: String, possibleValues: List<String>) -> List<String>

/**
 * A object used to control command line parsing and pass data between commands.
 *
 * A new Context instance is created for each command each time the command line is parsed.
 *
 * @property parent If this context is the child of another command, [parent] is the parent command's context.
 * @property command The command that this context associated with.
 * @property allowInterspersedArgs If false, options and arguments cannot be mixed; the first time an argument is
 *   encountered, all remaining tokens are parsed as arguments.
 * @property autoEnvvarPrefix The prefix to add to inferred envvar names. If null, the prefix is based on the
 *   parent's prefix, if there is one. If no command specifies, a prefix, envvar lookup is disabled.
 * @property printExtraMessages Set this to false to prevent extra messages from being printed automatically.
 *   You can still access them at [CliktCommand.messages] inside of [CliktCommand.run].
 * @property helpOptionNames The names to use for the help option. If any names in the set conflict with other
 *   options, the conflicting name will not be used for the help option. If the set is empty, or contains no
 *   unique names, no help option will be added.
 * @property helpFormatter The help formatter for this command.
 * @property tokenTransformer An optional transformation function that is called to transform command line
 *   tokens (options and commands) before parsing. This can be used to implement e.g. case insensitive
 *   behavior.
 * @property console The console to use to print messages.
 * @property expandArgumentFiles If true, arguments starting with `@` will be expanded as argument
 *   files. If false, they will be treated as normal arguments.
 * @property correctionSuggestor A callback called when the command line contains an invalid option or
 *   subcommand name. It takes the entered name and a list of all registered names option/subcommand
 *   names and filters the list down to values to suggest to the user.
 */
class Context(
        val parent: Context?,
        val command: CliktCommand,
        val allowInterspersedArgs: Boolean,
        val autoEnvvarPrefix: String?,
        val printExtraMessages: Boolean,
        val helpOptionNames: Set<String>,
        val helpFormatter: HelpFormatter,
        val tokenTransformer: Context.(String) -> String,
        val console: CliktConsole,
        val expandArgumentFiles: Boolean,
        val readEnvvarBeforeValueSource: Boolean,
        val valueSource: ValueSource?,
        val correctionSuggestor: TypoSuggestor,
        val localization: Localization
) {
    var invokedSubcommand: CliktCommand? = null
        internal set
    var obj: Any? = null

    /** Find the closest object of type [T] */
    inline fun <reified T : Any> findObject(): T? {
        return ancestors().mapNotNull { it.obj as? T }.firstOrNull()
    }

    /** Find the closest object of type [T], setting `this.`[obj] if one is not found. */
    inline fun <reified T : Any> findOrSetObject(defaultValue: () -> T): T {
        return findObject() ?: defaultValue().also { obj = it }
    }

    /** Find the outermost context */
    fun findRoot(): Context {
        var ctx = this
        while (ctx.parent != null) {
            ctx = ctx.parent!!
        }
        return ctx
    }

    /** Return a list of command names, starting with the topmost command and ending with this Context's parent. */
    fun parentNames(): List<String> {
        return ancestors().drop(1)
                .map { it.command.commandName }
                .toList().asReversed()
    }

    /** Return a list of command names, starting with the topmost command and ending with this Context's command. */
    fun commandNameWithParents(): List<String> {
        return parentNames() + command.commandName
    }

    /** Throw a [UsageError] with the given message */
    fun fail(message: String = ""): Nothing = throw UsageError(message)

    @PublishedApi
    internal fun ancestors() = generateSequence(this) { it.parent }

    class Builder(command: CliktCommand, parent: Context? = null) {
        /**
         * If false, options and arguments cannot be mixed; the first time an argument is encountered, all
         * remaining tokens are parsed as arguments.
         */
        var allowInterspersedArgs: Boolean = parent?.allowInterspersedArgs ?: true

        /**
         * Set this to false to prevent extra messages from being printed automatically.
         *
         * You can still access them at [CliktCommand.messages] inside of [CliktCommand.run].
         */
        var printExtraMessages: Boolean = parent?.printExtraMessages ?: true

        /**
         * The names to use for the help option.
         *
         * If any names in the set conflict with other options, the conflicting name will not be used for the
         * help option. If the set is empty, or contains no unique names, no help option will be added.
         */
        var helpOptionNames: Set<String> = parent?.helpOptionNames ?: setOf("-h", "--help")

        /** The help formatter for this command, or null to use the default */
        var helpFormatter: HelpFormatter? = parent?.helpFormatter

        /** An optional transformation function that is called to transform command line */
        var tokenTransformer: Context.(String) -> String = parent?.tokenTransformer ?: { it }

        /**
         * The prefix to add to inferred envvar names.
         *
         * If null, the prefix is based on the parent's prefix, if there is one. If no command specifies, a
         * prefix, envvar lookup is disabled.
         */
        var autoEnvvarPrefix: String? = parent?.autoEnvvarPrefix?.let {
            it + "_" + command.commandName.replace(Regex("\\W"), "_").uppercase()
        }

        /**
         * The console that will handle reading and writing text.
         *
         * The default uses stdin and stdout.
         */
        var console: CliktConsole = parent?.console ?: defaultCliktConsole()

        /**
         * If true, arguments starting with `@` will be expanded as argument files. If false, they
         * will be treated as normal arguments.
         */
        var expandArgumentFiles: Boolean = parent?.expandArgumentFiles ?: true

        /**
         * If `false`,the [valueSource] is searched before environment variables.
         *
         * By default, environment variables will be searched for option values before the [valueSource].
         */
        var readEnvvarBeforeValueSource: Boolean = parent?.readEnvvarBeforeValueSource ?: true

        /**
         * The source that will attempt to read values for options that aren't present on the command line.
         *
         * You can set multiple sources with [valueSources]
         */
        var valueSource: ValueSource? = parent?.valueSource

        /**
         * Set multiple sources that will attempt to read values for options not present on the command line.
         *
         * Values are read from the first source, then if it doesn't return a value, later sources
         * are read successively until one returns a value or all sources have been read.
         */
        fun valueSources(vararg sources: ValueSource) {
            valueSource = ChainedValueSource(sources.toList())
        }

        /**
         * A callback called when the command line contains an invalid option or
         * subcommand name. It takes the entered name and a list of all registered names option/subcommand
         * names and filters the list down to values to suggest to the user.
         */
        var correctionSuggestor: TypoSuggestor = DEFAULT_CORRECTION_SUGGESTOR

        /**
         * Localized strings to use for help output and error reporting.
         */
        var localization: Localization = defaultLocalization
    }

    companion object {
        fun build(command: CliktCommand, parent: Context? = null, block: Builder.() -> Unit): Context {
            with(Builder(command, parent)) {
                block()
                val interspersed = allowInterspersedArgs && !command.allowMultipleSubcommands &&
                        parent?.let { p -> p.ancestors().any { it.command.allowMultipleSubcommands } } != true
                val formatter = helpFormatter ?: CliktHelpFormatter(localization)
                return Context(
                        parent, command, interspersed, autoEnvvarPrefix, printExtraMessages,
                        helpOptionNames, formatter, tokenTransformer, console, expandArgumentFiles,
                        readEnvvarBeforeValueSource, valueSource, correctionSuggestor, localization
                )
            }
        }
    }
}

/** Find the closest object of type [T], or throw a [NullPointerException] */
@Suppress("unused") // these extensions don't use their receiver, but we want to limit where they can be called
inline fun <reified T : Any> CliktCommand.requireObject(): ReadOnlyProperty<CliktCommand, T> {
    return ReadOnlyProperty<CliktCommand, T> { thisRef, _ -> thisRef.currentContext.findObject<T>()!! }
}

/** Find the closest object of type [T], or null */
@Suppress("unused")
inline fun <reified T : Any> CliktCommand.findObject(): ReadOnlyProperty<CliktCommand, T?> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findObject<T>() }
}

/**
 * Find the closest object of type [T], setting `context.obj` if one is not found.
 *
 * Note that this function returns a delegate, and so the object will not be set on the context
 * until the delegated property's value is accessed. If you want to set a value for subcommands
 * without accessing the property, call [Context.findOrSetObject] in your [run][CliktCommand.run]
 * function instead.
 */
@Suppress("unused")
inline fun <reified T : Any> CliktCommand.findOrSetObject(crossinline default: () -> T): ReadOnlyProperty<CliktCommand, T> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findOrSetObject(default) }
}

private val DEFAULT_CORRECTION_SUGGESTOR: TypoSuggestor = { enteredValue, possibleValues ->
    possibleValues.map { it to jaroWinklerSimilarity(enteredValue, it) }
            .filter { it.second > 0.8 }
            .sortedByDescending { it.second }
            .map { it.first }
}
