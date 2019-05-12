package com.github.ajalt.clikt.completion

/**
 * Configurations for generating shell autocomplete suggestions
 */
sealed class CompletionCandidates {
    /** Do not autocomplete this parameter */
    object None : CompletionCandidates()
    /** Complete with filesystem paths */
    object Path : CompletionCandidates()
    /** Complete with entries in the system's hostfile */
    object Hostname : CompletionCandidates()
    /** Complete with usernames from the current system */
    object Username : CompletionCandidates()
    /** Complete the parameter with a fixed set of string */
    data class Fixed(val candidates: Set<String>) : CompletionCandidates()
}
