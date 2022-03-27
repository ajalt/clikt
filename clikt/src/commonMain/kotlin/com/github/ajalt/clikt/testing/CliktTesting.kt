package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.parsers.shlex

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
 */
fun CliktCommand.test(
    argv: Array<String>,
    stdin: String = "",
    envvars: Map<String, String> = emptyMap(),
    includeSystemEnvvars: Boolean = false,
): CliktCommandTestResult {
    val stdinLines = stdin.split("\n").toMutableList()
    val stdout = StringBuilder()
    val stderr = StringBuilder()
    val output = StringBuilder()
    var exitCode = 0
    val testConsole = object : CliktConsole {
        override fun promptForLine(prompt: String, hideInput: Boolean): String? {
            print(prompt, false)
            return if (stdinLines.isEmpty()) null else stdinLines.removeFirst()
        }

        override fun print(text: String, error: Boolean) {
            (if (error) stderr else stdout).append(text)
            output.append(text)
        }

        override val lineSeparator: String get() = "\n"
    }
    context {
        envvarReader = { envvars[it] ?: (if (includeSystemEnvvars) readEnvvar(it) else null) }
        console = testConsole
    }

    try {
        parse(argv)
    } catch (e: CliktError) {
        echoFormattedError(e)
        exitCode = e.statusCode
    }
    return CliktCommandTestResult(stdout.toString(), stderr.toString(), output.toString(), exitCode)
}
