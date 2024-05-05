package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.choice

private val choices = arrayOf("bash", "zsh", "fish")

/**
 * A subcommand that will print a completion script for the given shell when invoked.
 */
class CompletionCommand(
    private val help: String = "Generate a tab-complete script for the given shell",
    private val epilog: String = "",
    name: String = "generate-completion",
) : CliktCommand(name) {
    override fun help(context: Context): String = help
    override fun helpEpilog(context: Context): String = epilog
    private val shell by argument("shell").choice(*choices)
    override fun run() {
        val cmd = currentContext.parent?.command ?: this
        throw PrintCompletionMessage(CompletionGenerator.generateCompletionForCommand(cmd, shell))
    }
}
