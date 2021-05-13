package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.mpp.isWindowsMpp

object TermUi {
    /**
     * Print the [message] to the screen.
     *
     * This is similar to [print] or [println], but converts newlines to the system line separator.
     *
     * @param message The message to print.
     * @param trailingNewline If true, behave like [println], otherwise behave like [print]
     * @param err If true, print to stderr instead of stdout
     * @param console The console to echo to
     * @param lineSeparator The line separator to use, defaults to the [console]'s `lineSeparator`
     */
    fun echo(
            message: Any?,
            trailingNewline: Boolean = true,
            err: Boolean = false,
            console: CliktConsole = defaultCliktConsole(),
            lineSeparator: String = console.lineSeparator
    ) {
        val text = message?.toString()?.replace(Regex("\r?\n"), lineSeparator) ?: "null"
        console.print(if (trailingNewline) text + lineSeparator else text, err)
    }

    /**
     * Edit [text] in the [editor].
     *
     * This blocks until the editor is closed.
     *
     * @param text The text to edit.
     * @param editor The path to the editor to use. Defaults to automatic detection.
     * @param env Environment variables to forward to the editor.
     * @param requireSave If the editor is closed without saving, null will be returned if true, otherwise
     *   [text] will be returned.
     * @param extension The extension of the temporary file that the editor will open. This can affect syntax
     *   coloring etc.
     * @return The edited text, or null if [requireSave] is true and the editor was closed without saving.
     * @throws CliktError if the editor cannot be opened.
     */
    fun editText(
            text: String,
            editor: String? = null,
            env: Map<String, String> = emptyMap(),
            requireSave: Boolean = false,
            extension: String = ".txt"
    ): String? {
        return createEditor(editor, env, requireSave, extension).edit(text)
    }

    /**
     * Edit the file with [filename] in the [editor].
     *
     * @see editText for usage and parameter descriptions.
     */
    fun editFile(
            filename: String,
            editor: String? = null,
            env: Map<String, String> = emptyMap(),
            requireSave: Boolean = false,
            extension: String = ".txt"
    ) {
        createEditor(editor, env, requireSave, extension).editFile(filename)
    }

    /**
     * Prompt a user for text input.
     *
     * If the user sends a terminate signal (e.g. ctrl-c) while the prompt is active, null will be returned.
     *
     * @param text The text to display for the prompt.
     * @param default The default value to use for the input. If the user enters a newline without any other
     *   value, [default] will be returned. This parameter is a String instead of [T], since it will be
     *   displayed to the user.
     * @param hideInput If true, the user's input will not be echoed back to the screen. This is commonly used
     *   for password inputs.
     * @param requireConfirmation If true, the user will be required to enter the same value twice before it
     *   is accepted.
     * @param confirmationPrompt The text to show the user when [requireConfirmation] is true.
     * @param promptSuffix A delimiter printed between the [text] and the user's input.
     * @param showDefault If true, the [default] value will be shown as part of the prompt.
     * @param convert A callback that will convert the text that the user enters to the return value of the
     *   function. If the callback raises a [UsageError], its message will be printed and the user will be
     *   asked to enter a new value. If [default] is not null and the user does not input a value, the value
     *   of [default] will be passed to this callback.
     * @return the user's input, or null if the stdin is not interactive and EOF was encountered.
     */
    fun <T> prompt(
            text: String,
            default: String? = null,
            hideInput: Boolean = false,
            requireConfirmation: Boolean = false,
            confirmationPrompt: String = "Repeat for confirmation: ",
            promptSuffix: String = ": ",
            showDefault: Boolean = true,
            console: CliktConsole = defaultCliktConsole(),
            convert: ((String) -> T)
    ): T? {
        val prompt = buildPrompt(text, promptSuffix, showDefault, default)

        try {
            while (true) {
                var value: String
                while (true) {
                    value = console.promptForLine(prompt, hideInput) ?: return null

                    if (value.isNotBlank()) break
                    // Skip confirmation prompt if default is used
                    else if (default != null) return convert.invoke(default)
                }
                val result = try {
                    convert.invoke(value)
                } catch (err: UsageError) {
                    echo(err.helpMessage(), console = console)
                    continue
                }

                if (!requireConfirmation) return result

                var value2: String
                while (true) {
                    value2 = console.promptForLine(confirmationPrompt, hideInput) ?: return null
                    // No need to convert the confirmation, since it is valid if it matches the
                    // first value.
                    if (value2.isNotEmpty()) break
                }
                if (value == value2) return result
                echo("Error: the two entered values do not match", console = console)
            }
        } catch (err: Exception) {
            return null
        }
    }

    /**
     * Prompt a user for text input.
     *
     * If the user sends a terminate signal (e.g. ctrl-c) while the prompt is active, null will be returned.
     *
     * @param text The text to display for the prompt.
     * @param default The default value to use for the input. If the user enters a newline without any other
     *   value, [default] will be returned.
     * @param hideInput If true, the user's input will not be echoed back to the screen. This is commonly used
     *   for password inputs.
     * @param requireConfirmation If true, the user will be required to enter the same value twice before it
     *   is accepted.
     * @param confirmationPrompt The text to show the user when [requireConfirmation] is true.
     * @param promptSuffix A delimiter printed between the [text] and the user's input.
     * @param showDefault If true, the [default] value will be shown as part of the prompt.
     * @return the user's input, or null if the stdin is not interactive and EOF was encountered.
     */
    fun prompt(
            text: String,
            default: String? = null,
            hideInput: Boolean = false,
            requireConfirmation: Boolean = false,
            confirmationPrompt: String = "Repeat for confirmation: ",
            promptSuffix: String = ": ",
            showDefault: Boolean = true
    ): String? {
        return prompt(text, default, hideInput, requireConfirmation,
                confirmationPrompt, promptSuffix, showDefault) { it }
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
    fun confirm(
            text: String,
            default: Boolean = false,
            abort: Boolean = false,
            promptSuffix: String = ": ",
            showDefault: Boolean = true,
            console: CliktConsole = defaultCliktConsole()
    ): Boolean? {
        val prompt = buildPrompt(text, promptSuffix, showDefault,
                if (default) "Y/n" else "y/N")
        val rv: Boolean
        l@ while (true) {
            val input = console.promptForLine(prompt, false)?.trim()?.lowercase() ?: return null
            rv = when (input) {
                "y", "yes" -> true
                "n", "no" -> false
                "" -> default
                else -> {
                    echo("Error: invalid input", console = console)
                    continue@l
                }
            }
            break
        }
        if (abort && !rv) throw Abort()
        return rv
    }

    /** True if the current platform is a version of windows. */
    val isWindows: Boolean get() = isWindowsMpp()

    private fun buildPrompt(text: String, suffix: String, showDefault: Boolean,
                            default: String?) = buildString {
        append(text)
        if (!default.isNullOrBlank() && showDefault) {
            append(" [").append(default).append("]")
        }
        append(suffix)
    }
}
