package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.CliktCommand

/**
 * Generator of auto completion commands for a shell.
 *
 * @author Laurent Pireyn
 */
interface CompletionGenerator {
    fun generateCompletion(command: CliktCommand): String
}
