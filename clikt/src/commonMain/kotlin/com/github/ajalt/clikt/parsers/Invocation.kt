package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option

/**
 * The output of parsing a single option and its values.
 */
data class OptionInvocation(
    /**
     * The name that was used to invoke the option. May be empty if the value was not retrieved
     * from the command line (e.g. values from environment variables).
     */
    val name: String,
    /**
     * The values provided to the option. This will always have a size in the range of
     * [Option.nvalues] for the option that was invoked.
     */
    val values: List<String>,
)

/**
 * The output of parsing a single argument and its values.
 */
class ArgumentInvocation(
    /**
     * The argument that was invoked.
     */
    val argument: Argument,
    /**
     * The values provided to the argument. This will always have a size in the range of
     * [Argument.nvalues] for the argument that was invoked.
     */
    val values: List<String>,
)

/**
 * The output of parsing a single command and its options and arguments.
 */
class CommandInvocation<T : BaseCliktCommand<T>>(
    val command: T,
    val optionInvocations: Map<Option, List<OptionInvocation>>,
    val argumentInvocations: List<ArgumentInvocation>,
    /**
     * The subcommands of this command that were invoked.
     *
     * This list will have at most one entry unless [allowMultipleSubcommands][BaseCliktCommand.allowMultipleSubcommands] is
     * true.
     */
    val subcommandInvocations: List<CommandInvocation<T>>,
    /**
     * The errors that occurred while parsing this command.
     */
    val errors: List<CliktError>,
)

/**
 * The result of parsing a command line.
 */
class CommandLineParseResult<T : BaseCliktCommand<T>>(
    /**
     * The root command that was parsed.
     */
    val invocation: CommandInvocation<T>,
    /**
     * The original command line tokens that were passed to `parse`.
     */
    val originalArgv: List<String>,
    /**
     * The full argv after expanding any [argument file tokens][Context.argumentFileReader] or
     * [aliases][BaseCliktCommand.aliases].
     */
    val expandedArgv: List<String>,
)

/**
 * Flatten a command invocation into a sequence of invocations.
 *
 * This will yield the root command invocation, followed by all subcommand invocations in the order
 * they were invoked on the command line.
 *
 * You can use this to avoid recursion when processing invocations.
 *
 * ### Example
 *
 * ```
 * val invocations = rootInvocation.flatten()
 * try {
 *   for (inv in invocations) {
 *     run(inv.command)
 *   }
 * } finally {
 *   invocations.close()
 * }
 * ```
 *
 * @param finalize If true (the default), finalize all commands as they are emitted in the sequence. If false, you
 * must call [CommandLineParser.finalize] on each invocation yourself before running the command.
 */
fun <T : BaseCliktCommand<T>> CommandInvocation<T>.flatten(finalize: Boolean = true): FlatInvocations<T> {
    return FlatInvocations(this, finalize)
}

// TODO: this should be an AutoClosable once that interface is stable
class FlatInvocations<T : BaseCliktCommand<T>> internal constructor(
    root: CommandInvocation<T>, private val finalize: Boolean,
) : Sequence<CommandInvocation<T>> {
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
    }.onEach { if (finalize) CommandLineParser.finalize(it) }

    override fun iterator(): Iterator<CommandInvocation<T>> = seq.iterator()

    /**
     * [Close][Context.close] all open contexts of invoked commands.
     */
    fun close() {
        closables.forEach { it.close() }
    }
}
