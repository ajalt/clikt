package com.github.ajalt.clikt.samples.completion

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.generateCompletionOption
import com.github.ajalt.clikt.parameters.options.versionOption

class CompletionDemo : CliktCommand(
    name = "completion-demo"
) {
    init {
        versionOption("1.2.3")
        generateCompletionOption()
    }

    override fun run() {
        echo("This command only demonstrates the generation of programmable auto completion commands")
    }
}

fun main(args: Array<String>) = CompletionDemo().main(args)
