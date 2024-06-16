package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.core.Context.Companion.TERMINAL_KEY
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.mordant.platform.MultiplatformSystem
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
        // Only install mordant if we're the parent command so that we don't override inherited
        // settings.
        if (parent != null) return@configureContext
        helpFormatter = { MordantHelpFormatter(it) }
        readEnvvar = { MultiplatformSystem.readEnvironmentVariable(it) }
        readArgumentFile = { MultiplatformSystem.readFileAsUtf8(it) ?: throw FileNotFound(it) }
        exitProcess = { MultiplatformSystem.exitProcess(it) }
        echoMessage = { context: Context, message: Any?, trailingNewline: Boolean, err: Boolean ->
            if (trailingNewline) {
                context.terminal.println(message, stderr = err)
            } else {
                context.terminal.print(message, stderr = err)
            }
        }
    }
}

