package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.choice

private val choices = arrayOf("bash", "zsh", "fish")

/**
 * Add an option to a command that will print a completion script for the given shell when invoked.
 */
fun <T : CliktCommand> T.completionOption(
    vararg names: String = arrayOf("--generate-completion"),
    help: String = "",
    hidden: Boolean = false,
): T = apply {
    registerOption(option(*names, help = help, hidden = hidden, eager = true,
        metavar = choices.joinToString("|", prefix = "(", postfix = ")")).validate {
        CompletionGenerator.throwCompletionMessage(context.command, it)
    })
}

/**
 * A subcommand that will print a completion script for the given shell when invoked.
 */
class CompletionCommand(
    help: String = "Generate a tab-complete script for the given shell",
    epilog: String = "",
    name: String = "generate-completion",
) : CliktCommand(help, epilog, name) {
    private val shell by argument("shell").choice(*choices)
    override fun run() {
        val cmd = currentContext.parent?.command ?: this
        CompletionGenerator.throwCompletionMessage(cmd, shell)
    }
}
