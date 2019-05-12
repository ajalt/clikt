package com.github.ajalt.clikt.core

/**
 * A [CliktCommand] that has a default implementation of [CliktCommand.run] that is a no-op.
 */
open class NoRunCliktCommand(
        help: String = "",
        epilog: String = "",
        name: String? = null,
        invokeWithoutSubcommand: Boolean = false,
        printHelpOnEmptyArgs: Boolean = false,
        helpTags: Map<String, String> = emptyMap(),
        autoCompleteEnvvar: String? = ""
) : CliktCommand(help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs, helpTags, autoCompleteEnvvar) {
    override fun run() = Unit
}
