package com.github.ajalt.clikt.command

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parsers.CommandLineParser

// TODO: md docs, changelog
/**
 * A version of [CliktCommand] that returns a value from the [run] function, which is then passed to
 * subcommands.
 */
abstract class ChainedCliktCommand<T>(
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
) : BaseCliktCommand<ChainedCliktCommand<T>>(
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
     * This takes the value returned by the previously invoked command and returns a new value.
     *
     * This is called after command line parsing is complete. If this command is a subcommand, this
     * will only be called if the subcommand is invoked.
     *
     * If one of this command's subcommands is invoked, this is called before the subcommand's
     * arguments are parsed.
     */
    abstract fun run(value: T): T
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
fun <T> ChainedCliktCommand<T>.main(argv: List<String>, initial: T): T {
    try {
        return parse(argv, initial)
    } catch (e: CliktError) {
        echoFormattedHelp(e)
        CliktUtil.exitProcess(e.statusCode)
        // Throw an exception if we can't exit, since we don't have a return value
        throw ProgramResult(e.statusCode)
    }
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
fun <T> ChainedCliktCommand<T>.main(argv: Array<out String>, initial: T) {
    main(argv.asList(), initial)
}

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun <T> ChainedCliktCommand<T>.parse(argv: Array<String>, initial: T): T {
    return parse(argv.asList(), initial)
}

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun <T> ChainedCliktCommand<T>.parse(argv: List<String>, initial: T): T {
    var value = initial
    CommandLineParser.parseAndRun(this, argv) { value = it.run(value) }
    return value
}
