package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.BaseCliktCommand

object CompletionGenerator {
    /**
     * Generate a completion script for the given shell and command.
     *
     * @param command The command to generate a completion script for.
     * @param shell The shell to generate a completion script for. One of "bash", "zsh", or "fish".
     *   If any other value is provided, the script will be generated for bash.
     */
    fun generateCompletionForCommand(command: BaseCliktCommand<*>, shell: String): String {
        return when (shell.trim().lowercase()) {
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
    }
}
