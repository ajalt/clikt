package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.mpp.isWindowsMpp
import platform.posix.*

fun defaultNativeCliktConsole() = object : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        // hideInput is not currently implemented
        if (isatty(STDIN_FILENO) != 0) {
            println(prompt)
        }
        return readLine()
    }

    override fun print(text: String, error: Boolean) {
        if (error) {
            fprintf(stderr, text)
            fflush(stderr)
        } else {
            print(text)
        }
    }

    override val lineSeparator get() = if (isWindowsMpp()) "\r\n" else "\n"
}
