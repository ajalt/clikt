package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import com.github.ajalt.clikt.output.defaultLocalization
import com.github.ajalt.clikt.sources.ChainedValueSource
import com.github.ajalt.clikt.sources.ValueSource
import kotlin.properties.ReadOnlyProperty

typealias TypoSuggestor = (enteredValue: String, possibleValues: List<String>) -> List<String>
typealias MessageEchoer = (context: Context, message: Any?, trailingNewline: Boolean, err: Boolean) -> Unit

/**
 * An object used to control command line parsing and pass data between commands.
 *
 * A new Context instance is created for each command each time the command line is parsed.
 */
class Context private constructor(
    /**
     * If this context is the child of another command, [parent] is the parent command's context.
     */
    val parent: Context?,
    /**
     * The command that this context associated with.
     */
    val command: BaseCliktCommand<*>,
    /**
     * If false, options and arguments cannot be mixed; the first time an argument is
     *   encountered, all remaining tokens are parsed as arguments.
     */
    val allowInterspersedArgs: Boolean,
    /**
     * If true, short options can be grouped after a single `-` prefix.
     */
    val allowGroupedShortOptions: Boolean,
    /**
     * The prefix to add to inferred envvar names. If null, the prefix is based on the
     *   parent's prefix, if there is one. If no command specifies, a prefix, envvar lookup is disabled.
     */
    val autoEnvvarPrefix: String?,
    /**
     * Set this to false to prevent extra messages from being printed automatically.
     * You can still access them at [messages][BaseCliktCommand.messages] inside of
     * `CliktCommand.run`.
     */
    val printExtraMessages: Boolean,
    /**
     * The names to use for the help option. If any names in the set conflict with other options,
     * the conflicting name will not be used for the help option. If the set is empty, or contains
     * no unique names, no help option will be added.
     */
    val helpOptionNames: Set<String>,
    /**
     * The help formatter for this command.
     */
    val helpFormatter: (Context) -> HelpFormatter,
    /**
     * An optional transformation function that is called to transform command line tokens (options
     * and commands) before parsing. This can be used to implement e.g. case-insensitive behavior.
     */
    val transformToken: Context.(String) -> String,

    /**
     * A block that returns the content of an argument file for a given filename.
     */
    var readArgumentFile: ((filename: String) -> String)?,
    /**
     * If `false`,the [valueSource] is searched before environment variables.
     *
     * By default, environment variables will be searched for option values before the
     * [valueSource].
     */
    val readEnvvarBeforeValueSource: Boolean,
    /**
     * The source that will attempt to read values for options that aren't present on the
     * command line.
     */
    val valueSource: ValueSource?,
    /**
     * A callback called when the command line contains an invalid option or subcommand name. It
     * takes the entered name and a list of all registered names option/subcommand names and filters
     * the list down to values to suggest to the user.
     */
    val suggestTypoCorrection: TypoSuggestor,
    /**
     * Localized strings to use for help output and error reporting.
     */
    val localization: Localization,
    /**
     * A function called by Clikt to get a parameter value from a given environment variable
     *
     * The function returns `null` if the envvar is not defined.
     *
     * You can set this to read from a map or other source during tests.
     */
    val readEnvvar: (String) -> String?,
    /**
     * A map holding arbitrary data on the context.
     *
     * Values on this object can be retrieved with functions [findOrSetObject] and [requireObject].
     * You can also set the values on the context itself after it's been constructed.
     */
    val data: MutableMap<String, Any?>,

    /**
     * The callable to call to echo output.
     */
    val echoMessage: MessageEchoer,

    /**
     * The function to call to exit the process with a status code.
     *
     * You can set this in tests to avoid actually exiting the process.
     */
    val exitProcess: (status: Int) -> Unit,
) {
    /**
     * All invoked subcommands, in the order they were invoked.
     *
     * If `allowMultipleSubcommands=false`, this will have at most one entry. If
     * `allowMultipleSubcommands=true`, the same command can appear in the list more than once.
     */
    var invokedSubcommands: List<BaseCliktCommand<*>> = emptyList()
        internal set

    /**
     * If this command has subcommands and one of them was invoked, this is the subcommand that will
     * be run first.
     *
     * If `allowMultipleSubcommands=true` on this command, you can use [invokedSubcommands] to get
     * all invoked subcommands.
     */
    val invokedSubcommand: BaseCliktCommand<*>? get() = invokedSubcommands.firstOrNull()

    /**
     * If true, an error was previously encountered while parsing the command line, but parsing is
     * continuing to collect any more errors into a [MultiUsageError].
     *
     * If you want to skip option conversion and validation after an error is encountered, you can
     * throw [Abort].
     */
    var errorEncountered: Boolean = false
        internal set

    private val closeables = mutableListOf<() -> Unit>()

    /** Find the closest object with [key] of type [T] */
    inline fun <reified T : Any> findObject(key: String = DEFAULT_OBJ_KEY): T? {
        return selfAndAncestors().mapNotNull { it.data[key] as? T }.firstOrNull()
    }

    /** Find the closest object with [key] of type [T], setting it on `this` if one is not found. */
    inline fun <reified T : Any> findOrSetObject(
        key: String = DEFAULT_OBJ_KEY,
        defaultValue: () -> T,
    ): T {
        return findObject(key) ?: defaultValue().also { data[key] = it }
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
        return ancestors().map { it.command.commandName }.toList().asReversed()
    }

    /** Return a list of command names, starting with the topmost command and ending with this Context's command. */
    fun commandNameWithParents(): List<String> {
        return parentNames() + command.commandName
    }

    /** Throw a [UsageError] with the given message */
    fun fail(message: String = ""): Nothing = throw UsageError(message)

    /**
     * Register a callback to be called when this command and all its subcommands have finished.
     *
     * This is useful for resources that need to be shared across multiple commands.
     *
     * If your resource implements [AutoCloseable], you should use [registerCloseable] instead.
     *
     * ### Example
     *
     * ```
     * currentContext.callOnClose { myResource.close() }
     * ```
     */
    fun callOnClose(closeable: () -> Unit) {
        closeables.add(closeable)
    }

    /**
     * Close all registered closeables in the reverse order they were registered.
     *
     * This is called automatically after a command and its subcommands have finished running.
     */
    fun close() {
        var err: Throwable? = null
        for (c in closeables.asReversed()) {
            try {
                c()
            } catch (e: Throwable) {
                if (err == null) err = e
                else err.addSuppressed(e)
            }
        }
        closeables.clear()
        if (err != null) throw err
    }

    /**
     * If true, arguments starting with `@` will be expanded as argument files. If false, they
     * will be treated as normal arguments.
     */
    val expandArgumentFiles: Boolean get() = readArgumentFile != null

    /**
     * The original command line arguments.
     */
    @Deprecated("This property is deprecated and will be removed in the future. It will now always return an empty list. If your commands need an argv, you can pass it to them before they are run.")
    val originalArgv: List<String> = emptyList()

    @Suppress("unused")
    @Deprecated("Renamed to transformToken", ReplaceWith("transformToken"))
    val tokenTransformer: Context.(String) -> String
        get() = transformToken

    @Suppress("unused")
    @Deprecated("Renamed to readArgumentFile", ReplaceWith("readArgumentFile"))
    val argumentFileReader: ((filename: String) -> String)?
        get() = readArgumentFile

    @Deprecated("Renamed to suggestTypoCorrection", ReplaceWith("suggestTypoCorrection"))
    val correctionSuggestor: TypoSuggestor
        get() = suggestTypoCorrection

    override fun toString(): String {
        return "Context(command=${command.commandName}, parent=${parent?.command?.commandName})"
    }

    class Builder(command: BaseCliktCommand<*>, val parent: Context? = null) {
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
         * You can still access them at [messages][BaseCliktCommand.messages] inside of
         * `CliktCommand.run`.
         */
        var printExtraMessages: Boolean = parent?.printExtraMessages ?: true

        /**
         * The names to use for the help option.
         *
         * If any names in the set conflict with other options, the conflicting name will not be used for the
         * help option. If the set is empty, or contains no unique names, no help option will be added.
         */
        var helpOptionNames: Iterable<String> = parent?.helpOptionNames ?: setOf("-h", "--help")

        /** A lambda returning the help formatter for this command, or null to use the default */
        var helpFormatter: ((Context) -> HelpFormatter)? = parent?.helpFormatter

        /** An optional transformation function that is called to transform command line */
        var transformToken: Context.(String) -> String = parent?.transformToken ?: { it }

        @Suppress("unused")
        @Deprecated("Renamed to transformToken", ReplaceWith("transformToken"))
        var tokenTransformer: Context.(String) -> String
            get() = transformToken
            set(value) {
                transformToken = value
            }

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
         * If true, arguments starting with `@` will be expanded as argument files. If false, they
         * will be treated as normal arguments.
         */
        @Suppress("unused", "UNUSED_PARAMETER")
        @Deprecated("This property is deprecated and will be removed in the future. Setting it no longer has any effect. Set readArgumentFile instead.")
        var expandArgumentFiles: Boolean
            get() = readArgumentFile == null
            set(value) {}

        /**
         * A block that returns the content of an argument file for a given filename.
         *
         * If set to null, arguments starting with `@` will be treated as normal arguments.
         *
         * The block should throw [FileNotFound] if the given `filename` cannot be read.
         */
        var readArgumentFile: ((filename: String) -> String)? = parent?.readArgumentFile

        @Suppress("unused")
        @Deprecated("Renamed to readArgumentFile", ReplaceWith("readArgumentFile"))
        var argumentFileReader: ((filename: String) -> String)?
            get() = readArgumentFile
            set(value) {
                readArgumentFile = value
            }

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
        var suggestTypoCorrection: TypoSuggestor = DEFAULT_CORRECTION_SUGGESTOR

        @Deprecated("Renamed to suggestTypoCorrection", ReplaceWith("suggestTypoCorrection"))
        var correctionSuggestor: TypoSuggestor
            get() = suggestTypoCorrection
            set(value) {
                suggestTypoCorrection = value
            }


        /**
         * Localized strings to use for help output and error reporting.
         */
        var localization: Localization = parent?.localization ?: defaultLocalization

        /**
         * A function called by Clikt to get a parameter value from a given environment variable
         *
         * The function returns `null` if the envvar is not defined.
         *
         * You can set this to read from a map or other source during tests.
         */
        var readEnvvar: (key: String) -> String? = parent?.readEnvvar ?: { null }

        @Suppress("unused")
        @Deprecated("Renamed to readEnvvar", ReplaceWith("readEnvvar"))
        var envvarReader: (key: String) -> String?
            get() = readEnvvar
            set(value) {
                readEnvvar = value
            }

        /**
         * A map holding arbitrary data on the context.
         *
         * Values on this object can be retrieved with functions [findOrSetObject] and [requireObject].
         * You can also set the values on the context itself after it's been constructed.
         */
        val data: MutableMap<String, Any?> = parent?.data ?: mutableMapOf()

        /**
         * The function to call to echo messages when `CliktCommand.echo` is called.
         */
        var echoMessage: MessageEchoer = parent?.echoMessage ?: DefaultMessageEchoer

        /**
         * The function to call to exit the process with a status code.
         *
         * You can set this in tests to avoid actually exiting the process.
         */
        var exitProcess: (status: Int) -> Unit = parent?.exitProcess ?: {}
    }

    companion object {
        /** The key in [data] that [obj] is stored at. */
        const val DEFAULT_OBJ_KEY = "default_object"

        /** The key in [data] that the mordant terminal is stored at. */
        const val TERMINAL_KEY = "mordant_terminal"

        internal fun build(
            command: BaseCliktCommand<*>,
            parent: Context?,
            block: Builder.() -> Unit,
        ): Context {
            with(Builder(command, parent)) {
                block()
                val interspersed =
                    allowInterspersedArgs && !command.allowMultipleSubcommands && parent?.let { p ->
                        p.selfAndAncestors().any { it.command.allowMultipleSubcommands }
                    } != true
                return Context(
                    parent = parent,
                    command = command,
                    allowInterspersedArgs = interspersed,
                    allowGroupedShortOptions = allowGroupedShortOptions,
                    autoEnvvarPrefix = autoEnvvarPrefix,
                    printExtraMessages = printExtraMessages,
                    helpOptionNames = helpOptionNames.toSet(),
                    helpFormatter = helpFormatter ?: { PlaintextHelpFormatter(it) },
                    transformToken = transformToken,
                    readArgumentFile = readArgumentFile,
                    readEnvvarBeforeValueSource = readEnvvarBeforeValueSource,
                    valueSource = valueSource,
                    suggestTypoCorrection = suggestTypoCorrection,
                    localization = localization,
                    readEnvvar = readEnvvar,
                    data = data,
                    echoMessage = echoMessage,
                    exitProcess = exitProcess,
                )
            }
        }
    }
}

/**
 * Register an [AutoCloseable] to be closed when this command and all its subcommands have
 * finished running.
 *
 * This is useful for resources that need to be shared across multiple commands. For resources
 * that aren't shared, it's often simpler to use [use] directly.
 *
 * Registered closeables will be closed in the reverse order that they were registered.
 *
 * ### Example
 *
 * ```
 * currentContext.obj = currentContext.registerCloseable(MyResource())
 * ```
 *
 * @return the closeable that was registered
 * @see Context.callOnClose
 */
fun <T : AutoCloseable> Context.registerCloseable(closeable: T): T {
    callOnClose { closeable.close() }
    return closeable
}

/** Find the closest object of type [T], or throw a [NullPointerException] */
@Suppress("UnusedReceiverParameter") // these extensions don't use their receiver, but we want to limit where they can be called
inline fun <reified T : Any> BaseCliktCommand<*>.requireObject(
    key: String = Context.DEFAULT_OBJ_KEY,
): ReadOnlyProperty<BaseCliktCommand<*>, T> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findObject(key)!! }
}

/** Find the closest object of type [T], or null */
@Suppress("UnusedReceiverParameter")
inline fun <reified T : Any> BaseCliktCommand<*>.findObject(
    key: String = Context.DEFAULT_OBJ_KEY,
): ReadOnlyProperty<BaseCliktCommand<*>, T?> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findObject(key) }
}

/**
 * Find the closest object of type [T], setting `context.obj` if one is not found.
 *
 * Note that this function returns a delegate, and so the object will not be set on the context
 * until the delegated property's value is accessed. If you want to set a value for subcommands
 * without accessing the property, call [Context.findOrSetObject] in your `run`
 * function instead.
 */
@Suppress("UnusedReceiverParameter")
inline fun <reified T : Any> BaseCliktCommand<*>.findOrSetObject(
    key: String = Context.DEFAULT_OBJ_KEY,
    crossinline default: () -> T,
): ReadOnlyProperty<BaseCliktCommand<*>, T> {
    return ReadOnlyProperty { thisRef, _ -> thisRef.currentContext.findOrSetObject(key, default) }
}


/**
 * Set an arbitrary object on the context.
 *
 * This object can be retrieved with functions [findOrSetObject] and [requireObject].
 *
 * You can set more than one object on the context with [data][Context.data]
 */
var Context.obj: Any?
    get() = data[Context.DEFAULT_OBJ_KEY]
    set(value) {
        data[Context.DEFAULT_OBJ_KEY] = value
    }

/**
 * Set an arbitrary object on the context.
 *
 * This object can be retrieved with functions [findOrSetObject] and [requireObject]. You
 * can also set the object on the context itself after it's been constructed.
 *
 * You can set more than one object on the context with [data][Context.data]
 */
var Context.Builder.obj: Any?
    get() = data[Context.DEFAULT_OBJ_KEY]
    set(value) {
        data[Context.DEFAULT_OBJ_KEY] = value
    }

private val DEFAULT_CORRECTION_SUGGESTOR: TypoSuggestor = { enteredValue, possibleValues ->
    possibleValues.map { it to jaroWinklerSimilarity(enteredValue, it) }.filter { it.second > 0.8 }
        .sortedByDescending { it.second }.map { it.first }
}

@PublishedApi
internal fun Context.selfAndAncestors() = generateSequence(this) { it.parent }
internal fun Context.ancestors() = generateSequence(parent) { it.parent }

private val DefaultMessageEchoer: MessageEchoer =
    { _: Context, message: Any?, trailingNewline: Boolean, _: Boolean ->
        if (trailingNewline) {
            println(message)
        } else {
            print(message)
        }
    }
