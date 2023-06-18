package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.internal.defaultArgFileReader
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.output.*
import com.github.ajalt.clikt.sources.ChainedValueSource
import com.github.ajalt.clikt.sources.ValueSource
import com.github.ajalt.mordant.terminal.Terminal
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
 * @property terminal The terminal to used to read and write messages.
 * @property argumentFileReader A block that returns the content of an argument file for a given filename.
 * @property correctionSuggestor A callback called when the command line contains an invalid option or
 *   subcommand name. It takes the entered name and a list of all registered names option/subcommand
 *   names and filters the list down to values to suggest to the user.
 * @property allowGroupedShortOptions If true, short options can be grouped after a single `-` prefix.
 */

class Context private constructor(
    val parent: Context?,
    val command: CliktCommand,
    val allowInterspersedArgs: Boolean,
    val allowGroupedShortOptions: Boolean,
    val autoEnvvarPrefix: String?,
    val printExtraMessages: Boolean,
    val helpOptionNames: Set<String>,
    val helpFormatter: HelpFormatter,
    val tokenTransformer: Context.(String) -> String,
    val terminal: Terminal,
    var argumentFileReader: ((filename: String) -> String)?,
    val readEnvvarBeforeValueSource: Boolean,
    val valueSource: ValueSource?,
    val correctionSuggestor: TypoSuggestor,
    val localization: Localization,
    val readEnvvar: (String) -> String?,
    var obj: Any?,
    val originalArgv: List<String>,
) {
    var invokedSubcommand: CliktCommand? = null
        internal set

    /** Find the closest object of type [T] */
    inline fun <reified T : Any> findObject(): T? {
        return selfAndAncestors().mapNotNull { it.obj as? T }.firstOrNull()
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
        return ancestors()
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
    internal fun ancestors() = generateSequence(parent) { it.parent }

    @PublishedApi
    internal fun selfAndAncestors() = generateSequence(this) { it.parent }

    /**
     * If true, arguments starting with `@` will be expanded as argument files. If false, they
     * will be treated as normal arguments.
     */
    val expandArgumentFiles: Boolean get() = argumentFileReader != null


    class Builder(command: CliktCommand, val parent: Context? = null) {
        /**
         * If false, options and arguments cannot be mixed; the first time an argument is encountered, all
         * remaining tokens are parsed as arguments.
         */
        var allowInterspersedArgs: Boolean = parent?.allowInterspersedArgs ?: true

        /**
         * If true, short options can be grouped together after a single `-`.
         *
         * For example, `-abc` is equivalent to `-a -b -c`. Set to false to disable this behavior.
         */
        var allowGroupedShortOptions: Boolean = true

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
        var helpOptionNames: Iterable<String> = parent?.helpOptionNames ?: setOf("-h", "--help")

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
         * The terminal that will handle reading and writing text.
         */
        var terminal: Terminal = parent?.terminal ?: Terminal()

        /**
         * If true, arguments starting with `@` will be expanded as argument files. If false, they
         * will be treated as normal arguments.
         */
        var expandArgumentFiles: Boolean
            get() = argumentFileReader == null
            set(value) {
                argumentFileReader = if (value) defaultArgFileReader else null
            }

        /**
         * A block that returns the content of an argument file for a given filename.
         *
         * If set to null, arguments starting with `@` will be treated as normal arguments.
         *
         * The block should throw [FileNotFound] if the given `filename` cannot be read.
         */
        var argumentFileReader: ((filename: String) -> String)? = defaultArgFileReader

        /**
         * If `false`,the [valueSource] is searched before environment variables.
         *
         * By default, environment variables will be searched for option values before the
         * [valueSource].
         */
        var readEnvvarBeforeValueSource: Boolean = parent?.readEnvvarBeforeValueSource ?: true

        /**
         * The source that will attempt to read values for options that aren't present on the
         * command line.
         *
         * You can set multiple sources with [valueSources]
         */
        var valueSource: ValueSource? = parent?.valueSource

        /**
         * Set multiple sources that will attempt to read values for options not present on the
         * command line.
         *
         * Values are read from the first source, then if it doesn't return a value, later sources
         * are read successively until one returns a value or all sources have been read.
         */
        fun valueSources(vararg sources: ValueSource) {
            valueSource = ChainedValueSource(sources.toList())
        }

        /**
         * A callback called when the command line contains an invalid option or subcommand name. It
         * takes the entered name and a list of all registered names option/subcommand names and
         * filters the list down to values to suggest to the user.
         */
        var correctionSuggestor: TypoSuggestor = DEFAULT_CORRECTION_SUGGESTOR

        /**
         * Localized strings to use for help output and error reporting.
         */
        var localization: Localization = defaultLocalization

        /**
         * A function called by Clikt to get a parameter value from a given environment variable
         *
         * The function returns `null` if the envvar is not defined.
         *
         * You can set this to read from a map or other source during tests.
         */
        var envvarReader: (key: String) -> String? = parent?.readEnvvar ?: ::readEnvvar

        /**
         * Set an arbitrary object on the context.
         *
         * This object can be retrieved with functions [findOrSetObject] and [requireObject]. You
         * can also set the object on the context itself after it's been constructed.
         */
        var obj: Any? = parent?.obj
    }

    companion object {
        internal fun build(
            command: CliktCommand,
            parent: Context?,
            argv: List<String>,
            block: Builder.() -> Unit,
        ): Context {
            with(Builder(command, parent)) {
                block()
                val interspersed = allowInterspersedArgs && !command.allowMultipleSubcommands &&
                        parent?.let { p ->
                            p.selfAndAncestors().any { it.command.allowMultipleSubcommands }
                        } != true
                val formatter = helpFormatter ?: MordantHelpFormatter()
                return Context(
                    parent,
                    command,
                    interspersed,
                    allowGroupedShortOptions,
                    autoEnvvarPrefix,
                    printExtraMessages,
                    helpOptionNames.toSet(),
                    formatter,
                    tokenTransformer,
                    terminal,
                    argumentFileReader,
                    readEnvvarBeforeValueSource,
                    valueSource,
                    correctionSuggestor,
                    localization,
                    envvarReader,
                    obj,
                    argv
                )
            }
        }
    }
}

/** Find the closest object of type [T], or throw a [NullPointerException] */
@Suppress("unused") // these extensions don't use their receiver, but we want to limit where they can be called
inline fun <reified T : Any> CliktCommand.requireObject(): ReadOnlyProperty<CliktCommand, T> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findObject()!! }
}

/** Find the closest object of type [T], or null */
@Suppress("unused")
inline fun <reified T : Any> CliktCommand.findObject(): ReadOnlyProperty<CliktCommand, T?> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findObject() }
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
