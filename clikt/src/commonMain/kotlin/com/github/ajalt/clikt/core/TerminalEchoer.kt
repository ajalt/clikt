package com.github.ajalt.clikt.core

interface TerminalEchoer {
    fun echo(
        context: Context,
        message: Any?,
        trailingNewline: Boolean = true,
        err: Boolean = false,
    )
}
