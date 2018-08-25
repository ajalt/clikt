package com.github.ajalt.clikt.output

import java.io.Console
import java.io.IOException

interface CliktConsole {
    /**
     * Show the [prompt] to the user, and return a line of their response.
     *
     * This function will block until a line of input has been read.
     *
     * @param prompt The text to display to the user
     * @param hideInput If true, the user's input should not be echoed to the screen. If the current console
     *   does not support hidden input, this argument may be ignored..
     * @return A line of user input, or null if an error occurred.
     */
    fun promptForLine(prompt: String, hideInput: Boolean): String?

    /**
     * Show some [text] to the user.
     *
     * @param text The text to display. May or may not contain a tailing newline.
     * @param error If true, the [text] is an error message, and should be printed in an alternate stream or
     *   format, if applicable.
     */
    fun print(text: String, error: Boolean)

    /**
     * The line separator to use. (Either "\n" or "\r\n")
     */
    val lineSeparator: String
}

class InteractiveCliktConsole(private val console: Console) : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean) = when {
        hideInput -> console.readPassword(prompt)?.let { String(it) }
        else -> console.readLine(prompt)
    }

    override fun print(text: String, error: Boolean) {
        if (error) {
            System.err
        } else {
            System.out
        }.print(text)
    }

    override val lineSeparator: String get() = System.lineSeparator()
}

class NonInteractiveCliktConsole : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean) = try {
        print(prompt, false)
        System.`in`.bufferedReader().readLine()
    } catch (err: IOException) {
        null
    }

    override fun print(text: String, error: Boolean) {
        if (error) {
            System.err
        } else {
            System.out
        }.print(text)
    }

    override val lineSeparator: String get() = System.lineSeparator()
}

fun defaultCliktConsole(): CliktConsole {
    return System.console()?.let { InteractiveCliktConsole(it) }
            ?: NonInteractiveCliktConsole()
}
