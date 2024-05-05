package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.completion.CompletionGenerator.generateCompletionForCommand
import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate

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
