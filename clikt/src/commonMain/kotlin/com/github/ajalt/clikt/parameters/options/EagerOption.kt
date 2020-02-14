package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parsers.FlagOptionParser
import com.github.ajalt.clikt.parsers.OptionParser

/**
 * An [Option] with no values that is [finalize]d before other types of options.
 *
 * @param callback This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`Abort(error=false)`][Abort]. The callback is passed the current execution context as a
 *   parameter.
 */
class EagerOption(
        override val names: Set<String>,
        override val nvalues: Int,
        override val help: String,
        override val hidden: Boolean,
        override val helpTags: Map<String, String>,
        private val callback: OptionTransformContext.() -> Unit) : Option {
    constructor(vararg names: String, nvalues: Int = 0, help: String = "", hidden: Boolean = false,
                helpTags: Map<String, String> = emptyMap(), callback: OptionTransformContext.() -> Unit)
            : this(names.toSet(), nvalues, help, hidden, helpTags, callback)

    init {
        require(names.isNotEmpty()) { "options must have at least one name" }
    }

    override val secondaryNames: Set<String> get() = emptySet()
    override val parser: OptionParser = FlagOptionParser
    override val metavar: String? get() = null
    override fun postValidate(context: Context) {}
    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        this.callback(OptionTransformContext(this, context))
    }
}

internal fun helpOption(names: Set<String>, message: String) = EagerOption(names, 0, message, false, emptyMap(),
        callback = { throw PrintHelpMessage(context.command) })

/**
 * Add an eager option to this command that, when invoked, runs [action].
 *
 * @param action This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`Abort(error=false)`][Abort]. The callback is passed the current execution context as a
 *   parameter.
 */
fun <T : CliktCommand> T.eagerOption(
        name: String,
        vararg additionalNames: String,
        help: String = "",
        hidden: Boolean = false,
        helpTags: Map<String, String> = emptyMap(),
        action: OptionTransformContext.() -> Unit
): T = eagerOption(listOf(name) + additionalNames, help, hidden, helpTags, action)

/**
 * Add an eager option to this command that, when invoked, runs [action].
 *
 * @param action This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`Abort(error=false)`][Abort]. The callback is passed the current execution context as a
 *   parameter.
 */
fun <T : CliktCommand> T.eagerOption(
        names: Collection<String>,
        help: String = "",
        hidden: Boolean = false,
        helpTags: Map<String, String> = emptyMap(),
        action: OptionTransformContext.() -> Unit
): T = apply { registerOption(EagerOption(names.toSet(), 0, help, hidden, helpTags, action)) }

/** Add an eager option to this command that, when invoked, prints a version message and exits. */
inline fun <T : CliktCommand> T.versionOption(
        version: String,
        help: String = "Show the version and exit",
        names: Set<String> = setOf("--version"),
        crossinline message: (String) -> String = { "$commandName version $it" }
): T = eagerOption(names, help) { throw PrintMessage(message(version)) }
