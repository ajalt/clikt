package com.github.ajalt.clikt.options

/**
 * A function annotation to customize the behavior of a command executed by Clickt.
 *
 * You do not need to annotate a function to use it with Click; if this annotation isn't present,
 * all default values will be used.
 *
 * @property name The name of the command to display when used in a subcommad. If blank, the
 *     function name will be used.
 * @property help The long help text to display when help for this command is displayed.
 * @property epilog Text like the [help] string, but usually displayed after everything else.
 * @property shortHelp Short help text to display in the parent's help if this is used as a
 *     subcommand.
 * @property addHelpOption If true, a help option will be added to the command.
 * @property helpOptionNames If [addHelpOption] is true, the names to use for the added command.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ClicktCommand(
        val name: String = "", val help: String = "", val epilog: String = "",
        val shortHelp: String = "", val addHelpOption: Boolean = true,
        val helpOptionNames: Array<String> = arrayOf("-h", "--help"))
