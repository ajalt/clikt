package com.github.ajalt.clikt.command

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parsers.CommandLineParser

/**
 * A version of [CoreCliktCommand] that returns a value from the [run] function, which
 * is then passed to subcommands.
 *
 * This command works best if you set [allowMultipleSubcommands] to `true`.
 */
abstract class CoreChainedCliktCommand<T>(
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
) : BaseCliktCommand<CoreChainedCliktCommand<T>>(name) {
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
fun <T> CoreChainedCliktCommand<T>.main(argv: List<String>, initial: T): T {
    return CommandLineParser.mainReturningValue(this) { parse(argv, initial) }
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
fun <T> CoreChainedCliktCommand<T>.main(argv: Array<out String>, initial: T): T {
    return main(argv.asList(), initial)
}

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun <T> CoreChainedCliktCommand<T>.parse(argv: Array<String>, initial: T): T {
    return parse(argv.asList(), initial)
}

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun <T> CoreChainedCliktCommand<T>.parse(argv: List<String>, initial: T): T {
    var value = initial
    CommandLineParser.parseAndRun(this, argv) { value = it.run(value) }
    return value
}
