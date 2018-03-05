package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Abort

object TermUi {
    /**
     * Prompt for user confirmation.
     *
     * Responses will be read from stdin, even if it's redirected to a file.
     *
     * @param text the question to ask
     * @param default the default, used if stdin is empty
     * @param abort if `true`, a negative answer aborts the program by raising [Abort]
     * @param promptSuffix a string added after the question and choices
     * @param showDefault if false, the choices will not be shown in the prompt.
     */
    fun confirm(text: String, default: Boolean = false, abort: Boolean = false,
                promptSuffix: String = ": ", showDefault: Boolean = true): Boolean {
        val prompt = buildPrompt(text, promptSuffix, showDefault,
                if (default) "Y/n" else "y/N")
        val rv: Boolean
        l@ while (true) {
            print(prompt)
            val input = readLine()?.trim()
            rv = when (input) {
                "y", "yes" -> true
                "n", "no" -> false
                "" -> default
                null -> throw Abort()
                else -> {
                    println("Error: invalid input")
                    continue@l
                }
            }
            break
        }
        if (abort && !rv) throw Abort()
        return rv
    }

    private fun buildPrompt(text: String, suffix: String, showDefault: Boolean,
                            default: String) = buildString {
        append(text)
        if (default.isNotBlank() && showDefault) {
            append(" [").append(default).append("]")
        }
        append(suffix)
    }
}
