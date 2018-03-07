package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.CliktCommand

internal fun helpOption(names: Set<String>, message: String) = EagerOption(message, names, { ctx, _ ->
    throw PrintHelpMessage(ctx.command)
})

inline fun CliktCommand.versionOption( // TODO test
        version: String,
        help: String = "Show the version and exit.",
        names: Set<String> = setOf("--version"),
        crossinline message: (String) -> String = { "$name version $version" }): CliktCommand {
    registerOption(EagerOption(help, names) { _, _ ->
        throw PrintMessage(message(version))
    })
    return this
}
