package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.internal.finalizeParameters
import com.github.ajalt.clikt.internal.validateParameters
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization

// TODO: move this back to parsers package

object CommandLineParser {
    fun tokenize(
        commandLine: String,
        localization: Localization = defaultLocalization,
    ): List<String> {
        return shlex("TODO", commandLine, localization)// TODO
    }

    // TODO: docs does not throw
    fun <RunnerT : Function<*>> parse(
        command: BaseCliktCommand<RunnerT>,
        originalArgv: List<String>,
    ): CommandLineParseResult<RunnerT> {
        return parseArgv(command, originalArgv)
    }

    fun finalize(invocation: CommandInvocation<*>) {
        val usageErrors = finalizeParameters(
            invocation.command.currentContext,
            invocation.command.registeredOptions(),
            invocation.command.registeredParameterGroups(),
            invocation.optionInvocations,
            invocation.argumentInvocations,
        )

        // Now that all parameters have been finalized, we can validate everything
        val validationErrors = validateParameters(
            invocation.command.currentContext,
            invocation.optionInvocations
        )

        MultiUsageError.buildOrNull(usageErrors + validationErrors)?.let { throw it }
    }
}
