package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.UsageError

/**
 * Creates help and usage strings for a command.
 *
 * You can set the formatter for a command when configuring the context.
 */
interface HelpFormatter {
    /**
     * Return a help string to show the user.
     *
     * @param error The error encountered, if there was one
     * @param prolog Text to display before any parameter information
     * @param epilog Text to display after any parameter information
     * @param parameters Information about the command's parameters
     * @param programName The name of the currently executing program
     */
    fun formatHelp(
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String = "",
    ): String

    sealed class ParameterHelp {
        data class Option(
            /** The names that can be used to invoke this option */
            val names: Set<String>,
            /** Secondary names that can be used to e.g. disable the option */
            val secondaryNames: Set<String>,
            /** The metavar to display for the option if it takes values */
            val metavar: String?,
            /** The option's description */
            val help: String,
            /** The number of values that this option takes */
            val nvalues: IntRange,
            /** Any extra tags to display with the help message for this option */
            val tags: Map<String, String>,
            /** True if this option can be called like `-1`, `-2` etc. */
            val acceptsNumberValueWithoutName: Boolean,
            /** True if this option can be called like `--option 1` in addition to `--option=1` */
            val acceptsUnattachedValue: Boolean,
            /** The name of the group this option belongs to, if there is one and its name should be shown in the help message */
            val groupName: String?,
        ) : ParameterHelp()

        data class Argument(
            /** The name / metavar for this argument */
            val name: String,
            /** The argument's description */
            val help: String,
            /** True if this argument must be specified */
            val required: Boolean,
            /** True if this argument takes an unlimited number of values */
            val repeatable: Boolean,
            /** Any extra tags to display with the help message for this argument */
            val tags: Map<String, String>,
        ) : ParameterHelp()

        data class Subcommand(
            /** The name for this command */
            val name: String,
            /** The command's description */
            val help: String,
            /** Any extra tags to display with the help message for this command */
            val tags: Map<String, String>,
        ) : ParameterHelp()

        /**
         * Help for an option group. If the group doesn't have a name or help, you don't need to
         * create an instance of this class for it.
         */
        data class Group(
            /** The group name */
            val name: String,
            /** The help text for this group */
            val help: String,
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

/** A formatter for styling parts of a help message */
interface ParameterFormatter {
    /** Format an option name */
    fun formatOption(name: String): String

    /** Format an argument name */
    fun formatArgument(name: String): String

    /** Format a subcommand name */
    fun formatSubcommand(name: String): String

    /** A ParameterFormatter that does no formatting */
    object Plain : ParameterFormatter {
        override fun formatOption(name: String) = name
        override fun formatArgument(name: String) = name
        override fun formatSubcommand(name: String) = name
    }
}
