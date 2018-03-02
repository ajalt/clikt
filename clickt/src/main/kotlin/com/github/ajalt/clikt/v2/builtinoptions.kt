package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.PrintHelpMessage

internal fun helpOption(names: Set<String>, message: String)  = EagerOption(message, names, { ctx, _ ->
    throw PrintHelpMessage(ctx.command)
})
