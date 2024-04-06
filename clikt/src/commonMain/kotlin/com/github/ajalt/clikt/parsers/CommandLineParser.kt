package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.internal.*
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization

object CommandLineParser {
    fun tokenize(
        commandLine: String,
        localization: Localization = defaultLocalization,
    ): List<String> {
        return shlex("TODO", commandLine, localization)// TODO
    }

    /**
     * A shortcut for calling `run(parse(command, argv), runCommand)`
     */
    fun <RunnerT> parseAndRun(
        command: BaseCliktCommand<RunnerT>,
        argv: List<String>,
        runCommand: (BaseCliktCommand<RunnerT>) -> Unit,
    ) {
        run(parse(command, argv), runCommand)
    }

    // TODO: docs throws
    fun <RunnerT> run(
        result: CommandLineParseResult<RunnerT>,
        runCommand: (BaseCliktCommand<RunnerT>) -> Unit,
    ) {
        result.throwError()
        for (invocation in result.invocations) {
            try {
                finalize(invocation)
                runCommand(invocation.command)
            } finally {
                invocation.command.currentContext.close()
            }
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
        val subcommand = invocation.invokedSubcommand
        val groups = command.registeredParameterGroups()
        val arguments = command.registeredArguments()

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
        finalizeParameters(
            context,
            nonEagerNonGroupOpts,
            groups,
            arguments,
            nonEagerInvs,
            invocation.argumentInvocations,
        ).throwErrors()

        validateParameters(context, nonEagerNonGroupOpts, groups, arguments).throwErrors()

        if (subcommand == null && command._subcommands.isNotEmpty() &&
            !command.invokeWithoutSubcommand
        ) {
            throw PrintHelpMessage(context, error = true)
        }

        context.invokedSubcommand = subcommand
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
