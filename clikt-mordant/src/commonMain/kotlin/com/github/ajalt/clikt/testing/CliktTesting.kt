package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.input.InputEvent
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
 * @param hyperlinks Whether to enable hyperlink support in the terminal
 * @param outputInteractive Whether the output is interactive
 * @param inputInteractive Whether the input is interactive
 */
fun CliktCommand.test(
    argv: String,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
): CliktCommandTestResult {
    val argvArray = CommandLineParser.tokenize(argv)
    return test(
        argvArray, stdin, envvars, includeSystemEnvvars, ansiLevel, width, height,
        hyperlinks, outputInteractive, inputInteractive
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
 * @param hyperlinks Whether to enable hyperlink support in the terminal
 * @param outputInteractive Whether the output is interactive
 * @param inputInteractive Whether the input is interactive
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
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
): CliktCommandTestResult {
    return test(
        argv.asList(), stdin, envvars, includeSystemEnvvars, ansiLevel, width, height,
        hyperlinks, outputInteractive, inputInteractive
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
 * @param hyperlinks Whether to enable hyperlink support in the terminal
 * @param outputInteractive Whether the output is interactive
 * @param inputInteractive Whether the input is interactive
 */
fun CliktCommand.test(
    argv: Array<String>,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
): CliktCommandTestResult {
    return test(
        argv.asList(), stdin, envvars, includeSystemEnvvars, ansiLevel, width, height,
        hyperlinks, outputInteractive, inputInteractive
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
 * @param inputEvents Input events to pass to an interactive command
 * @param envvars A map of environment variable name to value for envvars that can be read by the command
 * @param includeSystemEnvvars Set to true to include the environment variables from the system in addition to those
 *   defined in [envvars]
 * @param ansiLevel Defaults to no colored output; set to [AnsiLevel.TRUECOLOR] to include ANSI codes in the output.
 * @param width The width of the terminal, used to wrap text
 * @param height The height of the terminal
 * @param hyperlinks Whether to enable hyperlink support in the terminal
 * @param outputInteractive Whether the output is interactive
 * @param inputInteractive Whether the input is interactive
 */
fun CliktCommand.test(
    argv: List<String>,
    stdin: String = "",
    inputEvents: List<InputEvent> = listOf(),
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
): CliktCommandTestResult {
    return test(
        argv, stdin, inputEvents, envvars, includeSystemEnvvars, ansiLevel, width,
        height, hyperlinks, outputInteractive, inputInteractive
    ) { parse(it) }
}

/**
 * Test this command, returning a result that captures the output and result status code.
 *
 * Note that only output printed with [echo][CliktCommand.echo] will be captured. Anything printed
 * with [print] or [println] is not.
 *
 * @param argv The command line to send to the command
 * @param stdin Content of stdin that will be read by prompt options. Multiple inputs should be separated by `\n`.
 * @param inputEvents Input events to pass to an interactive command
 * @param envvars A map of environment variable name to value for envvars that can be read by the command
 * @param includeSystemEnvvars Set to true to include the environment variables from the system in addition to those
 *   defined in [envvars]
 * @param ansiLevel Defaults to no colored output; set to [AnsiLevel.TRUECOLOR] to include ANSI codes in the output.
 * @param width The width of the terminal, used to wrap text
 * @param height The height of the terminal
 * @param hyperlinks Whether to enable hyperlink support in the terminal
 * @param outputInteractive Whether the output is interactive
 * @param inputInteractive Whether the input is interactive
 * @param parse The function to call to parse the command line and run the command
 */
inline fun <T : BaseCliktCommand<T>> BaseCliktCommand<T>.test(
    argv: List<String>,
    stdin: String = "",
    inputEvents: List<InputEvent> = listOf(),
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
    ansiLevel: AnsiLevel = AnsiLevel.NONE,
    width: Int = 79,
    height: Int = 24,
    hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
    outputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    inputInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
    parse: (argv: List<String>) -> Unit,
): CliktCommandTestResult {
    var exitCode = 0
    val recorder = TerminalRecorder(
        ansiLevel, width, height, hyperlinks, outputInteractive, inputInteractive
    )
    recorder.inputLines = stdin.split("\n").toMutableList()
    recorder.inputEvents = inputEvents.toMutableList()
    configureContext {
        val originalReader = readEnvvar
        readEnvvar = { envvars[it] ?: (if (includeSystemEnvvars) originalReader(it) else null) }
        terminal = Terminal(
            theme = terminal.theme,
            tabWidth = terminal.tabWidth,
            terminalInterface = recorder
        )
    }
    try {
        parse(argv)
    } catch (e: CliktError) {
        echoFormattedHelp(e)
        exitCode = e.statusCode
    }
    return CliktCommandTestResult(recorder.stdout(), recorder.stderr(), recorder.output(), exitCode)
}
