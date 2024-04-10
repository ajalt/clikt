package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
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

fun <T: BaseCliktCommand<T>> CommandInvocation<T>.flatten(): FlatInvocations<T> {
    return FlatInvocations(this)
}

class FlatInvocations<T: BaseCliktCommand<T>> internal constructor(
    root: CommandInvocation<T>,
): Sequence<CommandInvocation<T>> {
    private val closables = mutableListOf<Context>()
    private val seq = sequence {
        suspend fun SequenceScope<CommandInvocation<T>>.yieldSubs(inv: CommandInvocation<T>) {
            closables.add(inv.command.currentContext)
            yield(inv)
            for (sub in inv.subcommandInvocations) {
                yieldSubs(sub)
            }
            closables.removeLast().close()
        }
        yieldSubs(root)
    }

    override fun iterator(): Iterator<CommandInvocation<T>> = seq.iterator()

    fun close() {
        closables.forEach { it.close() }
    }
}

fun <T: BaseCliktCommand<T>> CommandLineParseResult<T>.flatten(): Sequence<CommandInvocation<T>> {
    return sequence {
        yield(invocation)
        yieldAll(invocation.subcommandInvocations)
    }
}
