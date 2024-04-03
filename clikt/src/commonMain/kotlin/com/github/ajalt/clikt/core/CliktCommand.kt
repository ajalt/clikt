package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parsers.CommandLineParser

/**
 * The [CliktCommand] is the core of command line interfaces in Clikt.
 *
 * Command line interfaces created by creating a subclass of [CliktCommand] with properties defined with
 * [option] and [argument]. You can then parse `argv` by calling [main], which will take care of printing
 * errors and help to the user. If you want to handle output yourself, you can use [parse] instead.
 *
 * Once the command line has been parsed and all the parameters are populated, [run] is called.
 */
abstract class CliktCommand(
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
) : BaseCliktCommand<() -> Unit>(
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
    final override val runner: () -> Unit get() = ::run

    /**
     * Perform actions after parsing is complete and this command is invoked.
     *
     * This is called after command line parsing is complete. If this command is a subcommand, this
     * will only be called if the subcommand is invoked.
     *
     * If one of this command's subcommands is invoked, this is called before the subcommand's
     * arguments are parsed.
     */
    abstract fun run()
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
fun BaseCliktCommand<() -> Unit>.main(argv: Array<out String>) = main(argv.asList())

/**
 * Parse the command line and print helpful output if any errors occur.
 *
 * This function calls [parse] and catches any [CliktError]s that are thrown, exiting the process
 * with the specified [status code][CliktError.statusCode]. Other errors are allowed to pass
 * through.
 *
 * If you don't want Clikt to exit your process, call [parse] instead.
 */
fun BaseCliktCommand<() -> Unit>.main(argv: List<String>) = main(argv) { parse(argv) }

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun BaseCliktCommand<() -> Unit>.parse(argv: Array<String>) {
    parse(argv.asList())
}

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun BaseCliktCommand<() -> Unit>.parse(argv: List<String>) {
    CommandLineParser.parseAndRun(this, argv) { it.runner() }
}
