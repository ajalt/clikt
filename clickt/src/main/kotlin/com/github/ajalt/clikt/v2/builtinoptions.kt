package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.PrintHelpMessage
import com.github.ajalt.clikt.parser.PrintMessage

internal fun helpOption(names: Set<String>, message: String) = EagerOption(message, names, { ctx, _ ->
    throw PrintHelpMessage(ctx.command)
})

inline fun CliktCommand.versionOption(
        version: String,
        help: String = "Show the version and exit.",
        names: Set<String> = setOf("--version"),
        crossinline message: (String) -> String = { "$name version $version" }): CliktCommand {
    registerOption(EagerOption(help, names) { _, _ ->
        throw PrintMessage(message(version))
    })
    return this
}
