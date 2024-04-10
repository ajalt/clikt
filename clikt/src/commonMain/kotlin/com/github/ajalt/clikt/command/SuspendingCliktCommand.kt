package com.github.ajalt.clikt.command

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parsers.CommandLineParser

/**
 * A version of [CliktCommand] that supports a suspending [run] function.
 */
abstract class SuspendingCliktCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = "",
    allowMultipleSubcommands: Boolean = false,
    treatUnknownOptionsAsArgs: Boolean = false,
    hidden: Boolean = false,
) : BaseCliktCommand<SuspendingCliktCommand>(
    help,
    epilog,
    name,
    invokeWithoutSubcommand,
    printHelpOnEmptyArgs,
    helpTags,
    autoCompleteEnvvar,
    allowMultipleSubcommands,
    treatUnknownOptionsAsArgs,
    hidden
) {
    /**
     * Perform actions after parsing is complete and this command is invoked.
     *
     * This is called after command line parsing is complete. If this command is a subcommand, this
     * will only be called if the subcommand is invoked.
     *
     * If one of this command's subcommands is invoked, this is called before the subcommand's
     * arguments are parsed.
     */
    abstract suspend fun run()
}


/**
 * Parse the command line and print helpful output if any errors occur.
 *
 * This function calls [parse] and catches any [CliktError]s that are thrown, exiting the process
 * with the specified [status code][CliktError.statusCode]. Other errors are allowed to pass
 * through.
 *
 * If you don't want Clikt to exit your process, call [parse] instead.
 */
suspend fun SuspendingCliktCommand.main(argv: List<String>) {
    CommandLineParser.main(this, argv) { parse(argv) }
}

/**
 * Parse the command line and print helpful output if any errors occur.
 *
 * This suspend function calls [parse] and catches any [CliktError]s that are thrown, exiting the process
 * with the specified [status code][CliktError.statusCode]. Other errors are allowed to pass
 * through.
 *
 * If you don't want Clikt to exit your process, call [parse] instead.
 */
suspend fun SuspendingCliktCommand.main(argv: Array<out String>) = main(argv.asList())

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
suspend fun SuspendingCliktCommand.parse(argv: Array<String>) {
    parse(argv.asList())
}

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
suspend fun SuspendingCliktCommand.parse(argv: List<String>) {
    CommandLineParser.parseAndRun(this, argv) { it.run() }
}
