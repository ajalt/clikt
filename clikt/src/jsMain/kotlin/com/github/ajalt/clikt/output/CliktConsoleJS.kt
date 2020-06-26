package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.mpp.isWindowsMpp
import com.github.ajalt.clikt.mpp.nodeRequire

private external val process: dynamic
private external val Buffer: dynamic

actual fun defaultCliktConsole(): CliktConsole {
    return try {
        NodeCliktConsole(nodeRequire("fs"))
    } catch (e: Exception) {
        BrowserCliktConsole()
    }
}

private class NodeCliktConsole(private val fs: dynamic) : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean): String? = buildString {
        // hideInput is not currently implemented
        println(prompt)

        var char: String
        val buf = Buffer.alloc(1)
        do {
            fs.readSync(fd = 0, bufer = buf, offset = 0, len = 1, position = null)
            char = buf.toString()
            append(char)
        } while (char != "\n")
    }

    override fun print(text: String, error: Boolean) {
        if (error) {
            process.stderr
        } else {
            process.stdout
        }.write(text)
    }

    override val lineSeparator: String get() = if (isWindowsMpp()) "\r\n" else "\n"
}

private class BrowserCliktConsole : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        return null
    }

    override fun print(text: String, error: Boolean) {
        println(text)
    }

    override val lineSeparator: String
        get() = "\n"
}
