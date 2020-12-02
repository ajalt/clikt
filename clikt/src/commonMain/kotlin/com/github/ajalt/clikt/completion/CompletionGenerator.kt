package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.CliktCommand

object CompletionGenerator {
    fun generateBashCompletion(command: CliktCommand): String {
        return BashCompletionGenerator.generateBashOrZshCompletion(command, zsh = false)
    }

    fun generateZshCompletion(command: CliktCommand): String {
        return BashCompletionGenerator.generateBashOrZshCompletion(command, zsh = true)
    }

    fun generateFishCompletion(command: CliktCommand): String {
        return FishCompletionGenerator.generateFishCompletion(command)
    }
}
