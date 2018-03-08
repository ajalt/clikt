package com.github.ajalt.clikt.samples.echo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class Echo : CliktCommand(help = "Echo the STRING(s) to standard output") {
    private val suppressNewline by option("-n", help = "do not output the trailing newline").flag()
    private val strings by argument(help = "do not output the trailing newline").multiple()
    override fun run() {
        print(strings.joinToString(separator = " ", postfix = if (suppressNewline) "" else "\n"))
    }
}

fun main(args: Array<String>) = Echo().main(args)
