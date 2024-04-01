package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.internal.finalizeOptions
import com.github.ajalt.clikt.internal.finalizeParameters
import com.github.ajalt.clikt.internal.validateOptions
import com.github.ajalt.clikt.internal.validateParameters
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization

object CommandLineParser {
    fun tokenize(
        commandLine: String,
        localization: Localization = defaultLocalization,
    ): List<String> {
        return shlex("TODO", commandLine, localization)// TODO
    }

    // TODO: docs does not throw
    fun <RunnerT> parse(
        command: BaseCliktCommand<RunnerT>, originalArgv: List<String>,
    ): CommandLineParseResult<RunnerT> {
        return parseArgv(command, originalArgv)
    }

    // TODO: docs throws
    fun finalize(invocation: CommandInvocation<*>) {
        val (eagerOpts, nonEagerOpts) = invocation.command.registeredOptions()
            .partition { it.eager }

        val (eagerInvs, nonEagerInvs) = invocation.optionInvocations.entries
            .partition { it.key.eager }
            .toList().map { it.associate { (k, v) -> k to v } }

        // finalize and validate eager options first
        finalizeOptions(invocation.command.currentContext, eagerOpts, eagerInvs)
        validateOptions(invocation.command.currentContext, eagerInvs).throwErrors()

        // then finalize and validate everything else
        finalizeParameters(
            invocation.command.currentContext,
            nonEagerOpts,
            invocation.command.registeredParameterGroups(),
            invocation.command.registeredArguments(),
            nonEagerInvs,
            invocation.argumentInvocations,
        ).throwErrors()

        validateParameters(
            invocation.command.currentContext,
            invocation.optionInvocations
        ).throwErrors()
    }
}

private fun List<UsageError>.throwErrors() {
    MultiUsageError.buildOrNull(this)?.let { throw it }
}
