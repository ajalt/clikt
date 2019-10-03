package com.github.ajalt.clikt.sample

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.option

class HelloClikt : CliktCommand() {
    val verbose by option("-v", "--verbose").counted()

    override fun run() {
        val punct = if (verbose <= 0) "." else (1..verbose).joinToString("") { "!" }
        echo("Hello Native$punct")
    }
}

fun main(args: Array<String>) = HelloClikt().main(args)
