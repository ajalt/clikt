package com.github.ajalt.clikt.core

/**
 * A [CoreCliktCommand] that has a default implementation of [CoreCliktCommand.run] that is a no-op.
 */
open class NoOpCliktCommand(
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
) : CoreCliktCommand(name) {
    final override fun run() {}
}
