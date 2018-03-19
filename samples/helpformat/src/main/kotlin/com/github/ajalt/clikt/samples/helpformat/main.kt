package com.github.ajalt.clikt.samples.helpformat

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class ArgparseHelpFormatter: PlaintextHelpFormatter(
        usageTitle = "usage:",
        optionsTitle = "optional arguments:",
        argumentsTitle = "positional arguments:") {
    override fun formatHelp(prolog: String,
                            epilog: String,
                            parameters: List<HelpFormatter.ParameterHelp>,
                            programName: String)= buildString {
        // argparse prints arguments before options
        addUsage(parameters, programName)
        addProlog(prolog)
        addArguments(parameters)
        addOptions(parameters)
        addCommands(parameters)
        addEpilog(epilog)
    }
}

class Echo : CliktCommand(help = """
    Echo the STRING(s) to standard output

    This command works like the echo example, but uses a custom help formatter that outputs help in a format
    similar to Python's argparse module.
    """) {
    init {
        context { helpFormatter = ArgparseHelpFormatter() }
    }
    private val suppressNewline by option("-n", help = "do not output the trailing newline").flag()
    private val strings by argument(help = "do not output the trailing newline").multiple()

    override fun run() {
        print(strings.joinToString(separator = " ", postfix = if (suppressNewline) "" else "\n"))
    }
}

fun main(args: Array<String>) = Echo().main(args)
