package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.clikt.parsers.shlex
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import kotlin.jvm.JvmName

data class CliktCommandTestResult(
    /** Standard output captured from the command */
    val stdout: String,
    /** Error output captured form the command */
    val stderr: String,
    /** A string combining [stdout] and [stderr] in the order that they were printed */
    val output: String,
    /**
     * The exit status code of the command.
     *
     * By default, commands will return 0 for success and 1 if an error occurs.
     */
    val statusCode: Int,
)

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed with [print] or
 * [println] is not.
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
fun CliktCommand.test(
    argv: String,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
): CliktCommandTestResult {
    val argvArray = CommandLineParser.tokenize(argv)
    return test(argvArray, stdin, envvars, includeSystemEnvvars, ansiLevel, width, height)
}

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed with [print] or
 * [println] is not.
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
@JvmName("varargTest")
fun CliktCommand.test(
    vararg argv: String,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
): CliktCommandTestResult {
    return test(argv.asList(), stdin, envvars, includeSystemEnvvars, ansiLevel, width, height)
}

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed with [print] or
 * [println] is not.
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
fun CliktCommand.test(
    argv: List<String>,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
): CliktCommandTestResult {
    return test(argv.toTypedArray(), stdin, envvars, includeSystemEnvvars, ansiLevel, width, height)
}

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed with [print] or
 * [println] is not.
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
fun CliktCommand.test(
    argv: Array<String>,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
): CliktCommandTestResult {
    var exitCode = 0
    val recorder = TerminalRecorder(ansiLevel, width, height)
    recorder.inputLines = stdin.split("\n").toMutableList()
    context {
        envvarReader = { envvars[it] ?: (if (includeSystemEnvvars) readEnvvar(it) else null) }
        terminal = Terminal(terminal.theme, terminal.tabWidth, recorder)
    }

    try {
        parse(argv)
    } catch (e: CliktError) {
        echoFormattedHelp(e)
        exitCode = e.statusCode
    }
    return CliktCommandTestResult(recorder.stdout(), recorder.stderr(), recorder.output(), exitCode)
}
