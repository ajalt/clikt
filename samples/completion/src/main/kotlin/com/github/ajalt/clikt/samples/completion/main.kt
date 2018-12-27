package com.github.ajalt.clikt.samples.completion

import com.github.ajalt.clikt.core.CliktCommand

class CompletionDemo : CliktCommand(
    name = "completion-demo"
) {
    // TODO: Add generate completion option

    override fun run() {
        echo("This command only demonstrates the generation of programmable auto completion commands")
    }
}

fun main(args: Array<String>) = CompletionDemo().main(args)
