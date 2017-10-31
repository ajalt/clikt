package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp.Companion.SECTION_ARGUMENTS
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp.Companion.SECTION_OPTIONS
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp.Companion.SECTION_SUBCOMMANDS

interface HelpFormatter {
    fun formatUsage(parameters: List<ParameterHelp>, programName: String = ""): String
    fun formatHelp(parameters: List<ParameterHelp>, programName: String = ""): String

    data class ParameterHelp constructor(val names: List<String>,
                                         val metavar: String?,
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
                                  val optionsMetavar: String = "[OPTIONS]",
                                  val maxColWidth: Int = 30,
                                  val colSpacing: Int = 2) : HelpFormatter {
    private val width: Int = when (width) {
        null -> minOf(maxWidth, System.getenv("COLUMNS")?.toInt() ?: maxWidth)
        else -> width
    }

    override fun formatUsage(parameters: List<HelpFormatter.ParameterHelp>, programName: String): String {
        return buildString { formatUsage(this, parameters, programName) }
    }

    private fun formatUsage(sb: StringBuilder, parameters: List<HelpFormatter.ParameterHelp>,
                            programName: String): Unit = with(sb) {
        val prog = usageTitle + programName
        val usage = buildString {
            if (parameters.any { it.section == SECTION_OPTIONS }) {
                append(optionsMetavar)
            }

            parameters.filterSection(SECTION_ARGUMENTS).forEach {
                append(" ")
                if (!it.required) append("[")
                it.metavar?.let { append(it) }
                if (!it.required) append("]")
                if (it.repeatable) append("...")
            }

            if (parameters.any { it.section == SECTION_SUBCOMMANDS }) {
                append(" COMMAND [ARGS]...")
            }
        }

        if (usage.isEmpty()) {
            append(prog)
        } else if (prog.length >= width - 20) {
            append(prog).append("\n")
            val usageIndent = " ".repeat(minOf(width / 3, 11))
            usage.wrapText(this, width, usageIndent, usageIndent)
        } else {
            val usageIndent = " ".repeat(prog.length + 1)
            usage.wrapText(this, width, prog + " ", usageIndent)
        }
    }

    override fun formatHelp(parameters: List<HelpFormatter.ParameterHelp>,
                            programName: String) = buildString {
        // TODO: required, repeatable
        formatUsage(this, parameters, programName)
        if (prolog.isNotEmpty()) {
            section("")
            prolog.wrapText(this, width, preserveParagraph = true)
        }

        val options = parameters.filterSection(SECTION_OPTIONS).map {
            it.names.sortedBy { it.startsWith("--") }
                    .joinToString(", ", postfix = it.metavar?.let { " " + it } ?: "") to it.help
        }
        if (options.isNotEmpty()) {
            append("\n")
            section(optionsTitle)
            appendDefinitionList(options)
        }

        val arguments = parameters.filterSection(SECTION_ARGUMENTS).map {
            it.names[0] to it.help
        }
        if (arguments.isNotEmpty() && arguments.any { it.second.isNotEmpty() }) {
            append("\n")
            section(argumentsTitle)
            appendDefinitionList(arguments)
        }

        val commands = parameters.filterSection(SECTION_SUBCOMMANDS).map {
            it.names[0] to it.help
        }
        if (commands.isNotEmpty()) {
            append("\n")
            section(commandsTitle)
            appendDefinitionList(commands)
        }

        if (epilog.isNotEmpty()) {
            section("")
            epilog.wrapText(this, width, preserveParagraph = true)
        }
    }

    protected fun List<HelpFormatter.ParameterHelp>.filterSection(section: Int) =
            filter { it.section == section }

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
                appendRepeat(" ", firstWidth - first.length + colSpacing)
            }

            second.wrapText(this, secondWidth, subsequentIndent = secondIndent)
        }
    }

    private fun measureFirstColumn(rows: List<Pair<String, String>>): Int =
            rows.maxBy({ it.first.length })?.first?.length?.coerceAtMost(maxColWidth) ?: maxColWidth

    private fun StringBuilder.section(title: String) {
        append("\n").append(title).append("\n")
    }
}

