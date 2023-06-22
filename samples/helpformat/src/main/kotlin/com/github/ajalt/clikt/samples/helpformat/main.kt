package com.github.ajalt.clikt.samples.helpformat

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Panel


class PanelHelpFormatter(context: Context) : MordantHelpFormatter(context) {
    override fun styleSectionTitle(title: String): String = theme.style("muted")(title)
    override fun normalizeParameter(name: String): String = name.uppercase()

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

class Echo(t: Terminal) : CliktCommand(help = "Echo the STRING(s) to standard output") {
    init {
        context {
            terminal = t
            helpFormatter = { PanelHelpFormatter(it) }
            localization = object : Localization {
                override fun optionsMetavar(): String = "OPTIONS"
            }
        }
    }

    val suppressNewline by option("-n", help = "do not output the trailing newline").flag()
    val strings by argument(help = "the strings to echo").multiple()

    override fun run() {
        val message = if (strings.isEmpty()) String(System.`in`.readBytes())
        else strings.joinToString(" ", postfix = if (suppressNewline) "" else "\n")
        echo(message, trailingNewline = false)
    }
}

fun main(args: Array<String>) {
    val theme = Theme {
        styles["info"] = TextColors.green
        styles["warning"] = TextColors.blue
        styles["danger"] = TextColors.magenta
    }
    Echo(Terminal(theme = theme)).main(args)
}
