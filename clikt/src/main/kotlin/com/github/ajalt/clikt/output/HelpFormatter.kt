package com.github.ajalt.clikt.output

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

