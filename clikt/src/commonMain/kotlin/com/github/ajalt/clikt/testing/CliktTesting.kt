package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.parsers.shlex
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.ExperimentalTerminalApi
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.VirtualTerminalInterface

/**
 * @property stdout Standard output captured from the command
 * @property stderr Error output captured form the command
 * @property output A string combining [stdout] and [stderr] in the order that they were printed
 * @property statusCode The exit status code of the command. By default, commands will return 0
 *   for success and 1 if an error occurs.
 */
data class CliktCommandTestResult(
    val stdout: String,
    val stderr: String,
    val output: String,
    val statusCode: Int,
)

fun CliktCommand.test(
    argv: String,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
): CliktCommandTestResult {
    val toks = shlex("test", argv, null)
    return test(toks, stdin, envvars)
}


fun CliktCommand.test(
    argv: List<String>,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
): CliktCommandTestResult = test(argv.toTypedArray(), stdin, envvars)

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed with [print] or
 * [println] will not.
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
@OptIn(ExperimentalTerminalApi::class)
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
    val iface = VirtualTerminalInterface(ansiLevel, width, height)
    iface.inputLines = stdin.split("\n").toMutableList()
    context {
        envvarReader = { envvars[it] ?: (if (includeSystemEnvvars) readEnvvar(it) else null) }
        terminal = Terminal(terminal.theme, terminal.tabWidth, iface)
    }

    try {
        parse(argv)
    } catch (e: CliktError) {
        echoFormattedError(e)
        exitCode = e.statusCode
    }
    return CliktCommandTestResult(iface.stdout(), iface.stderr(), iface.output(), exitCode)
}
