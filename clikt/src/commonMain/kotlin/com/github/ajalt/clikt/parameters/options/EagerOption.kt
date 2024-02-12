package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult


/**
 * Add an eager option to this command that, when invoked, runs [action].
 *
 * @param names The names that can be used to invoke this option. They must start with a punctuation character.
 * @param help The description of this option, usually a single line.
 * @param hidden Hide this option from help outputs.
 * @param helpTags Extra information about this option to pass to the help formatter
 * @param groupName All options that share a group name will be grouped together in help output.
 * @param action This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`Abort(error=false)`][Abort].
 */
fun <T : CliktCommand> T.eagerOption(
    vararg names: String,
    help: String = "",
    hidden: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    groupName: String? = null,
    action: OptionTransformContext.() -> Unit,
): T = eagerOption(names.asList(), help, hidden, helpTags, groupName, action)

/**
 * Add an eager option to this command that, when invoked, runs [action].
 *
 * @param names The names that can be used to invoke this option. They must start with a punctuation character.
 * @param help The description of this option, usually a single line.
 * @param hidden Hide this option from help outputs.
 * @param helpTags Extra information about this option to pass to the help formatter
 * @param groupName All options that share a group name will be grouped together in help output.
 * @param action This callback is called when the option is encountered on the command line. If
 *   you want to print a message and halt execution normally, you should throw a [PrintMessage]
 *   exception. If you want to exit normally without printing a message, you should throw
 *   [`ProgramResult(0)`][ProgramResult].
 */
fun <T : CliktCommand> T.eagerOption(
    names: Collection<String>,
    help: String = "",
    hidden: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    groupName: String? = null,
    action: OptionTransformContext.() -> Unit,
): T {
    val o = option(
        *names.toTypedArray(),
        help = help,
        hidden = hidden,
        helpTags = helpTags,
        eager = true,
    ).flag().validate { if (it) action() }
    o.groupName = groupName
    registerOption(o)
    return this
}

/** Add an eager option to this command that, when invoked, prints a version message and exits. */
inline fun <T : CliktCommand> T.versionOption(
    /** The version to print */
    version: String,
    /** The help message for the option */
    help: String = "Show the version and exit",
    /** The names of this option. Defaults to -v or --version */
    names: Set<String> = setOf("-v", "--version"),
    /** A block that returns the message to print. The [version] is passed as a parameter */
    crossinline message: (String) -> String = { "$commandName version $it" },
): T = eagerOption(names, help) { throw PrintMessage(message(version)) }
