package com.github.ajalt.clikt.core

/**
 * A [CliktCommand] that has a default implementation of [CliktCommand.run] that is a no-op.
 */
open class NoOpCliktCommand(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = "",
    allowMultipleSubcommands: Boolean = false,
    hidden: Boolean = false,
) : BaseCliktCommand<() -> Unit>(
    help,
    epilog,
    name,
    invokeWithoutSubcommand,
    printHelpOnEmptyArgs,
    helpTags,
    autoCompleteEnvvar,
    allowMultipleSubcommands,
    hidden
) {
    final override val runner: () -> Unit = {}
}
