package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context
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
     * @param context The context for the command to format help for, which includes the
     *   [localization][Context.localization] and [terminal][Context.terminal] to use
     * @param error The error encountered, if there was one
     * @param prolog Text to display before any parameter information
     * @param epilog Text to display after any parameter information
     * @param parameters Information about the command's parameters
     * @param programName The name of the currently executing program
     */
    fun formatHelp(
        context: Context,
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String = "",
    ): String

    sealed class ParameterHelp {
        /**
         * @param names The names that can be used to invoke this option
         * @param secondaryNames Secondary names that can be used to e.g. disable the option
         * @param metavar The metavar to display for the option if it takes values
         * @param help The option's description
         * @param nvalues The number of values that this option takes
         * @param tags Any extra tags to display with the help message for this option
         * @param groupName The name of the group this option belongs to, if there is one and its name should be shown in the help message
         */
        data class Option(
            val names: Set<String>,
            val secondaryNames: Set<String>,
            val metavar: String?,
            val help: String,
            val nvalues: IntRange,
            val tags: Map<String, String>,
            val acceptsNumberValueWithoutName: Boolean,
            val acceptsUnattachedValue: Boolean,
            val groupName: String?,
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
            val tags: Map<String, String>,
        ) : ParameterHelp()

        /**
         * @param name The name for this command
         * @param help The command's description
         */
        data class Subcommand(
            val name: String,
            val help: String,
            val tags: Map<String, String>,
        ) : ParameterHelp()

        /**
         * Help for an option group. If the group doesn't have a name or help, you don't need to
         * create an instance of this class for it.
         *
         * @property name The group name
         * @property help The help text for this group
         */
        data class Group(
            val name: String,
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
