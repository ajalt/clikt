package com.github.ajalt.clikt.samples.json

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.sources.ExperimentalValueSourceApi

class Subcommand : CliktCommand() {
    private val number by option(help = "an integer").float()

    override fun run() {
        echo("subcommand --number=$number")
    }
}

class Cli : CliktCommand(help = "An example using json files for configuration values") {
    init {
        context {
            valueSources(
                    JsonValueSource.from(System.getProperty("user.dir") + "config.json"),
                    JsonValueSource.from(System.getProperty("user.dir") + "/samples/json/config.json")
            )
        }
    }

    private val option by option("-o", "--option", help = "this option takes multiple values").multiple()
    private val flag by option("-f", "--flag", help = "this option is a flag").flag()

    override fun run() {
        echo("--option=$option")
        echo("--flag=$flag")
    }
}

fun main(args: Array<String>) = Cli().subcommands(Subcommand()).main(args)
