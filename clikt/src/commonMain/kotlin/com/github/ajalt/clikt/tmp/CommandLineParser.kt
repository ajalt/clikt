package com.github.ajalt.clikt.tmp

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.internal.finalizeParameters
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parsers.CommandInvocation
import com.github.ajalt.clikt.parsers.CommandLineParseResult
import com.github.ajalt.clikt.parsers.Invocation
import com.github.ajalt.clikt.parsers.shlex

// TODO: move this back to parsers package

object CommandLineParser {
    fun tokenize(
        commandLine: String,
        localization: Localization = defaultLocalization,
    ): List<String> {
        return shlex("TODO", commandLine, localization)// TODO
    }

    // does not throw
    fun parse(command: CliktCommand, originalArgv: List<String>): CommandLineParseResult {
        return parseArgv(command, originalArgv)
    }

    fun finalize(invocation: CommandInvocation) {
        finalizeParameters(
            invocation.command.currentContext,
            invocation.command.registeredOptions(),
            invocation.command.registeredParameterGroups(),
            invocation.optionInvocations,
            invocation.argumentInvocations,
        )
    }
}
