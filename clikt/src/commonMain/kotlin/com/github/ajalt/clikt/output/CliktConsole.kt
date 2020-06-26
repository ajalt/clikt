package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context

/**
 * An object that is used by commands and parameters to show text to the user and read input.
 *
 * By default, stdin and stdout are used, but you can provide an implementation of this interface to
 * [Context.console] to customize the behavior.
 */
interface CliktConsole {
    /**
     * Show the [prompt] to the user, and return a line of their response.
     *
     * This function will block until a line of input has been read.
     *
     * @param prompt The text to display to the user
     * @param hideInput If true, the user's input should not be echoed to the screen. If the current console
     *   does not support hidden input, this argument may be ignored. Currently, this argument is
     *   ignored on JS and Native platforms.
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


/**
 * Return a CliktConsole for this platform.
 */
expect fun defaultCliktConsole(): CliktConsole
