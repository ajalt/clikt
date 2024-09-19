package com.github.ajalt.clikt.command

/**
 * A [SuspendingCliktCommand] that has a default implementation of
 * [run][SuspendingCliktCommand.run] that is a no-op.
 */
abstract class SuspendingNoOpCliktCommand(
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
) : SuspendingCliktCommand(name) {
    override suspend fun run() = Unit
}
