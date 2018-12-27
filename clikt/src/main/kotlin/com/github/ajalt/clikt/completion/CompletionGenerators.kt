package com.github.ajalt.clikt.completion

/**
 * Built-in [CompletionGenerator]s.
 *
 * @author Laurent Pireyn
 */
object CompletionGenerators {
    fun getCompletionGenerator(shell: String): CompletionGenerator? =
        when (shell) {
            "bash" -> BashCompletionGenerator
            else -> null
        }
}
