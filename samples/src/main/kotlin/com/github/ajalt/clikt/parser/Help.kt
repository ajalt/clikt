package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp.Companion.SECTION_ARGUMENTS
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp.Companion.SECTION_OPTIONS
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp.Companion.SECTION_SUBCOMMANDS

interface HelpFormatter {
    fun formatUsage(parameters: List<ParameterHelp>): String
    fun formatHelp(parameters: List<ParameterHelp>): String

    data class ParameterHelp(val names: List<String>,
                             val metavars: List<String>,
                             val help: String,
                             val section: Int,
                             val required: Boolean,
                             val repeatable: Boolean) {
        companion object {
            const val SECTION_OPTIONS = 1
            const val SECTION_ARGUMENTS = 2
            const val SECTION_SUBCOMMANDS = 3
        }
    }
}

open class PlaintextHelpFormatter(val prolog: String = "",
                                  val epilog: String = "",
                                  val indent: String = "  ",
                                  width: Int? = null,
                                  maxWidth: Int = 78,
                                  val usageTitle: String = "Usage: ",
                                  val optionsTitle: String = "Options:",
                                  val argumentsTitle: String = "Arguments:",
                                  val commandsTitle: String = "Commands:",
                                  val maxColWidth: Int = 30,
                                  val colSpacing: Int = 2) : HelpFormatter {
    private val width: Int = when (width) {
        null -> minOf(maxWidth, System.getenv("COLUMNS")?.toInt() ?: maxWidth)
        else -> width
    }

    override fun formatUsage(parameters: List<HelpFormatter.ParameterHelp>): String {
        return buildString { formatUsage(this, parameters) }
    }

    private fun formatUsage(sb: StringBuilder, parameters: List<HelpFormatter.ParameterHelp>): Unit = with(sb) {
//        append(usageTitle)
//        if (usageTitle.length >= width - 20) append("\n")
//        TODO() // TODO

    }

    override fun formatHelp(parameters: List<HelpFormatter.ParameterHelp>) = buildString {
        // TODO: required, repeatable
        formatUsage(this, parameters)
        section("")
        if (prolog.isNotEmpty()) {
            append(prolog.wrapText(width, indent, indent, true))
            if (!prolog.endsWith("\n")) append("\n")
        }

        val options = parameters.filter { it.section == SECTION_OPTIONS }.map {
            it.names.sortedBy { it.startsWith("--") }
                    .joinToString(", ", postfix = when {
                        it.metavars.isEmpty() -> ""
                        else -> it.metavars.joinToString(" ", prefix = " ")
                    }) to it.help
        }
        if (options.isNotEmpty()) {
            section(optionsTitle)
            appendDefinitionList(options)
        }

        val arguments = parameters.filter { it.section == SECTION_ARGUMENTS }.map {
            it.names[0] to it.help
        }
        if (arguments.isNotEmpty() && arguments.any { it.second.isNotEmpty() }) {
            section(argumentsTitle)
            appendDefinitionList(arguments)
        }

        val commands = parameters.filter { it.section == SECTION_SUBCOMMANDS }.map {
            it.names[0] to it.help
        }
        if (commands.isNotEmpty()) {
            section(commandsTitle)
            appendDefinitionList(commands)
        }

        if (epilog.isNotEmpty()) {
            section("")
            append(epilog.wrapText(width, indent, indent, true))
        }
    }

    private fun StringBuilder.appendDefinitionList(rows: List<Pair<String, String>>) {
        if (rows.isEmpty()) return
        val firstWidth = measureFirstColumn(rows)
        val secondWidth = width - firstWidth - colSpacing
        val secondIndent = " ".repeat(firstWidth + colSpacing)
        for ((first, second) in rows) {
            append(indent).append(first)
            if (first.length + indent.length > maxColWidth) {
                append("\n").append(secondIndent)
            } else {
                val n = firstWidth - first.length + colSpacing
                append(" ".repeat(n))
            }

            val lines = second.wrapText(secondWidth).split("\n")
            for ((i, line) in lines.withIndex()) {
                if (i > 0) append(secondIndent)
                append(line).append("\n")
            }
        }
    }

    private fun measureFirstColumn(rows: List<Pair<String, String>>): Int =
            rows.maxBy({ it.first.length })?.first?.length?.coerceAtMost(maxColWidth) ?: maxColWidth

    private fun StringBuilder.section(title: String) {
        append("\n").append(title).append("\n")
    }
}

