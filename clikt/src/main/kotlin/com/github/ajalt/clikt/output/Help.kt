package com.github.ajalt.clikt.output

import kotlin.LazyThreadSafetyMode.NONE

interface HelpFormatter {
    fun formatUsage(parameters: List<ParameterHelp>, programName: String = ""): String
    fun formatHelp(prolog: String, epilog: String, parameters: List<ParameterHelp>,
                   programName: String = ""): String

    sealed class ParameterHelp {
        data class Option(val names: Set<String>,
                          val secondaryNames: Set<String>,
                          val metavar: String?,
                          val help: String,
                          val repeatable: Boolean) : ParameterHelp()

        data class Argument(val name: String,
                            val help: String,
                            val required: Boolean,
                            val repeatable: Boolean) : ParameterHelp()

        data class Subcommand(val name: String,
                              val help: String) : ParameterHelp()
    }
}

open class PlaintextHelpFormatter(protected val indent: String = "  ",
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
            if (parameters.any { it is HelpFormatter.ParameterHelp.Option }) {
                append(optionsMetavar)
            }

            parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().forEach {
                append(" ")
                if (!it.required) append("[")
                append(it.name)
                if (!it.required) append("]")
                if (it.repeatable) append("...")
            }

            if (parameters.any { it is HelpFormatter.ParameterHelp.Subcommand }) {
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
            usage.wrapText(this, width, "$prog ", usageIndent)
        }
    }


    override fun formatHelp(prolog: String,
                            epilog: String,
                            parameters: List<HelpFormatter.ParameterHelp>,
                            programName: String) = buildString {
        formatUsage(this, parameters, programName)
        if (prolog.isNotEmpty()) {
            section("")
            prolog.wrapText(this, width, initialIndent = "  ", subsequentIndent = "  ",
                    preserveParagraph = true)
        }

        val options = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Option>().map {
            val names = mutableListOf(joinOptionNames(it.names))
            if (it.secondaryNames.isNotEmpty()) names += joinOptionNames(it.secondaryNames)
            names.joinToString(" / ", postfix = optionMetavar(it)) to it.help
        }
        if (options.isNotEmpty()) {
            append("\n")
            section(optionsTitle)
            appendDefinitionList(options)
        }

        val arguments = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().map {
            it.name to it.help
        }
        if (arguments.isNotEmpty() && arguments.any { it.second.isNotEmpty() }) {
            append("\n")
            section(argumentsTitle)
            appendDefinitionList(arguments)
        }

        val commands = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Subcommand>().map {
            it.name to it.help
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

    protected fun joinOptionNames(names: Set<String>): String {
        return names.sortedBy { it.startsWith("--") }.joinToString(", ")
    }

    protected fun optionMetavar(option: HelpFormatter.ParameterHelp.Option): String {
        if (option.metavar == null) return ""
        val metavar = " " + option.metavar
        if (option.repeatable) return "$metavar..."
        return metavar
    }

    private fun StringBuilder.appendDefinitionList(rows: List<Pair<String, String>>) {
        if (rows.isEmpty()) return
        val firstWidth = measureFirstColumn(rows)
        val secondWidth = width - firstWidth - colSpacing
        val subsequentIndent by lazy(NONE) { " ".repeat(indent.length + firstWidth + colSpacing) }
        for ((i, row) in rows.withIndex()) {
            val (first, second) = row
            if (i > 0) append("\n")
            append(indent).append(first)
            if (first.length > maxColWidth) {
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

