package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option

/**
 * The output of parsing a single option and its values.
 *
 * @param name The name that was used to invoke the option. May be empty if the value was not retrieved
 *   from the command line (e.g. values from environment variables).
 * @param values The values provided to the option. All invocations passed to [Option.finalize]
 *   will have a size in the range of [Option.nvalues].
 */
data class Invocation(val name: String, val values: List<String>)

// TODO: docs
data class ArgumentInvocation(val argument: Argument, val values: List<String>)

data class CommandInvocation<T: BaseCliktCommand<T>>(
    val command: T,
    val optionInvocations: Map<Option, List<Invocation>>,
    val argumentInvocations: List<ArgumentInvocation>,
    val subcommandInvocations: List<CommandInvocation<T>>,
    val errors: List<CliktError>,
)

class CommandLineParseResult<T: BaseCliktCommand<T>>(
    val invocation: CommandInvocation<T>,
    val originalArgv: List<String>,
    val expandedArgv: List<String>,
)
