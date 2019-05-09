package com.github.ajalt.clikt.output

import java.io.Console
import java.io.IOException

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
        readLine() ?: throw RuntimeException("EOF")
    } catch (err: IOException) {
        throw err
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

actual fun defaultCliktConsole(): CliktConsole {
    return System.console()?.let { InteractiveCliktConsole(it) }
            ?: NonInteractiveCliktConsole()
}
