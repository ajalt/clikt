package com.github.ajalt.clikt.core

/**
 * A [CliktCommand] that has a default implementation of [CliktCommand.run] that is a no-op.
 */
open class NoRunCliktCommand(help: String = "",
                             epilog: String = "",
                             name: String? = null)
    : CliktCommand(help, epilog, name, false) {
    override fun run() = Unit
}
