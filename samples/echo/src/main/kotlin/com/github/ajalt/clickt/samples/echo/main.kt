package com.github.ajalt.clickt.samples.echo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.flag
import com.github.ajalt.clikt.parameters.multiple
import com.github.ajalt.clikt.parameters.option

class Echo : CliktCommand(help = "Echo the STRING(s) to standard output") {
    private val suppressNewline by option("-n", help = "do not output the trailing newline").flag()
    private val strings by argument(help = "do not output the trailing newline").multiple()
    override fun run() {
        print(strings.joinToString(separator = " ", postfix = if (suppressNewline) "" else "\n"))
    }
}

fun main(args: Array<String>) = Echo().main(args)
