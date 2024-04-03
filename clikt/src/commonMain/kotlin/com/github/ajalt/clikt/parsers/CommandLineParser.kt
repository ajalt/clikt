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

    inline fun <RunnerT> parseAndRun(
        command: BaseCliktCommand<RunnerT>,
        argv: List<String>,
        crossinline run: (BaseCliktCommand<RunnerT>) -> Unit,
    ) {
// TODO   generateCompletion()
        val result = parse(command, argv)
        for (invocation in result.invocations) {
            try {
                finalize(invocation)
                run(invocation.command)
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

        val (eagerOpts, nonEagerOpts) = command.registeredOptions()
            .partition { it.eager }

        val (eagerInvs, nonEagerInvs) = invocation.optionInvocations.entries
            .partition { it.key.eager }
            .toList().map { it.associate { (k, v) -> k to v } }

        // finalize and validate eager options first
        finalizeOptions(context, eagerOpts, eagerInvs)
        validateOptions(context, eagerInvs).throwErrors()

        // throw any parse errors after the eager options are finalized
        invocation.throwErrors()

        // then finalize and validate everything else
        finalizeParameters(
            context,
            nonEagerOpts.filter { it.group == null },
            command.registeredParameterGroups(),
            command.registeredArguments(),
            nonEagerInvs,
            invocation.argumentInvocations,
        ).throwErrors()

        validateParameters(context, nonEagerInvs).throwErrors()

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
