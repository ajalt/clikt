package com.github.ajalt.clikt.output

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
        return buildString { this.addUsage(parameters, programName) }
    }


    override fun formatHelp(prolog: String,
                            epilog: String,
                            parameters: List<HelpFormatter.ParameterHelp>,
                            programName: String) = buildString {
        addUsage(parameters, programName)
        addProlog(prolog)
        addOptions(parameters)
        addArguments(parameters)
        addCommands(parameters)
        addEpilog(epilog)
    }

    protected open fun StringBuilder.addUsage(parameters: List<HelpFormatter.ParameterHelp>,
                                              programName: String) {
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

    protected open fun StringBuilder.addProlog(prolog: String) {
        if (prolog.isNotEmpty()) {
            section("")
            prolog.wrapText(this, width, initialIndent = "  ", subsequentIndent = "  ",
                    preserveParagraph = true)
        }
    }

    protected open fun StringBuilder.addOptions(parameters: List<HelpFormatter.ParameterHelp>) {
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
    }

    protected open fun StringBuilder.addArguments(parameters: List<HelpFormatter.ParameterHelp>) {
        val arguments = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().map {
            it.name to it.help
        }
        if (arguments.isNotEmpty() && arguments.any { it.second.isNotEmpty() }) {
            append("\n")
            section(argumentsTitle)
            appendDefinitionList(arguments)
        }
    }

    protected open fun StringBuilder.addCommands(parameters: List<HelpFormatter.ParameterHelp>) {
        val commands = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Subcommand>().map {
            it.name to it.help
        }
        if (commands.isNotEmpty()) {
            append("\n")
            section(commandsTitle)
            appendDefinitionList(commands)
        }
    }

    protected open fun StringBuilder.addEpilog(epilog: String) {
        if (epilog.isNotEmpty()) {
            section("")
            epilog.wrapText(this, width, preserveParagraph = true)
        }
    }

    protected open fun joinOptionNames(names: Set<String>): String {
        return names.sortedBy { it.startsWith("--") }.joinToString(", ")
    }

    protected open fun optionMetavar(option: HelpFormatter.ParameterHelp.Option): String {
        if (option.metavar == null) return ""
        val metavar = " " + option.metavar
        if (option.nargs > 1) return "$metavar..."
        return metavar
    }

    protected fun StringBuilder.appendDefinitionList(rows: List<Pair<String, String>>) {
        if (rows.isEmpty()) return
        val firstWidth = measureFirstColumn(rows)
        val secondWidth = width - firstWidth - colSpacing
        val subsequentIndent by lazy(LazyThreadSafetyMode.NONE) { " ".repeat(indent.length + firstWidth + colSpacing) }
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
