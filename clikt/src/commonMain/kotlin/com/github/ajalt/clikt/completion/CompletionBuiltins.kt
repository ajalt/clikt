package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.completion.CompletionGenerator.generateCompletionForCommand
import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.choice

private val choices = arrayOf("bash", "zsh", "fish")

/**
 * Add an option to a command that will print a completion script for the given shell when invoked.
 */
fun <T : BaseCliktCommand<*>> T.completionOption(
    vararg names: String = arrayOf("--generate-completion"),
    help: String = "",
    hidden: Boolean = false,
): T = apply {
    registerOption(option(
        *names, help = help, hidden = hidden, eager = true,
        metavar = choices.joinToString("|", prefix = "(", postfix = ")")
    ).validate {
        throw PrintCompletionMessage(generateCompletionForCommand(context.command, it))
    })
}

/**
 * A subcommand that will print a completion script for the given shell when invoked.
 */
class CompletionCommand(
    private val help: String = "Generate a tab-complete script for the given shell",
    private val epilog: String = "",
    name: String = "generate-completion",
) : CoreCliktCommand(name) {
    override fun help(context: Context): String = help
    override fun helpEpilog(context: Context): String = epilog
    private val shell by argument("shell").choice(*choices)
    override fun run() {
        val cmd = currentContext.parent?.command ?: this
        throw PrintCompletionMessage(generateCompletionForCommand(cmd, shell))
    }
}
