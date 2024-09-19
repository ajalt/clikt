package com.github.ajalt.clikt.command

/**
 * A [CoreSuspendingCliktCommand] that has a default implementation of
 * [run][CoreSuspendingCliktCommand.run] that is a no-op.
 */
abstract class CoreSuspendingNoOpCliktCommand(
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
) : CoreSuspendingCliktCommand(name) {
    override suspend fun run() = Unit
}
