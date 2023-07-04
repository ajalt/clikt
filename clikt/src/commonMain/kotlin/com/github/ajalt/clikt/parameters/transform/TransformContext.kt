package com.github.ajalt.clikt.parameters.transform

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal

interface TransformContext {
    /** The current context object */
    val context: Context

    /** Throw an exception indicating that usage was incorrect. */
    fun fail(message: String): Nothing
}

/** Issue a message that can be shown to the user */
fun TransformContext.message(message: String) {
    context.command.issueMessage(message)
}

/** The terminal for the current context */
val TransformContext.terminal: Terminal get() = context.terminal

/** The theme for the current context */
val TransformContext.theme: Theme get() = terminal.theme

data class HelpTransformContext(override val context: Context) : TransformContext {
    override fun fail(message: String): Nothing {
        throw UsageError(message)
    }
}
