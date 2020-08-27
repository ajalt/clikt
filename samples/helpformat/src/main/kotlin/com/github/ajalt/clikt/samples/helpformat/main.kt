package com.github.ajalt.clikt.samples.helpformat

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class ArgparseLocalization : Localization {
    override fun usageTitle(): String = "usage:"
    override fun optionsTitle(): String = "optional arguments:"
    override fun argumentsTitle(): String = "positional arguments:"
}

class ArgparseHelpFormatter : CliktHelpFormatter(ArgparseLocalization()) {
    override fun formatHelp(
            prolog: String,
            epilog: String,
            parameters: List<HelpFormatter.ParameterHelp>,
            programName: String
    ) = buildString {
        // argparse prints arguments before options
        addUsage(parameters, programName)
        addProlog(prolog)
        addArguments(parameters)
        addOptions(parameters)
        addCommands(parameters)
        addEpilog(epilog)
    }
}

class Echo : CliktCommand(help = "Echo the STRING(s) to standard output") {
    init {
        context { helpFormatter = ArgparseHelpFormatter() }
    }

    val suppressNewline by option("-n", help = "do not output the trailing newline").flag()
    val strings by argument(help = "the strings to echo").multiple()

    override fun run() {
        val message = if (strings.isEmpty()) String(System.`in`.readBytes())
        else strings.joinToString(" ", postfix = if (suppressNewline) "" else "\n")
        echo(message, trailingNewline = false)
    }
}

fun main(args: Array<String>) = Echo().main(args)
