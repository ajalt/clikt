package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.completion.CompletionGenerator
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.internal.*
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization

// TODO: docs, changelog
object CommandLineParser {
    fun tokenize(
        commandLine: String,
        localization: Localization = defaultLocalization,
    ): List<String> {
        return shlex("TODO", commandLine, localization)// TODO shlex
    }

    inline fun <T : BaseCliktCommand<T>> main(
        command: T,
        argv: List<String>,
        parseAndRun: T.(argv: List<String>) -> Unit,
    ) {
        try {
            command.parseAndRun(argv)
        } catch (e: CliktError) {
            command.echoFormattedHelp(e)
            CliktUtil.exitProcess(e.statusCode)
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

    // TODO: docs throws
    inline fun <T : BaseCliktCommand<T>> run(
        rootInvocation: CommandInvocation<T>,
        runCommand: (T) -> Unit,
    ) {
        val invocations = rootInvocation.flatten()
        try {
            for (invocation in invocations) {
                finalize(invocation)
                runCommand(invocation.command)
            }
        } finally {
            invocations.close()
        }
    }

    // TODO: docs does not throw
    fun <T : BaseCliktCommand<T>> parse(command: T, argv: List<String>): CommandLineParseResult<T> {
        return parseArgv(command, argv)
    }

    // TODO: docs throws
    fun finalize(invocation: CommandInvocation<*>) {
        val command = invocation.command
        val context = command.currentContext
        val groups = command.registeredParameterGroups()
        val arguments = command.registeredArguments()

        throwCompletionMessageIfRequested(context, command)

        val (eagerOpts, nonEagerOpts) = command.registeredOptions()
            .partition { it.eager }

        val (eagerInvs, nonEagerInvs) = invocation.optionInvocations.entries
            .partition { it.key.eager }
            .toList().map { it.associate { (k, v) -> k to v } }

        // finalize and validate eager options first; unlike other options, eager options only get
        // validated if they're invoked
        finalizeOptions(context, eagerOpts, eagerInvs)
        validateParameters(context, eagerInvs.keys).throwErrors()

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
}

private fun CommandInvocation<*>.throwErrors() {
    when (val first = errors.firstOrNull()) {
        // TODO: check if there is ever an error after a UsageError
        is UsageError -> errors.takeWhile { it is UsageError }
            .filterIsInstance<UsageError>().throwErrors()

        is CliktError -> throw first
    }
}

private fun throwCompletionMessageIfRequested(
    context: Context,
    command: BaseCliktCommand<*>,
) {
    if (command.autoCompleteEnvvar == null) return
    val envvar = when {
        command.autoCompleteEnvvar.isBlank() -> "_${
            command.commandName.replace("-", "_").uppercase()
        }_COMPLETE"

        else -> command.autoCompleteEnvvar
    }
    val envval = context.readEnvvar(envvar) ?: return
    throw CompletionGenerator.getCompletionMessage(command, envval)
}
