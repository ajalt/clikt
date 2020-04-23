package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.mpp.isWindowsMpp
import com.github.ajalt.clikt.mpp.readEnvvar

actual fun defaultCliktConsole(): CliktConsole = object : CliktConsole {
    private val nativeCliktConsole = defaultNativeCliktConsole()

    override fun promptForLine(prompt: String, hideInput: Boolean): String? =
            nativeCliktConsole.promptForLine(prompt, hideInput)

    override fun print(text: String, error: Boolean) {
        nativeCliktConsole.print(text, error)
    }

    override val lineSeparator: String
        get() = if (isWindowsMpp() && !isRunningInMingw())
            "\r\n" else "\n"

    private fun isRunningInMingw(): Boolean = readEnvvar("MSYSTEM")
            ?.toLowerCase()?.contains("mingw") ?: false
}
