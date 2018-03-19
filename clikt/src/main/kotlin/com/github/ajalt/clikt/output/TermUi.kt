package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import java.io.IOError
import java.io.IOException

object TermUi {
    /**
     * Edit [text] in the [editor].
     *
     * This blocks until the editor is closed.
     *
     * @param text The text to edit.
     * @param editor The path to the editor to use. Defaults to automatic detection.
     * @param env Environment variables to forward to the editor.
     * @param requireSave If true, null will be returned if the editor is closed without saving.
     * @param extension The extension of the temporary file that the editor will open. This can affect syntax
     *   coloring etc.
     * @return The edited text, or null if [requireSave] is true and the editor was closed without saving.
     * @throws CliktError if the editor cannot be opened.
     */
    fun editText(text: String, editor: String? = null, env: Map<String, String> = emptyMap(),
                 requireSave: Boolean = false, extension: String = ".txt"): String? {
        return Editor(editor, env, requireSave, extension).edit(text)
    }

    /**
     * Edit the file with [filename] in the [editor].
     *
     * @see editText for usage and parameter descriptions.
     */
    fun editFile(filename: String, editor: String? = null, env: Map<String, String> = emptyMap(),
                 requireSave: Boolean = false, extension: String = ".txt") {
        Editor(editor, env, requireSave, extension).editFile(filename)
    }

    // TODO finish docs. Default is a string since it's shown the the user. Wouldn't want to map it to
    // something that doesn't have toString
    /**
     * Prompt a user for text input.
     *
     * @return the user's input, or null if the stdin is not interactive and EOF was encountered.
     */
    fun <T> prompt(text: String,
                   default: String? = null,
                   hideInput: Boolean = false,
                   requireConfirmation: Boolean = false,
                   confirmationPrompt: String = "Repeat for confirmation: ",
                   promptSuffix: String = ": ",
                   showDefault: Boolean = true,
                   convert: ((String) -> T)? = null): T? {
        val prompt = buildPrompt(text, promptSuffix, showDefault, default)

        try {
            while (true) {
                var value: String
                while (true) {
                    value = promptForLine(prompt, hideInput) ?: return null

                    if (value.isNotBlank()) break
                    // Skip confirmation prompt if default is used
                    else if (default != null) return convert?.invoke(default)
                }
                val result = try {
                    convert?.invoke(value)
                } catch (err: UsageError) {
                    println(err.helpMessage(null))
                    continue
                }

                if (!requireConfirmation) return result

                var value2: String
                while (true) {
                    value2 = promptForLine(confirmationPrompt, hideInput) ?: return null
                    // No need to convert the confirmation, since it is valid if it matches the
                    // first value.
                    if (value2.isNotEmpty()) break
                }
                if (value == value2) return result
                println("Error: the two entered values do not match")
            }
        } catch (err: IOError) {
            return null
        }
    }

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
     * @return the user's response, or null if stdin is not interactive and EOF was encountered.
     */
    fun confirm(text: String, default: Boolean = false, abort: Boolean = false,
                promptSuffix: String = ": ", showDefault: Boolean = true): Boolean? {
        val prompt = buildPrompt(text, promptSuffix, showDefault,
                if (default) "Y/n" else "y/N")
        val rv: Boolean
        l@ while (true) {
            val input = promptForLine(prompt, false)?.trim() ?: return null
            rv = when (input) {
                "y", "yes" -> true
                "n", "no" -> false
                "" -> default
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

    /**
     * Print [prompt] to stdout and read a line from stdin.
     *
     * @param prompt The text to display to the user
     * @param hideInput If true, the user's input will not be echoed to the screen. If either stdin or stdout
     *   is not interactive, this argument is ignored.
     * @return A line from stdin, or null if an IOError occurs.
     */
    fun promptForLine(prompt: String, hideInput: Boolean): String? {
        val console = System.console()
        return if (console != null) {
            when {
                hideInput -> console.readPassword(prompt)?.let { String(it) }
                else -> console.readLine(prompt)
            }
        } else {
            try {
                System.out.print(prompt)
                System.`in`.bufferedReader().readLine()
            } catch (err: IOException) {
                null
            }
        }
    }

    /** True if the current platform is a version of windows. */
    val isWindows: Boolean
        get() = System.getProperty("os.name")
                .contains(Regex("windows", RegexOption.IGNORE_CASE))

    private fun buildPrompt(text: String, suffix: String, showDefault: Boolean,
                            default: String?) = buildString {
        append(text)
        if (!default.isNullOrBlank() && showDefault) {
            append(" [").append(default).append("]")
        }
        append(suffix)
    }
}
