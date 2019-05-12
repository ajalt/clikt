package com.github.ajalt.clikt.samples.ansicolors

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.TermColors

private val tc = TermColors(TermColors.Level.TRUECOLOR)

class ColorHelpFormatter : PlaintextHelpFormatter(usageTitle = (tc.bold + tc.underline)("Usage:")) {
    override fun renderTag(tag: String, value: String) = tc.green(super.renderTag(tag, value))
    override fun renderOptionName(name: String) = tc.yellow(super.renderOptionName(name))
    override fun renderArgumentName(name: String) = tc.yellow(super.renderArgumentName(name))
    override fun renderSubcommandName(name: String) = tc.yellow(super.renderSubcommandName(name))
    override fun renderSectionTitle(title: String) = (tc.bold + tc.underline)(super.renderSectionTitle(title))
    override fun optionMetavar(option: HelpFormatter.ParameterHelp.Option) = tc.green(super.optionMetavar(option))
}

class Cli : NoRunCliktCommand(help = "An example of a custom help formatter that uses ansi colors") {
    init {
        context { helpFormatter = ColorHelpFormatter() }
    }

    val option by option("-o", "--option", help = "this option takes a value")
    val flag by option("-f", help = "this option is a flag").flag()
    val files by argument(help = "files to input").multiple()
}

class Sub : NoRunCliktCommand(help = "this is a subcommand")

fun main(args: Array<String>) = Cli().subcommands(Sub()).main(args)
