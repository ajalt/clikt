package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintCompletionMessage

internal object CompletionGenerator {
    fun throwCompletionMessage(command: CliktCommand, shell: String): Nothing {
        val message = when(shell.trim().lowercase()) {
            "fish"  -> FishCompletionGenerator.generateFishCompletion(command = command)
            "zsh"  -> BashCompletionGenerator.generateBashOrZshCompletion(command = command, zsh = true)
            else -> BashCompletionGenerator.generateBashOrZshCompletion(command = command, zsh = false)
        }
        throw PrintCompletionMessage(message, forceUnixLineEndings = true)
    }
}
