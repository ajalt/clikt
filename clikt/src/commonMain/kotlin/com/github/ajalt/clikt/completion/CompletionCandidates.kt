package com.github.ajalt.clikt.completion

@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Deprecated(message = "This opt-in requirement is not used anymore. Remove its usages from your code.")
annotation class ExperimentalCompletionCandidates

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

    /** Complete the parameter with a fixed set of strings */
    data class Fixed(val candidates: Set<String>) : CompletionCandidates() {
        constructor(vararg candidates: String) : this(candidates.toSet())
    }

    /**
     * Complete the parameter with words emitted from a custom script.
     *
     * The [generator] takes the type of shell to generate a script for and returns code to add to
     * the generated completion script. If you just want to call another script or binary that
     * prints all possible completion words to stdout, you can use [fromStdout].
     *
     * ## Bash/ZSH
     *
     * Both Bash and ZSH scripts use Bash's Programmable Completion system (ZSH via a comparability
     * layer). The string returned from [generator] should be the body of a function that will be
     * passed to `compgen -F`.
     *
     * Specifically, you should set the variable `COMPREPLY` to the completion(s) for the current
     * word being typed. The word being typed can be retrieved from the `COMP_WORDS` array at index
     * `COMP_CWORD`.
     *
     * ## Fish
     *
     * The string returned from [generator] should be a Fish string with each completion suggestion
     * separated by a newline. Each completion can optionally end with a tab, followed by a
     * description of the suggestion.
     *
     * ```
     * """'
     * start
     * stop
     * help\\t"show the help for this command"
     * test\\t"run test suite"
     * '"""
     * ```
     *
     * You can also construct the string from a fish command or function call, e.g.
     *
     * ```
     * "\"(__fish_print_hostnames)\""
     * ```
     *
     * or
     *
     * ```
     * "\"(ls -1)\""
     * ```
     */
    data class Custom(val generator: (ShellType) -> String?) : CompletionCandidates() {
        enum class ShellType { BASH, FISH }
        companion object {
            fun fromStdout(command: String) = Custom {
                when(it) {
                    ShellType.FISH -> "\"($command)\""
                    else -> "COMPREPLY=(\$(compgen -W \"\$($command)\" -- \"\${COMP_WORDS[\$COMP_CWORD]}\"))"
                }
            }
        }
    }
}
