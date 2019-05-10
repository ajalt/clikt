package com.github.ajalt.clikt.output

import platform.posix.STDIN_FILENO
import platform.posix.isatty

actual fun defaultCliktConsole() = object : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        if (isatty(STDIN_FILENO) != 0) {
            println(prompt)
        }
        return readLine()
    }

    override fun print(text: String, error: Boolean) {
        println(text)
    }

    override val lineSeparator get() = "\n"

}
