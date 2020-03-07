package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.completion.CompletionGenerator
import com.github.ajalt.clikt.completion.ExperimentalCompletionCandidates
import com.github.ajalt.clikt.mpp.exitProcessMpp
import com.github.ajalt.clikt.mpp.mppClassSimpleName
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parsers.Parser

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
 * @param helpTags Extra information about this option to pass to the help formatter.
 * @param autoCompleteEnvvar The envvar to use to enable shell autocomplete script generation. Set
 *   to null to disable generation.
 * @param allowMultipleSubcommands If true, allow multiple of this command's subcommands to be
 *   called sequentially. This will disable `allowInterspersedArgs` on the context of this command an
 *   its descendants. This functionality is experimental, and may change in a future release.
 */
@Suppress("PropertyName")
@ParameterHolderDsl
abstract class CliktCommand(
        help: String = "",
        epilog: String = "",
        name: String? = null,
        val invokeWithoutSubcommand: Boolean = false,
        val printHelpOnEmptyArgs: Boolean = false,
        val helpTags: Map<String, String> = emptyMap(),
        private val autoCompleteEnvvar: String? = "",
        internal val allowMultipleSubcommands: Boolean = false
) : ParameterHolder {
    val commandName = name ?: mppClassSimpleName()
    val commandHelp = help
    val commandHelpEpilog = epilog
    internal var _subcommands: List<CliktCommand> = emptyList()
    internal val _options: MutableList<Option> = mutableListOf()
    internal val _arguments: MutableList<Argument> = mutableListOf()
    internal val _groups: MutableList<ParameterGroup> = mutableListOf()
    internal var _contextConfig: Context.Builder.() -> Unit = {}
    private var _context: Context? = null
    private val _messages = mutableListOf<String>()

    private fun registeredOptionNames() = _options.flatMapTo(mutableSetOf()) { it.names }

    private fun createContext(parent: Context? = null, ancestors: List<CliktCommand> = emptyList()) {
        _context = Context.build(this, parent, _contextConfig)

        if (allowMultipleSubcommands) {
            require(currentContext.ancestors().drop(1).none { it.command.allowMultipleSubcommands }) {
                "Commands with allowMultipleSubcommands=true cannot be nested in " +
                        "commands that also have allowMultipleSubcommands=true"
            }
        }

        if (currentContext.helpOptionNames.isNotEmpty()) {
            val names = currentContext.helpOptionNames - registeredOptionNames()
            if (names.isNotEmpty()) _options += helpOption(names, currentContext.helpOptionMessage)
        }

        for (command in _subcommands) {
            val a = (ancestors + parent?.command).filterNotNull()
            check(command !in a) { "Command ${command.commandName} already registered" }
            command.createContext(currentContext, a)
        }
    }

    private fun allHelpParams(): List<ParameterHelp> {
        return _options.mapNotNull { it.parameterHelp } +
                _arguments.mapNotNull { it.parameterHelp } +
                _groups.mapNotNull { it.parameterHelp } +
                _subcommands.map { ParameterHelp.Subcommand(it.commandName, it.shortHelp(), it.helpTags) }
    }

    private fun getCommandNameWithParents(): String {
        if (_context == null) createContext()
        return currentContext.commandNameWithParents().joinToString(" ")
    }

    private fun generateCompletion() {
        if (autoCompleteEnvvar == null) return
        val envvar = when {
            autoCompleteEnvvar.isBlank() -> "_${commandName.replace("-", "_").toUpperCase()}_COMPLETE"
            else -> autoCompleteEnvvar
        }
        val envval = readEnvvar(envvar) ?: return
        val completion = CompletionGenerator.generateCompletion(command = this, zsh = "zsh" in envval)
        throw PrintCompletionMessage(completion, forceUnixLineEndings = true)
    }

    @Deprecated("Renamed to currentContext", ReplaceWith("currentContext"))
    val context: Context
        get() = currentContext

    /**
     * This command's context.
     *
     * @throws NullPointerException if accessed before [parse] or [main] are called.
     */
    val currentContext: Context
        get() {
            checkNotNull(_context) { "Context accessed before parse has been called." }
            return _context!!
        }

    /** All messages issued during parsing. */
    val messages: List<String> get() = _messages

    /** Add a message to be printed after parsing */
    fun issueMessage(message: String) {
        _messages += message
    }

    /** The help displayed in the commands list when this command is used as a subcommand. */
    protected fun shortHelp(): String = Regex("""\s*(?:```)?\s*(.+)""").find(commandHelp)?.groups?.get(1)?.value ?: ""

    /** The names of all direct children of this command */
    fun registeredSubcommandNames(): List<String> = _subcommands.map { it.commandName }

    /**
     * Get a read-only list of commands registered as [subcommands] of this command.
     */
    fun registeredSubcommands(): List<CliktCommand> = _subcommands.toList()

    /**
     * Get a read-only list of options registered in this command (e.g. via [registerOption] or an [option] delegate)
     */
    fun registeredOptions(): List<Option> = _options.toList()

    /**
     * Get a read-only list of arguments registered in this command (e.g. via [registerArgument] or an [argument] delegate)
     */
    fun registeredArguments(): List<Argument> = _arguments.toList()

    /**
     * Get a read-only list of groups registered in this command (e.g. via [registerOptionGroup] or an [OptionGroup] delegate)
     */
    fun registeredParameterGroups(): List<ParameterGroup> = _groups.toList()

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

    override fun registerOption(option: GroupableOption) = registerOption(option as Option)

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

    /**
     * Register a group with this command.
     *
     * This is called automatically for built in groups, but you need to call this if you want to
     * add a custom group.
     */
    fun registerOptionGroup(group: ParameterGroup) {
        require(group !in _groups) { "Cannot register the same group twice" }
        require(group.groupName == null || _groups.none { it.groupName == group.groupName }) { "Cannot register the same group name twice" }
        _groups += group
    }

    /** Return the usage string for this command. */
    open fun getFormattedUsage(): String {
        val programName = getCommandNameWithParents()
        return currentContext.helpFormatter.formatUsage(allHelpParams(), programName = programName)
    }

    /** Return the full help string for this command. */
    open fun getFormattedHelp(): String {
        val programName = getCommandNameWithParents()
        return currentContext.helpFormatter.formatHelp(commandHelp, commandHelpEpilog,
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
    protected fun echo(
            message: Any?,
            trailingNewline: Boolean = true,
            err: Boolean = false,
            lineSeparator: String = currentContext.console.lineSeparator
    ) {
        TermUi.echo(message, trailingNewline, err, currentContext.console, lineSeparator)
    }

    /**
     * Parse the command line and throw an exception if parsing fails.
     *
     * You should use [main] instead unless you want to handle output yourself.
     */
    fun parse(argv: List<String>, parentContext: Context? = null) {
        createContext(parentContext)
        generateCompletion()
        Parser.parse(argv, this.currentContext)
    }

    fun parse(argv: Array<String>, parentContext: Context? = null) {
        parse(argv.asList(), parentContext)
    }

    /**
     * Parse the command line and print helpful output if any errors occur.
     *
     * This function calls [parse] and catches and [CliktError]s that are thrown. Other errors are allowed to
     * pass through.
     */
    fun main(argv: List<String>) {
        try {
            parse(argv)
        } catch (e: PrintHelpMessage) {
            echo(e.command.getFormattedHelp())
            exitProcessMpp(0)
        } catch (e: PrintCompletionMessage) {
            val s = if (e.forceUnixLineEndings) "\n" else currentContext.console.lineSeparator
            echo(e.message, lineSeparator = s)
            exitProcessMpp(0)
        } catch (e: PrintMessage) {
            echo(e.message)
            exitProcessMpp(0)
        } catch (e: UsageError) {
            echo(e.helpMessage(), err = true)
            exitProcessMpp(e.statusCode)
        } catch (e: CliktError) {
            echo(e.message, err = true)
            exitProcessMpp(1)
        } catch (e: Abort) {
            echo("Aborted!", err = true)
            exitProcessMpp(if (e.error) 1 else 0)
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

    override fun toString() = buildString {
        append("<CliktCommand name=$commandName ")

        if (_options.isNotEmpty()) {
            append("options=[")
            for ((i, option) in _options.withIndex()) {
                if (i > 0) append(" ")
                append(option.longestName())
                if (_context != null && option is OptionDelegate<*>) {
                    try {
                        val value = option.value
                        append("=").append(value)
                    } catch (_: IllegalStateException) {
                    }
                }
            }
            append("]")
        }


        if (_arguments.isNotEmpty()) {
            append(" arguments=[")
            for ((i, argument) in _arguments.withIndex()) {
                if (i > 0) append(" ")
                append(argument.name)
                if (_context != null && argument is ProcessedArgument<*, *>) {
                    try {
                        val value = argument.value
                        append("=").append(value)
                    } catch (_: IllegalStateException) {
                    }
                }
            }
            append("]")
        }

        if (_subcommands.isNotEmpty()) {
            append(" subcommands=[")
            _subcommands.joinTo(this, " ")
            append("]")
        }

        append(">")
    }
}

/** Add the given commands as a subcommand of this command. */
fun <T : CliktCommand> T.subcommands(commands: Iterable<CliktCommand>): T = apply {
    _subcommands = _subcommands + commands
}

/** Add the given commands as a subcommand of this command. */
fun <T : CliktCommand> T.subcommands(vararg commands: CliktCommand): T = apply {
    _subcommands = _subcommands + commands
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
