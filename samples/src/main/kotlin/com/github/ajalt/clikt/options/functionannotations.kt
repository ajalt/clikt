package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.Option
import com.github.ajalt.clikt.parser.PrintMessage

/**
 * A function annotation to customize the behavior of a command executed by Clickt.
 *
 * You do not need to annotate a function to use it with Click; if this annotation isn't present,
 * all default values will be used.
 *
 * @property name The name of the command when used as a subcommand. If blank, the
 *     function name will be used.
 * @property help The long help text to display when help for this command is displayed.
 * @property epilog Text like the [help] string, but usually displayed after everything else.
 * @property shortHelp Short help text to display in the parent's help if this is used as a
 *     subcommand.
 * @property addHelpOption If true, a help option will be added to the command.
 * @property helpOptionNames If [addHelpOption] is true, the names to use for the added command.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ClicktCommand(
        val name: String = "", val help: String = "", val epilog: String = "",
        val shortHelp: String = "", val addHelpOption: Boolean = true,
        val helpOptionNames: Array<String> = arrayOf("-h", "--help"))


/**
 * A function annotation to add a --version option which immediately ends the program and prints out the
 * program version.
 *
 * @property version The version number to show.
 * @property names The option names to use.
 * @property progName The program name to use. Defaults to the command name.
 * @property message The message to print. The default is `"$progName, version $version"`
 */
@Target(AnnotationTarget.FUNCTION)
annotation class AddVersionOption(val version: String,
                                  val names: Array<String> = arrayOf("--version"),
                                  val progName: String = "",
                                  val message: String = "")

class VersionOption(names: List<String>,
                    private val progName: String,
                    private val version: String,
                    private val message: String) : Option(
        names, FlagOptionParser(), false, false, null,
        "Show the version and exit.", eager = true, exposeValue = false) {
    override fun processValues(context: Context, values: List<*>): Any? {
        val message: String = if (message.isNotBlank()) message else {
            val name = if (progName.isNotBlank()) progName else context.command.name
            "$name, version $version"
        }
        if (values.lastOrNull() == true) {
            throw PrintMessage(message)
        }
        return null
    }
}
