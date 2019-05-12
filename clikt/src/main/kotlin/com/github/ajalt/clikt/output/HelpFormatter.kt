package com.github.ajalt.clikt.output

/**
 * Creates help and usage strings for a command.
 *
 * You can set the formatter for a command when configuring the context.
 */
interface HelpFormatter {
    /**
     * Create the one-line usage information for a command.
     *
     * This is usually displayed when incorrect input is encountered, and as the first line of the full help.
     */
    fun formatUsage(parameters: List<ParameterHelp>, programName: String = ""): String

    /**
     * Create the full help string.
     *
     * @param prolog Text to display before any parameter information
     * @param epilog Text to display after any parameter information
     * @param parameters Information about the command's parameters
     * @param programName The name of the currently executing program
     */
    fun formatHelp(prolog: String, epilog: String, parameters: List<ParameterHelp>, programName: String = ""): String

    sealed class ParameterHelp {
        /**
         * @param names The names that can be used to invoke this option
         * @param secondaryNames Secondary names that can be used to e.g. disable the option
         * @param metavar The metavar to display for the option if it takes values
         * @param help The option's description
         * @param nvalues The number of values that this option takes
         */
        data class Option(
                val names: Set<String>,
                val secondaryNames: Set<String>,
                val metavar: String?,
                val help: String,
                val nvalues: Int,
                val tags: Map<String, String>
        ) : ParameterHelp()

        /**
         * @param name The name / metavar for this argument
         * @param help The arguments's description
         * @param required True if this argument must be specified
         * @param repeatable True if this argument takes an unlimited number of values
         */
        data class Argument(
                val name: String,
                val help: String,
                val required: Boolean,
                val repeatable: Boolean,
                val tags: Map<String, String>
        ) : ParameterHelp()

        /**
         * @param name The name for this command
         * @param help The command's description
         */
        data class Subcommand(
                val name: String,
                val help: String,
                val tags: Map<String, String>
        ) : ParameterHelp()
    }

    /** Standard tag names for parameter help */
    object Tags {
        /** A value that can be displayed to the user as the default for this option, or null if there is no default. */
        const val DEFAULT = "default"
        /** If true, this option is required. Only used for help output. */
        const val REQUIRED = "required"
    }
}

