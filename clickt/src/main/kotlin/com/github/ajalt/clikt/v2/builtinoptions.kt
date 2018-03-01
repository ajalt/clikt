package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.PrintHelpMessage

fun helpOption()  = EagerOption("Show this message and exit", listOf("--help"), { ctx, _ ->
    throw PrintHelpMessage(ctx.command)
})
