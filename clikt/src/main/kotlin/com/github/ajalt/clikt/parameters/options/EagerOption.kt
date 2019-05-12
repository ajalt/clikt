package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parsers.FlagOptionParser
import com.github.ajalt.clikt.parsers.OptionParser

/**
 * An [Option] with no values that is [finalize]d before other types of options.
 *
 * @param callback This callback is called when the option is encountered on the command line. If you want to
 *   print a message and halt execution normally, you should throw a [PrintMessage] exception. The callback it
 *   passed the current execution context as a parameter.
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

    override val secondaryNames: Set<String> get() = emptySet()
    override val parser: OptionParser = FlagOptionParser
    override val metavar: String? get() = null
    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        this.callback(OptionTransformContext(this, context))
    }
}

internal fun helpOption(names: Set<String>, message: String) = EagerOption(names, 0, message, false, emptyMap(),
        callback = { throw PrintHelpMessage(context.command) })

/** Add an eager option to this command that, when invoked, prints a version message and exits. */
inline fun <T : CliktCommand> T.versionOption(
        version: String,
        help: String = "Show the version and exit",
        names: Set<String> = setOf("--version"),
        crossinline message: (String) -> String = { "$commandName version $it" }): T = apply {
    registerOption(EagerOption(names, 0, help, false, emptyMap()) { throw PrintMessage(message(version)) })
}
