package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.completion.CompletionGenerator
import com.github.ajalt.clikt.mpp.exitProcessMpp
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parsers.Parser
import com.github.ajalt.mordant.terminal.Terminal

// TODO: docs
/**
 * A base class for commands that want to define a custom type for their `run` function.
 */
@Suppress("PropertyName")
@ParameterHolderDsl
abstract class BaseCliktCommand<RunnerT : Function<*>>(
    /**
     * The help for this command. The first line is used in the usage string, and the entire string
     * is used in the help output. Paragraphs are automatically re-wrapped to the terminal width.
     */
    private val help: String = "",
    /**
     * Text to display at the end of the full help output. It is automatically re-wrapped to the
     * terminal width.
     */
    private val epilog: String = "",
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
    /**
     * Used when this command has subcommands, and this command is called without a subcommand. If
     * true, [run] will be called. By default, a [PrintHelpMessage] is thrown instead.
     */
    val invokeWithoutSubcommand: Boolean = false,
    /**
     * If this command is called with no values on the command line, print a help message (by
     * throwing [PrintHelpMessage]) if this is true, otherwise run normally.
     */
    val printHelpOnEmptyArgs: Boolean = false,
    /**
     * Extra information about this command to pass to the help formatter.
     */
    val helpTags: Map<String, String> = emptyMap(),
    /**
     * The envvar to use to enable shell autocomplete script generation. Set to null to disable
     * generation.
     */
    private val autoCompleteEnvvar: String? = "",
    /**
     * If true, allow multiple of this command's subcommands to be called sequentially. This will
     * disable `allowInterspersedArgs` on the context of this command and its descendants.
     */
    internal val allowMultipleSubcommands: Boolean = false,
    /**
     * If true, any options on the command line whose names aren't valid will be parsed as an
     * argument rather than reporting an error. You'll need to define an `argument().multiple()` to
     * collect these options, or an error will still be reported. Unknown short option flags grouped
     * with other flags on the command line will always be reported as errors.
     */
    internal val treatUnknownOptionsAsArgs: Boolean = false,
    /**
     * If true, don't display this command in help output when used as a subcommand.
     */
    private val hidden: Boolean = false,
) : ParameterHolder {
    /**
     * The function to run when this command is invoked.
     *
     * For [CliktCommand], this just calls `run`.
     */
    abstract val runner: RunnerT

    /**
     * The name of this command, used in help output.
     *
     * You can set this by passing `name` to the [CliktCommand] constructor.
     */
    val commandName: String = name ?: inferCommandName()

    /**
     * The help text for this command.
     *
     * You can set this by passing `help` to the [CliktCommand] constructor, or by overriding this
     * method if you need to build the string lazily or access the terminal or context.
     *
     * ### Example:
     *
     * ```
     * class Command: CliktCommand() {
     *     override fun commandHelp(context: Context): String {
     *         return context.theme.info("This is a command")
     *     }
     * }
     * ```
     */
    open fun commandHelp(context: Context): String {
        return help
    }

    /**
     * Help text to display at the end of the help output, after any parameters.
     *
     * You can set this by passing `epilog` to the [CliktCommand] constructor, or by overriding this
     * method.
     */
    open fun commandHelpEpilog(context: Context): String {
        return epilog
    }

    internal var _subcommands: List<BaseCliktCommand<RunnerT>> = emptyList()
    internal val _options: MutableList<Option> = mutableListOf()
    internal val _arguments: MutableList<Argument> = mutableListOf()
    internal val _groups: MutableList<ParameterGroup> = mutableListOf()
    internal var _contextConfig: Context.Builder.() -> Unit = {}
    private var _context: Context? = null
    private val _messages = mutableListOf<String>()

    private fun registeredOptionNames() = _options.flatMapTo(mutableSetOf()) { it.names }

    fun resetContext(parent: Context?): Context {
        //TODO ("doc that each run get a fresh context now")
        _context?.close()
        // TODO either pass argv or remove it from ctx
        _context = Context.build(this, parent, emptyList(), _contextConfig)

        if (allowMultipleSubcommands) {
            require(currentContext.ancestors().none { it.command.allowMultipleSubcommands }) {
                "Commands with allowMultipleSubcommands=true cannot be nested in " +
                        "commands that also have allowMultipleSubcommands=true"
            }
        }

        if (currentContext.helpOptionNames.isNotEmpty()) {
            val names = currentContext.helpOptionNames - registeredOptionNames()
            if (names.isNotEmpty()) {
                eagerOption(names, currentContext.localization.helpOptionMessage()) {
                    throw PrintHelpMessage(context)
                }
            }
        }
        // TODO  check(command !in a) { "Command ${command.commandName} already registered" }
        return _context!!
    }

    private fun createContext(
        argv: List<String>,
        parent: Context?,
        ancestors: List<BaseCliktCommand<RunnerT>>,
    ): Context {
        _context = Context.build(this, parent, argv, _contextConfig)

        if (allowMultipleSubcommands) {
            require(currentContext.ancestors().none { it.command.allowMultipleSubcommands }) {
                "Commands with allowMultipleSubcommands=true cannot be nested in " +
                        "commands that also have allowMultipleSubcommands=true"
            }
        }

        if (currentContext.helpOptionNames.isNotEmpty()) {
            val names = currentContext.helpOptionNames - registeredOptionNames()
            if (names.isNotEmpty()) {
                eagerOption(names, currentContext.localization.helpOptionMessage()) {
                    throw PrintHelpMessage(context)
                }
            }
        }

        for (command in _subcommands) {
            val a = (ancestors + parent?.command).filterNotNull()
            check(command !in a) { "Command ${command.commandName} already registered" }
            command.createContext(argv, currentContext, a)
        }
        return _context!!
    }

    /**
     * Return the parameters that should be sent to the help formatter when this command's [getFormattedHelp] is
     * called.
     */
    open fun allHelpParams(): List<ParameterHelp> {
        return listOf(
            _options.mapNotNull { it.parameterHelp(currentContext) },
            _arguments.mapNotNull { it.parameterHelp(currentContext) },
            _groups.mapNotNull { it.parameterHelp(currentContext) },
            _subcommands.mapNotNull { c ->
                ParameterHelp.Subcommand(
                    c.commandName,
                    c.shortHelp(currentContext),
                    c.helpTags
                ).takeUnless { c.hidden }
            }
        ).flatten()
    }

    private fun getCommandNameWithParents(): String {
        if (_context == null) resetContext(null)
        return currentContext.commandNameWithParents().joinToString(" ")
    }

    private fun generateCompletion() {
        if (autoCompleteEnvvar == null) return
        val envvar = when {
            autoCompleteEnvvar.isBlank() -> "_${commandName.replace("-", "_").uppercase()}_COMPLETE"
            else -> autoCompleteEnvvar
        }

        val envval = currentContext.readEnvvar(envvar) ?: return

        CompletionGenerator.throwCompletionMessage(this, envval)
    }

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
    protected fun shortHelp(context: Context): String =
        Regex("""\s*(?:```)?\s*(.+)""").find(commandHelp(context))?.groups?.get(1)?.value ?: ""

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
     * This is called automatically for the built-in options, but you need to call this if you want to add a
     * custom option.
     */
    fun registerOption(option: Option) {
        val names = registeredOptionNames()
        for (name in option.names) {
            require(name !in names) { "Duplicate option name $name" }
        }
        if (option.acceptsNumberValueWithoutName) {
            require(_options.none { it.acceptsNumberValueWithoutName }) {
                "Multiple options with acceptsNumberValueWithoutName"
            }
        }
        _options += option
    }

    override fun registerOption(option: GroupableOption) = registerOption(option as Option)

    /**
     * Register an argument with this command.
     *
     * This is called automatically for the built-in arguments, but you need to call this if you want to add a
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
     * This is called automatically for built-in groups, but you need to call this if you want to
     * add a custom group.
     *
     * If you call this manually, you also need to call [registerOption] for any options within the group.
     */
    fun registerOptionGroup(group: ParameterGroup) {
        require(group !in _groups) { "Cannot register the same group twice" }
        require(group.groupName == null || _groups.none { it.groupName == group.groupName }) {
            "Cannot register the same group name twice"
        }
        _groups += group
    }


    /**
     * Return the help string for this command, optionally with an [error].
     *
     * Return `null` if the error does not have a message (e.g. [ProgramResult])
     */
    fun getFormattedHelp(error: CliktError? = null): String? {
        if (error != null && error !is ContextCliktError) {
            return error.message
        }

        val ctx = (error as? ContextCliktError)?.context ?: _context ?: resetContext(null)
        val cmd = ctx.command
        val programName = cmd.getCommandNameWithParents()
        return ctx.helpFormatter(ctx).formatHelp(
            error as? UsageError,
            cmd.commandHelp(ctx),
            cmd.commandHelpEpilog(ctx),
            cmd.allHelpParams(),
            programName
        )
    }

    /**
     * Echo the string returned by [getFormattedHelp].
     */
    fun echoFormattedHelp(error: CliktError? = null) {
        val msg = getFormattedHelp(error)
        if (msg != null) {
            echo(msg, err = error?.printError ?: false)
        }
    }

    /**
     * A list of command aliases.
     *
     * If the user enters a command that matches a key in this map, the command is replaced with the
     * corresponding value in the map. The aliases aren't recursive, so aliases won't be looked up again while
     * tokens from an existing alias are being parsed.
     */
    open fun aliases(): Map<String, List<String>> = emptyMap()

    /** Print a line break to `stdout` */
    fun echo() {
        echo("")
    }

    /**
     * Print the [message] to the screen.
     *
     * This is similar to [print] or [println], but converts newlines to the system line separator.
     *
     * @param message The message to print.
     * @param trailingNewline If true, behave like [println], otherwise behave like [print]
     * @param err If true, print to stderr instead of stdout
     */
    fun echo(
        message: Any?,
        trailingNewline: Boolean = true,
        err: Boolean = false,
    ) {
        if (trailingNewline) {
            currentContext.terminal.println(message, stderr = err)
        } else {
            currentContext.terminal.print(message, stderr = err)
        }
    }

    /**
     * Parse the command line and throw an exception if parsing fails.
     *
     * You should use [main] instead unless you want to handle output yourself.
     */
    fun parse(argv: List<String>, parentContext: Context? = null) {
        generateCompletion()
        Parser.parse(argv, this.currentContext)
    }

    fun parse(argv: Array<String>, parentContext: Context? = null) {
        parse(argv.asList(), parentContext)
    }

    /**
     * Parse the command line and print helpful output if any errors occur.
     *
     * This function calls [parse] and catches any [CliktError]s that are thrown, exiting the process with the specified
     * [status code][CliktError.statusCode]. Other errors are allowed to pass through.
     *
     * If you don't want Clikt to exit your process, call [parse] instead.
     */
    fun main(argv: List<String>) {
        try {
            parse(argv)
        } catch (e: CliktError) {
            echoFormattedHelp(e)
            exitProcessMpp(e.statusCode)
        }
    }

    fun main(argv: Array<out String>) = main(argv.asList())

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
        append("<${this@BaseCliktCommand.classSimpleName()} name=$commandName")

        if (_options.isNotEmpty()) {
            _options.joinTo(this, prefix = " options=[", postfix = "]")
        }

        if (_arguments.isNotEmpty()) {
            _arguments.joinTo(this, prefix = " arguments=[", postfix = "]")
        }

        if (_subcommands.isNotEmpty()) {
            _subcommands.joinTo(this, prefix = " subcommands=[", postfix = "]")
        }

        append(">")
    }
}

//abstract class CliktCommand : BaseCliktCommand<() -> Unit>() {
//    override fun run() {
//        throw PrintHelpMessage(currentContext)
//    }
//}

/** Add the given commands as a subcommand of this command. */
fun <RunnerT, CommandT> CommandT.subcommands(commands: Iterable<CommandT>): CommandT
        where CommandT : BaseCliktCommand<RunnerT> = apply {
    _subcommands = _subcommands + commands
}

/** Add the given commands as a subcommand of this command. */
fun <RunnerT, CommandT> CommandT.subcommands(vararg commands: CommandT): CommandT
        where CommandT : BaseCliktCommand<RunnerT> = apply {
    _subcommands = _subcommands + commands
}

/**
 * Configure this command's [Context].
 *
 * Context property values are normally inherited from the parent context, but you can override any of them
 * here.
 */
fun <T : BaseCliktCommand<*>> T.context(block: Context.Builder.() -> Unit): T = apply {
    // save the old config to allow multiple context calls
    val c = _contextConfig
    _contextConfig = {
        c()
        block()
    }
}

private fun Any.classSimpleName(): String = this::class.simpleName.orEmpty().split("$").last()

private fun BaseCliktCommand<*>.inferCommandName(): String {
    val name = classSimpleName()

    val suffixes = setOf("Command", "Commands")
    if (name in suffixes) return name.lowercase()

    val nameWithoutSuffixes = suffixes.fold(name) { acc, s -> acc.removeSuffix(s) }
    val lowerUpperRegex = Regex("([a-z])([A-Z])")

    return nameWithoutSuffixes.replace(lowerUpperRegex) {
        "${it.groupValues[1]}-${it.groupValues[2]}"
    }.lowercase()
}

/** A short for accessing the terminal from the [currentContext][CliktCommand.currentContext] */
val BaseCliktCommand<*>.terminal: Terminal
    get() = currentContext.terminal
