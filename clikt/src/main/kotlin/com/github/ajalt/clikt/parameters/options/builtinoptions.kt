package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.CliktCommand

internal fun helpOption(names: Set<String>, message: String) = EagerOption(message, names, { ctx, _ ->
    throw PrintHelpMessage(ctx.command)
})

inline fun <T: CliktCommand> T.versionOption(
        version: String,
        help: String = "Show the version and exit.",
        names: Set<String> = setOf("--version"),
        crossinline message: (String) -> String = { "$name version $it" }): T {
    registerOption(EagerOption(help, names) { _, _ ->
        throw PrintMessage(message(version))
    })
    return this
}
