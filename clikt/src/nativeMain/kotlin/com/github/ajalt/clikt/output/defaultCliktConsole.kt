package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.mpp.isWindowsMpp
import com.github.ajalt.clikt.mpp.readEnvvar
import platform.posix.*

actual fun defaultCliktConsole() = object : CliktConsole {
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

    override val lineSeparator
        get() = if (isWindowsMpp() && !isRunningInMingw()) "\r\n" else "\n"

    private fun isRunningInMingw(): Boolean =
            readEnvvar("OS") == "Windows_NT"
}
