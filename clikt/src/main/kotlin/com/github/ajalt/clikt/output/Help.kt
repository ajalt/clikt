package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp.Companion.SECTION_ARGUMENTS
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp.Companion.SECTION_OPTIONS
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp.Companion.SECTION_SUBCOMMANDS
import kotlin.LazyThreadSafetyMode.NONE

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

open class PlaintextHelpFormatter(protected val prolog: String = "",
                                  protected val epilog: String = "",
                                  protected val indent: String = "  ",
                                  width: Int? = null,
                                  maxWidth: Int = 78,
                                  maxColWidth: Int? = null,
                                  protected val usageTitle: String = "Usage:",
                                  protected val optionsTitle: String = "Options:",
                                  protected val argumentsTitle: String = "Arguments:",
                                  protected val commandsTitle: String = "Commands:",
                                  protected val optionsMetavar: String = "[OPTIONS]",
                                  protected val commandMetavar: String = "COMMAND [ARGS]...",
                                  protected val colSpacing: Int = 2) : HelpFormatter {
    protected val width: Int = when (width) {
        null -> minOf(maxWidth, System.getenv("COLUMNS")?.toInt() ?: maxWidth)
        else -> width
    }

    protected val maxColWidth: Int = maxColWidth ?: (this.width / 2.5).toInt()

    override fun formatUsage(parameters: List<HelpFormatter.ParameterHelp>, programName: String): String {
        return buildString { formatUsage(this, parameters, programName) }
    }

    private fun formatUsage(sb: StringBuilder, parameters: List<HelpFormatter.ParameterHelp>,
                            programName: String): Unit = with(sb) {
        val prog = "$usageTitle $programName"
        val usage = buildString {
            if (parameters.any { it.section == SECTION_OPTIONS }) {
                append(optionsMetavar)
            }

            parameters.filterSection(SECTION_ARGUMENTS).forEach {
                append(" ")
                if (!it.required) append("[")
                append(it.names[0])
                if (!it.required) append("]")
                if (it.repeatable) append("...")
            }

            if (parameters.any { it.section == SECTION_SUBCOMMANDS }) {
                append(" ").append(commandMetavar)
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
        formatUsage(this, parameters, programName)
        if (prolog.isNotEmpty()) {
            section("")
            prolog.wrapText(this, width, preserveParagraph = true)
        }

        val options = parameters.filterSection(SECTION_OPTIONS).map {
            it.names.sortedBy { it.startsWith("--") }
                    .joinToString(", ", postfix = optionMetavar(it)) to it.help
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

    protected fun optionMetavar(option: HelpFormatter.ParameterHelp) : String {
        if (option.metavar == null) return ""
        val metavar = " " + option.metavar
        if (option.repeatable) return metavar + "..."
        return metavar
    }

    protected fun List<HelpFormatter.ParameterHelp>.filterSection(section: Int) =
            filter { it.section == section }

    private fun StringBuilder.appendDefinitionList(rows: List<Pair<String, String>>) {
        if (rows.isEmpty()) return
        val firstWidth = measureFirstColumn(rows)
        val secondWidth = width - firstWidth - colSpacing
        val subsequentIndent by lazy(NONE) { " ".repeat(indent.length + firstWidth + colSpacing) }
        for ((i, row) in rows.withIndex()) {
            val (first, second) = row
            if (i > 0) append("\n")
            append(indent).append(first)
            if (first.length + indent.length > maxColWidth) {
                append("\n").append(subsequentIndent)
            } else {
                appendRepeat(" ", firstWidth - first.length + colSpacing)
            }

            val t = second.wrapText(secondWidth, subsequentIndent = subsequentIndent)
            append(t)

        }
    }

    private fun measureFirstColumn(rows: List<Pair<String, String>>): Int =
            rows.maxBy({ it.first.length })?.first?.length?.coerceAtMost(maxColWidth) ?: maxColWidth

    private fun StringBuilder.section(title: String) {
        append("\n").append(title).append("\n")
    }
}

