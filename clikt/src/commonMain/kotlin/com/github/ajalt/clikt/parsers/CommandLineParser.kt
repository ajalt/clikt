package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.completion.CompletionGenerator
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.internal.*
import com.github.ajalt.clikt.mpp.exitProcessMpp
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization

// TODO: docs, changelog
object CommandLineParser {
    fun tokenize(
        commandLine: String,
        localization: Localization = defaultLocalization,
    ): List<String> {
        return shlex("TODO", commandLine, localization)// TODO
    }

    fun <RunnerT> main(
        command: BaseCliktCommand<RunnerT>,
        argv: List<String>,
        parseAndRun: BaseCliktCommand<RunnerT>.(List<String>) -> Unit,
    ) {
        try {
            command.parseAndRun(argv)
        } catch (e: CliktError) {
            command.echoFormattedHelp(e)
            exitProcessMpp(e.statusCode)
        }
    }


    /**
     * A shortcut for calling `run(parse(command, argv).invocation, runCommand)`
     */
    fun <RunnerT> parseAndRun(
        command: BaseCliktCommand<RunnerT>,
        argv: List<String>,
        runCommand: (BaseCliktCommand<RunnerT>) -> Unit,
    ): CommandLineParseResult<RunnerT> {
        val result = parse(command, argv)
        run(result.invocation, runCommand)
        return result
    }

    // TODO: docs throws
    fun <RunnerT> run(
        invocation: CommandInvocation<RunnerT>,
        runCommand: (BaseCliktCommand<RunnerT>) -> Unit,
    ) {
        try {
            finalize(invocation)
            runCommand(invocation.command)

            for (subcommandInvocation in invocation.subcommandInvocations) {
                run(subcommandInvocation, runCommand)
            }
        } finally {
            invocation.command.currentContext.close()
        }
    }

    // TODO: docs does not throw
    fun <RunnerT> parse(
        command: BaseCliktCommand<RunnerT>, argv: List<String>,
    ): CommandLineParseResult<RunnerT> {
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

        context.invokedSubcommands = invocation.subcommandInvocations.map { it.command }
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
