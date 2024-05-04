package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.core.Context.Companion.TERMINAL_KEY
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal

/**
 * The terminal to used to read and write messages.
 */
val Context.terminal: Terminal
    get() = findObject(TERMINAL_KEY) ?: Terminal().also {
        // Set the terminal on the root so that we don't create multiple
        selfAndAncestors().last().data[TERMINAL_KEY] = it
    }

/** The current terminal's theme */
val Context.theme: Theme get() = terminal.theme

/**
 * The terminal that will handle reading and writing text.
 */
var Context.Builder.terminal: Terminal
    get() = data[TERMINAL_KEY] as? Terminal
        ?: parent?.terminal
        ?: Terminal().also { data[TERMINAL_KEY] = it }
    set(value) {
        data[TERMINAL_KEY] = value
    }


internal fun Context.selfAndAncestors() = generateSequence(this) { it.parent }


/**
 * A shortcut for accessing the terminal from the [currentContext][CliktCommand.currentContext]
 */
val BaseCliktCommand<*>.terminal: Terminal
    get() = currentContext.terminal

/**
 * Set up this command's context to use Mordant for rendering.
 *
 * This is done automatically for [CliktCommand]s, but you can call this if you are making a custom
 * command class.
 */
fun BaseCliktCommand<*>.installMordant() {
    configureContext {
        echoer = MordantEchoer
        helpFormatter = { parent?.helpFormatter?.invoke(it) ?: MordantHelpFormatter(it) }
    }
}

private object MordantEchoer: TerminalEchoer {
    override fun echo(context: Context,message: Any?, trailingNewline: Boolean, err: Boolean) {
        if (trailingNewline) {
            context.terminal.println(message, stderr = err)
        } else {
            context.terminal.print(message, stderr = err)
        }
    }
}
