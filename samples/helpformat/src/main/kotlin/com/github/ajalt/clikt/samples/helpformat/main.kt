package com.github.ajalt.clikt.samples.helpformat

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.widgets.Panel


class PanelHelpFormatter(context: Context) : MordantHelpFormatter(context) {
    override fun styleSectionTitle(title: String): String = theme.style("muted")(title)

    override fun renderParameters(
        parameters: List<HelpFormatter.ParameterHelp>,
    ): Widget = verticalLayout {
        width = ColumnWidth.Expand()
        for (section in collectParameterSections(parameters)) {
            cell(
                Panel(
                    section.content,
                    section.title,
                    expand = true,
                    titleAlign = TextAlign.LEFT,
                    borderStyle = theme.style("muted")
                )
            )
        }
    }
}

class Echo : CliktCommand(help = "Echo the STRING(s) to standard output") {
    init {
        context { helpFormatter = { PanelHelpFormatter(it) } }
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
