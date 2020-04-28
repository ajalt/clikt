package com.github.ajalt.clikt.output

import kotlinx.cinterop.toKString
import platform.posix.*

class CliktConsolePosix : CliktConsole {
    private val defaultNativeCliktConsole = defaultNativeCliktConsole()

    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        return if (isatty(STDIN_FILENO) == 1 && hideInput) {
            getpass(prompt)?.toKString()
        } else {
            print(prompt)
            readLine()
        }
    }

    override fun print(text: String, error: Boolean) {
        defaultNativeCliktConsole.print(text, error)
    }

    override val lineSeparator get() = defaultNativeCliktConsole.lineSeparator
}

actual fun defaultCliktConsole(): CliktConsole = CliktConsolePosix()
