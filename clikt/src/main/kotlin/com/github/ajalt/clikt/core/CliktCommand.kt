package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.helpOption
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parsers.Parser
import java.util.*
import java.util.Collections.emptyList
import java.util.Collections.emptyMap
import kotlin.system.exitProcess

/**
 * The [CliktCommand] is the core of command line interfaces in Clikt.
 *
 * Command line interfaces created by creating a subclass of [CliktCommand] with properties defined with
 * [option] and [argument]. You can then parse `argv` by calling [main], which will take care of printing
 * errors and help to the user. If you want to handle output yourself, you can use [parse] instead.
 *
 * Once the command line has been parsed and all of the parameters are populated, [run] is called.
 *
 * @param help The help for this command. The first line is used in the usage string, and the entire string is
 *   used in the help output. Paragraphs are automatically re-wrapped to the terminal width.
 * @param epilog Text to display at the end of the full help output. It is automatically re-wrapped to the
 *   terminal width.
 * @param name The name of the program to use in the help output. If not given, it is inferred from the class
 *   name.
 * @param invokeWithoutSubcommand Used when this command has subcommands, and this command is called
 *   without a subcommand. If true, [run] will be called. By default, a [PrintHelpMessage] is thrown instead.
 * @param printHelpOnEmptyArgs If this command is called with no values on the command line, print a
 *   help message (by throwing [PrintHelpMessage]) if this is true, otherwise run normally.
 */
@Suppress("PropertyName")
abstract class CliktCommand(
        help: String = "",
        epilog: String = "",
        name: String? = null,
        val invokeWithoutSubcommand: Boolean = false,
        val printHelpOnEmptyArgs: Boolean = false) {
    val commandName = name ?: javaClass.simpleName.split("$").last().toLowerCase()
    val commandHelp = help
    val commandHelpEpilog = epilog
    internal var _subcommands: List<CliktCommand> = emptyList()
    internal val _options: MutableList<Option> = mutableListOf()
    internal val _arguments: MutableList<Argument> = mutableListOf()
    internal var _contextConfig: Context.Builder.() -> Unit = {}
    private var _context: Context? = null

    private fun registeredOptionNames() = _options.flatMapTo(HashSet()) { it.names }

    private fun createContext(parent: Context? = null) {
        _context = Context.build(this, parent, _contextConfig)

        if (context.helpOptionNames.isEmpty()) return
        val names = context.helpOptionNames - registeredOptionNames()
        if (names.isNotEmpty()) _options += helpOption(names, context.helpOptionMessage)

        for (command in _subcommands) {
            command.createContext(context)
        }
    }

    private fun allHelpParams() = _options.mapNotNull { it.parameterHelp } +
            _arguments.mapNotNull { it.parameterHelp } +
            _subcommands.map { ParameterHelp.Subcommand(it.commandName, it.shortHelp()) }

    private fun getCommandNameWithParents(): String {
        if (_context == null) createContext()
        return generateSequence(context) { it.parent }.toList()
                .asReversed()
                .joinToString(" ") { it.command.commandName }
    }

    /**
     * This command's context.
     *
     * @throws NullPointerException if accessed before [parse] or [main] are called.
     */
    val context: Context
        get() {
            checkNotNull(_context) { "Context accessed before parse has been called." }
            return _context!!
        }

    /** The help displayed in the commands list when this command is used as a subcommand. */
    protected fun shortHelp(): String = Regex("\\S.*\$", RegexOption.MULTILINE).find(commandHelp)?.value ?: ""


    /** The names of all direct children of this command */
    fun registeredSubcommandNames(): List<String> = _subcommands.map { it.commandName }

    /**
     * Register an option with this command.
     *
     * This is called automatically for the built in options, but you need to call this if you want to add a
     * custom option.
     */
    fun registerOption(option: Option) {
        val names = registeredOptionNames()
        for (name in option.names) {
            require(name !in names) { "Duplicate option name $name" }
        }
        _options += option
    }

    /**
     * Register an argument with this command.
     *
     * This is called automatically for the built in arguments, but you need to call this if you want to add a
     * custom argument.
     */
    fun registerArgument(argument: Argument) {
        require(argument.nvalues > 0 || _arguments.none { it.nvalues < 0 }) {
            "Cannot declare multiple arguments with variable numbers of values"
        }
        _arguments += argument
    }

    /** Return the usage string for this command. */
    open fun getFormattedUsage(): String {
        val programName = getCommandNameWithParents()
        return context.helpFormatter.formatUsage(allHelpParams(), programName = programName)
    }

    /** Return the full help string for this command. */
    open fun getFormattedHelp(): String {
        val programName = getCommandNameWithParents()
        return context.helpFormatter.formatHelp(commandHelp, commandHelpEpilog,
                allHelpParams(), programName = programName)
    }

    /**
     * A list of command aliases.
     *
     * If the user enters a command that matches the a key in this map, the command is replaced with the
     * corresponding value in in map. The aliases aren't recursive, so aliases won't be looked up again while
     * tokens from an existing alias are being parsed.
     */
    open fun aliases(): Map<String, List<String>> = emptyMap()

    /**
     * Print the [message] to the screen.
     *
     * This is similar to [print] or [println], but converts newlines to the system line separator.
     *
     * This is equivalent to calling [TermUi.echo] with the console from the current context.
     *
     * @param message The message to print.
     * @param trailingNewline If true, behave like [println], otherwise behave like [print]
     * @param err If true, print to stderr instead of stdout
     */
    protected fun echo(message: Any?, trailingNewline: Boolean = true, err: Boolean = false) {
        TermUi.echo(message, trailingNewline, err, context.console)
    }

    /**
     * Parse the command line and throw an exception if parsing fails.
     *
     * You should use [main] instead unless you want to handle output yourself.
     */
    fun parse(argv: List<String>, parentContext: Context? = null) {
        createContext(parentContext)
        Parser.parse(argv, this.context)
    }

    fun parse(argv: Array<String>, parentContext: Context? = null) {
        parse(argv.asList(), parentContext)
    }

    /**
     * Parse the command line and print helpful output if any errors occur.
     *
     * This function calls [parse] and catches and [CliktError]s that are thrown. Other error are allowed to
     * pass through.
     */
    fun main(argv: List<String>) {
        try {
            parse(argv)
        } catch (e: PrintHelpMessage) {
            echo(e.command.getFormattedHelp())
            exitProcess(0)
        } catch (e: PrintMessage) {
            echo(e.message)
            exitProcess(0)
        } catch (e: UsageError) {
            echo(e.helpMessage(), err = true)
            exitProcess(1)
        } catch (e: CliktError) {
            echo(e.message, err = true)
            exitProcess(1)
        } catch (e: Abort) {
            echo("Aborted!", err = true)
            exitProcess(if (e.error) 1 else 0)
        }
    }

    fun main(argv: Array<String>) = main(argv.asList())

    /**
     * Perform actions after parsing is complete and this command is invoked.
     *
     * This is called after command line parsing is complete. If this command is a subcommand, this will only
     * be called if the subcommand is invoked.
     *
     * If one of this command's subcommands is invoked, this is called before the subcommand's arguments are
     * parsed.
     */
    abstract fun run()
}

/** Add the given commands as a subcommand of this command. */
fun <T : CliktCommand> T.subcommands(commands: Iterable<CliktCommand>): T = apply {
    _subcommands += commands
}

/** Add the given commands as a subcommand of this command. */
fun <T : CliktCommand> T.subcommands(vararg commands: CliktCommand): T = apply {
    _subcommands += commands
}

/**
 * Configure this command's [Context].
 *
 * Context property values are normally inherited from the parent context, but you can override any of them
 * here.
 */
fun <T : CliktCommand> T.context(block: Context.Builder.() -> Unit): T = apply {
    _contextConfig = block
}
