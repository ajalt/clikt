package com.github.ajalt.clikt.command

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.clikt.testing.CliktCommandTestResult
import com.github.ajalt.clikt.testing.test
import com.github.ajalt.mordant.rendering.AnsiLevel

/**
 * A version of [CliktCommand] that returns a value from the [run] function, which is then passed to
 * subcommands.
 *
 * This command works best if you set [allowMultipleSubcommands] to `true`.
 */
abstract class ChainedCliktCommand<T>(
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
) : CoreChainedCliktCommand<T>(name) {
    init {
        installMordant()
    }
}

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed
 * with [print] or [println] is not.
 *
 * @param argv The command line to send to the command
 * @param stdin Content of stdin that will be read by prompt options. Multiple inputs should be separated by `\n`.
 * @param envvars A map of environment variable name to value for envvars that can be read by the command
 * @param includeSystemEnvvars Set to true to include the environment variables from the system in addition to those
 *   defined in [envvars]
 * @param ansiLevel Defaults to no colored output; set to [AnsiLevel.TRUECOLOR] to include ANSI codes in the output.
 * @param width The width of the terminal, used to wrap text
 * @param height The height of the terminal
 */
fun <T> ChainedCliktCommand<T>.test(
    argv: String,
    initial: T,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
): CliktCommandTestResult {
    val argvArray = CommandLineParser.tokenize(argv)
    return test(argvArray, initial, stdin, envvars, includeSystemEnvvars, ansiLevel, width, height)
}

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed
 * with [print] or [println] is not.
 *
 * @param argv The command line to send to the command
 * @param stdin Content of stdin that will be read by prompt options. Multiple inputs should be separated by `\n`.
 * @param envvars A map of environment variable name to value for envvars that can be read by the command
 * @param includeSystemEnvvars Set to true to include the environment variables from the system in addition to those
 *   defined in [envvars]
 * @param ansiLevel Defaults to no colored output; set to [AnsiLevel.TRUECOLOR] to include ANSI codes in the output.
 * @param width The width of the terminal, used to wrap text
 * @param height The height of the terminal
 */
fun <T> ChainedCliktCommand<T>.test(
    argv: Array<String>,
    initial: T,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
): CliktCommandTestResult {
    return test(
        argv.asList(), initial, stdin, envvars, includeSystemEnvvars, ansiLevel, width, height
    )
}

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed
 * with [print] or [println] is not.
 *
 * @param argv The command line to send to the command
 * @param stdin Content of stdin that will be read by prompt options. Multiple inputs should be separated by `\n`.
 * @param envvars A map of environment variable name to value for envvars that can be read by the command
 * @param includeSystemEnvvars Set to true to include the environment variables from the system in addition to those
 *   defined in [envvars]
 * @param ansiLevel Defaults to no colored output; set to [AnsiLevel.TRUECOLOR] to include ANSI codes in the output.
 * @param width The width of the terminal, used to wrap text
 * @param height The height of the terminal
 */
fun <T> ChainedCliktCommand<T>.test(
    argv: List<String>,
    initial: T,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
): CliktCommandTestResult {
    return test(argv, stdin, envvars, includeSystemEnvvars, ansiLevel, width, height) {
        parse(it, initial)
    }
}
