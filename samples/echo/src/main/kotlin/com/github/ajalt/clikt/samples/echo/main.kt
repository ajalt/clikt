package com.github.ajalt.clikt.samples.echo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class Echo : CliktCommand(help = "Echo the STRING(s) to standard output") {
    val suppressNewline by option("-n", help = "do not output the trailing newline").flag()
    val strings by argument(help = "do not output the trailing newline").multiple()

    override fun run() {
        val message = if (strings.isEmpty()) String(System.`in`.readBytes())
        else strings.joinToString(" ", postfix = if (suppressNewline) "" else "\n")
        TermUi.echo(message, trailingNewline = false)
    }
}

fun main(args: Array<String>) = Echo().main(args)
