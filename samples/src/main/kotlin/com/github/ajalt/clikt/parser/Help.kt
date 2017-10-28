package com.github.ajalt.clikt.parser

interface HelpFormatter {
    fun formatUsage(parameters: List<ParameterHelp>): String
    fun formatHelp(parameters: List<ParameterHelp>): String

    data class ParameterHelp(val names: List<String>,
                             val metavars: List<String>,
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

open class PlaintextHelpFormatter(val prolog: String = "", val indent: String = "    ") : HelpFormatter {
    override fun formatUsage(parameters: List<HelpFormatter.ParameterHelp>): String {
        return buildString { formatUsage(this, parameters) }
    }

    private fun formatUsage(sb: StringBuilder, parameters: List<HelpFormatter.ParameterHelp>) {
        TODO()
    }

    override fun formatHelp(parameters: List<HelpFormatter.ParameterHelp>): String {
        buildString {
            formatUsage(this, parameters)
            section("")
            append(prolog)
            if (!prolog.endsWith("\n")) append("\n")

            if (parameters.any { it.section == HelpFormatter.ParameterHelp.SECTION_OPTIONS }) {
                section("")
                TODO()
            }
        }
    }

    private fun StringBuilder.section(title: String) {
        append("\n").append(title).append("\n")
    }
}
