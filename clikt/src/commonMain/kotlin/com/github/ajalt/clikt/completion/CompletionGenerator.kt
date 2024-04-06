package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.PrintCompletionMessage

internal object CompletionGenerator {
    fun throwCompletionMessage(command: BaseCliktCommand<*>, shell: String): Nothing {
        throw getCompletionMessage(command, shell)
    }

    fun getCompletionMessage(command: BaseCliktCommand<*>, shell: String): PrintCompletionMessage {
        val message = when (shell.trim().lowercase()) {
            "fish" -> FishCompletionGenerator.generateFishCompletion(command = command)
            "zsh" -> BashCompletionGenerator.generateBashOrZshCompletion(
                command = command,
                zsh = true
            )

            else -> BashCompletionGenerator.generateBashOrZshCompletion(
                command = command,
                zsh = false
            )
        }
        return PrintCompletionMessage(message)
    }
}
