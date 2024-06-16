package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import com.github.ajalt.clikt.parsers.CommandLineParser

/**
 * A command with a [run] function that's called when the command is invoked.
 *
 * It uses the [PlaintextHelpFormatter] by default. You usually want to inherit from `CliktCommand`
 * instead, which uses the `MordantHelpFormatter`.
 *
 * By default, this class doesn't support and text formatting or wrapping, and it doesn't support
 * environment variables, arg files, exit status codes, or printing to stderr. Although you can use
 * this class without those features, if you need then you can set a custom [Context.helpFormatter],
 * [Context.readArgumentFile], [Context.Builder.readEnvvar], [Context.exitProcess], or
 * [Context.echoMessage].
 *
 * ### Example
 *
 * On JVM, if you want to use this class and define the above features:
 *
 * ```kotlin
 *    abstract class MyCoreCommand : CoreCliktCommand() {
 *        init {
 *            context {
 *                argumentFileReader = {
 *                    with(Paths.get(it)) {
 *                        if (isRegularFile()) readText() else throw FileNotFound(it)
 *                    }
 *                }
 *                envvarReader = { System.getenv(it) }
 *                exitProcess = { System.exit(it) }
 *                echoer = { context, message, newline, err ->
 *                    val writer = if (err) System.err else System.out
 *                    if (newline) {
 *                        writer.println(message)
 *                    } else {
 *                        writer.print(message)
 *                    }
 *                }
 *            }
 *        }
 *    }
 * ```
 *
 */
abstract class CoreCliktCommand(
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
) : BaseCliktCommand<CoreCliktCommand>(name) {
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
fun CoreCliktCommand.main(argv: List<String>) {
    CommandLineParser.main(this) { parse(argv) }
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
fun CoreCliktCommand.main(argv: Array<out String>) = main(argv.asList())

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun CoreCliktCommand.parse(argv: Array<String>) {
    parse(argv.asList())
}

/**
 * Parse the command line and throw an exception if parsing fails.
 *
 * You should use [main] instead unless you want to handle output yourself.
 */
fun CoreCliktCommand.parse(argv: List<String>) {
    CommandLineParser.parseAndRun(this, argv) { it.run() }
}
