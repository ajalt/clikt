package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.completion.CompletionGenerator.generateCompletionForCommand
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.internal.*
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization

/**
 * Methods for parsing command lines and running commands manually.
 */
object CommandLineParser {
    /**
     * Split a command line into a list of argv tokens.
     *
     * ### Example
     * ```
     * tokenize("--text 'hello world'") == listOf("--text", "hello world")
     * ```
     *
     * @param commandLine The command line to split
     * @param filename The name of the file being parsed. This is used in error messages.
     * @param localization The localization to use for error messages
     */
    fun tokenize(
        commandLine: String,
        filename: String = "",
        localization: Localization = defaultLocalization,
    ): List<String> {
        return shlex(filename, commandLine, localization)
    }

    /**
     * Call [parseAndRun] on the given [command]. If an error is thrown, exit the
     * process with the error's [status code][CliktError.statusCode].
     */
    inline fun <T : BaseCliktCommand<T>> main(command: T, parseAndRun: T.() -> Unit) {
        try {
            command.parseAndRun()
        } catch (e: CliktError) {
            command.echoFormattedHelp(e)
            command.currentContext.exitProcess(e.statusCode)
        }
    }

    /**
     * Call [parseAndRun] on the given [command]. If an error is thrown, exit the
     * process with the error's [status code][CliktError.statusCode].
     *
     * @return The value returned by [parseAndRun]
     * @throws ProgramResult If run on Kotlin/JS on browsers and an error occurs, since it's not
     * possible to exit the process in that case.
     */
    inline fun <T : BaseCliktCommand<T>, R> mainReturningValue(
        command: T, parseAndRun: T.() -> R,
    ): R {
        try {
            return command.parseAndRun()
        } catch (e: CliktError) {
            command.echoFormattedHelp(e)
            command.currentContext.exitProcess(e.statusCode)
            throw ProgramResult(e.statusCode)
        }
    }


    /**
     * A shortcut for calling `run(parse(command, argv).invocation, runCommand)`
     */
    inline fun <T : BaseCliktCommand<T>> parseAndRun(
        command: T,
        argv: List<String>,
        runCommand: (T) -> Unit,
    ): CommandLineParseResult<T> {
        val result = parse(command, argv)
        run(result.invocation, runCommand)
        return result
    }

    /**
     * [Finalize][finalizeCommand] and [run][runCommand] all invoked commands.
     *
     * @throws CliktError if an error occurred while parsing or of any occur while finalizing or
     * running the commands.
     */
    inline fun <T : BaseCliktCommand<T>> run(
        rootInvocation: CommandInvocation<T>,
        runCommand: (T) -> Unit,
    ) {
        rootInvocation.flatten().use { invocations ->
            for (invocation in invocations) {
                runCommand(invocation.command)
            }
        }
    }

    /**
     * Parse a command line and return the result.
     *
     * This function does not throw exceptions. If parsing errors occur, they will be in the returned
     * result.
     *
     * This function does not [run] the command or [finalizeCommand] the invocations.
     */
    fun <T : BaseCliktCommand<T>> parse(command: T, argv: List<String>): CommandLineParseResult<T> {
        return parseArgv(command, argv)
    }
    /**
     * Finalize eager options for a command invocation, running them if they were invoked.
     *
     * This does not finalize any other parameters.
     *
     * @throws CliktError If any of the eager options were invoked and throw an error like
     * [PrintHelpMessage].
     */
    fun finalizeEagerOptions(invocation: CommandInvocation<*>) {
        val command = invocation.command
        val context = command.currentContext
        throwCompletionMessageIfRequested(context, command)

        val (eagerOpts, _) = getOpts(command)
        val (eagerInvs, _) = getInvs(invocation)

        // finalize and validate eager options first; unlike other options, eager options only get
        // validated if they're invoked
        finalizeOptions(context, eagerOpts, eagerInvs)
        validateParameters(context, eagerInvs.keys).throwErrors()
    }

    /**
     * Finalize a command invocation, converting and setting the values for all options and other
     * parameters. This function does not [finalizeEagerOptions] or [run] the command.
     *
     * @throws CliktError If the [invocation] had any errors or if any parameters fail to finalize,
     * such as if a required option is missing or a value could not be converted.
     */
    fun finalizeCommand(invocation: CommandInvocation<*>) {
        val command = invocation.command
        val context = command.currentContext
        val groups = command.registeredParameterGroups()
        val arguments = command.registeredArguments()

        val (_, nonEagerOpts) = getOpts(command)
        val (_, nonEagerInvs) = getInvs(invocation)

        // throw any parse errors after the eager options are finalized
        invocation.throwErrors()

        // then finalize and validate everything else
        val nonEagerNonGroupOpts = nonEagerOpts.filter { it.group == null }
        val argumentInvocations = invocation.argumentInvocations
        finalizeParameters(
            context, nonEagerNonGroupOpts, groups, arguments, nonEagerInvs, argumentInvocations
        ).throwErrors()

        validateParameters(context, nonEagerNonGroupOpts, groups, arguments).throwErrors()

        if (invocation.subcommandInvocations.isEmpty()
            && command._subcommands.isNotEmpty()
            && !command.invokeWithoutSubcommand
        ) {
            throw PrintHelpMessage(context, error = true)
        }

        context.invokedSubcommands += invocation.subcommandInvocations.map { it.command }
    }

    private fun getInvs(invocation: CommandInvocation<*>) =
        invocation.optionInvocations.entries
            .partition { it.key.eager }
            .toList().map { it.associate { (k, v) -> k to v } }

    private fun getOpts(command: BaseCliktCommand<*>) =
        command.registeredOptions()
            .partition { it.eager }
}

private fun CommandInvocation<*>.throwErrors() {
    // The errors are always UsageErrors, expect for the case of printHelpOnEmptyArgs
    when (val first = errors.firstOrNull()) {
        is UsageError -> errors.filterIsInstance<UsageError>().throwErrors()
        is CliktError -> throw first
    }
}

private fun throwCompletionMessageIfRequested(
    context: Context,
    command: BaseCliktCommand<*>,
) {
    val commandEnvvar = command.autoCompleteEnvvar ?: return
    val envvar = when {
        commandEnvvar.isBlank() -> "_${
            command.commandName.replace("-", "_").uppercase()
        }_COMPLETE"

        else -> commandEnvvar
    }
    val envval = context.readEnvvar(envvar) ?: return
    throw PrintCompletionMessage(generateCompletionForCommand(command, envval))
}
