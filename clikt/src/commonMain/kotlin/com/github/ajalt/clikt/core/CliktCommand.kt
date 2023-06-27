package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.completion.CompletionGenerator
import com.github.ajalt.clikt.mpp.exitProcessMpp
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parsers.Parser
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.YesNoPrompt

/**
 * The [CliktCommand] is the core of command line interfaces in Clikt.
 *
 * Command line interfaces created by creating a subclass of [CliktCommand] with properties defined with
 * [option] and [argument]. You can then parse `argv` by calling [main], which will take care of printing
 * errors and help to the user. If you want to handle output yourself, you can use [parse] instead.
 *
 * Once the command line has been parsed and all the parameters are populated, [run] is called.
 */
@Suppress("PropertyName")
@ParameterHolderDsl
abstract class CliktCommand(
    /**
     * The help for this command. The first line is used in the usage string, and the entire string
     * is used in the help output. Paragraphs are automatically re-wrapped to the terminal width.
     */
    help: String = "",
    /**
     * Text to display at the end of the full help output. It is automatically re-wrapped to the
     * terminal width.
     */
    epilog: String = "",
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
     * Extra information about this option to pass to the help formatter.
     */
    val helpTags: Map<String, String> = emptyMap(),
    /**
     * The envvar to use to enable shell autocomplete script generation. Set to null to disable
     * generation.
     */
    private val autoCompleteEnvvar: String? = "",
    /**
     * If true, allow multiple of this command's subcommands to be called sequentially. This will
     * disable `allowInterspersedArgs` on the context of this command and its descendants. This
     * functionality is experimental, and may change in a future release.
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
     * The name of this command, used in help output.
     *
     * You can set this by passing `name` to the [CliktCommand] constructor.
     */
    val commandName: String = name ?: inferCommandName()

    /**
     * The help text for this command.
     *
     * You can set this by passing `help` to the [CliktCommand] constructor, or by overriding this
     * property.
     */
    open val commandHelp: String = help

    /**
     * Help text to display at the end of the help output, after any parameters.
     *
     * You can set this by passing `epilog` to the [CliktCommand] constructor, or by overriding this
     * property.
     */
    open val commandHelpEpilog: String = epilog

    internal var _subcommands: List<CliktCommand> = emptyList()
    internal val _options: MutableList<Option> = mutableListOf()
    internal val _arguments: MutableList<Argument> = mutableListOf()
    internal val _groups: MutableList<ParameterGroup> = mutableListOf()
    internal var _contextConfig: Context.Builder.() -> Unit = {}
    private var _context: Context? = null
    private val _messages = mutableListOf<String>()

    private fun registeredOptionNames() = _options.flatMapTo(mutableSetOf()) { it.names }

    private fun createContext(
        argv: List<String>,
        parent: Context?,
        ancestors: List<CliktCommand>,
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
            _subcommands.mapNotNull {
                when {
                    it.hidden -> null
                    else -> ParameterHelp.Subcommand(it.commandName, it.shortHelp(), it.helpTags)
                }
            }
        ).flatten()
    }

    private fun getCommandNameWithParents(): String {
        if (_context == null) createContext(emptyList(), null, emptyList())
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
    protected fun shortHelp(): String =
        Regex("""\s*(?:```)?\s*(.+)""").find(commandHelp)?.groups?.get(1)?.value ?: ""

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

        val ctx = (error as? ContextCliktError)?.context
            ?: _context ?: createContext(emptyList(), null, emptyList())
        val cmd = ctx.command
        val programName = cmd.getCommandNameWithParents()
        return ctx.helpFormatter(ctx).formatHelp(
            error as? UsageError,
            cmd.commandHelp,
            cmd.commandHelpEpilog,
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
     * Print [text] to the user and return the value they enter.
     *
     * @param text The message asking for input to show the user
     * @param default The value to return if the user enters an empty line, or `null` to require a value
     * @param showDefault If true and a [default] is specified, add the default value to the prompt
     * @param hideInput If true, the user's input will be treated like a password and hidden from
     *   the screen. This value will be ignored on platforms where it is not supported.
     * @param choices The set of values that the user must choose from.
     * @param promptSuffix A string to append after [text] when showing the user the prompt
     * @param invalidChoiceMessage The message to show the user if [choices] is specified,
     *   and they enter a value that isn't one of the choices.
     *
     * @return The user input, or `null` if EOF was reached before this function was called.
     *
     * @see Terminal.prompt
     */
    fun prompt(
        text: String,
        default: String? = null,
        showDefault: Boolean = true,
        showChoices: Boolean = true,
        hideInput: Boolean = false,
        choices: Collection<String> = emptyList(),
        promptSuffix: String = ": ",
        invalidChoiceMessage: String = "Invalid value, choose from ",
    ): String? = currentContext.terminal.prompt(
        text,
        default,
        showDefault,
        showChoices,
        hideInput,
        choices,
        promptSuffix,
        invalidChoiceMessage
    )

    /**
     * Print [text] to the user and return the value they enter.
     *
     * @param text The message asking for input to show the user
     * @param default The value to return if the user enters an empty line, or `null` to require a value
     * @param showDefault If true and a [default] is specified, add the default value to the prompt
     * @param hideInput If true, the user's input will be treated like a password and hidden from
     *   the screen. This value will be ignored on platforms where it is not supported.
     * @param choices The set of values that the user must choose from.
     * @param promptSuffix A string to append after [text] when showing the user the prompt
     * @param invalidChoiceMessage The message to show the user if [choices] is specified,
     *   and they enter a value that isn't one of the choices.
     * @param convert A function that converts the user input to the final value
     *
     * @return The converted user input, or `null` if EOF was reached before this function was called.
     *
     * @see Terminal.prompt
     */
    fun <T> prompt(
        text: String,
        default: T? = null,
        showDefault: Boolean = true,
        showChoices: Boolean = true,
        hideInput: Boolean = false,
        choices: Collection<T> = emptyList(),
        promptSuffix: String = ": ",
        invalidChoiceMessage: String = "Invalid value, choose from ",
        convert: (String) -> ConversionResult<T>,
    ): T? = currentContext.terminal.prompt(
        text,
        default,
        showDefault,
        showChoices,
        hideInput,
        choices,
        promptSuffix,
        invalidChoiceMessage,
        convert
    )

    /**
     * Prompt for user confirmation.
     *
     * @param text The message asking for input to show the user
     * @param default The value to return if the user enters an empty line, or `null` to require a value
     * @param uppercaseDefault If true and [default] is not `null`, the default choice will be shown in uppercase.
     * @param showChoices If true, the choices will be added to the [prompt]
     * @param choiceStrings The strings to accept for `true` and `false` inputs
     * @param promptSuffix A string to append after [prompt] when showing the user the prompt
     * @param invalidChoiceMessage The message to show the user if they enter a value that isn't one of the [choiceStrings].
     *
     * @return The converted user input, or `null` if EOF was reached before this function was called.
     *
     * @see Terminal.prompt
     */
    fun confirm(
        text: String,
        default: Boolean? = null,
        uppercaseDefault: Boolean = true,
        showChoices: Boolean = true,
        choiceStrings: List<String> = listOf("y", "n"),
        promptSuffix: String = ": ",
        invalidChoiceMessage: String = "Invalid value, choose from ",
    ): Boolean? {
        return YesNoPrompt(
            text,
            currentContext.terminal,
            default,
            uppercaseDefault,
            showChoices,
            choiceStrings,
            promptSuffix,
            invalidChoiceMessage,
        ).ask()
    }

    /**
     * Parse the command line and throw an exception if parsing fails.
     *
     * You should use [main] instead unless you want to handle output yourself.
     */
    fun parse(argv: List<String>, parentContext: Context? = null) {
        createContext(argv, parentContext, emptyList())
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
        append("<${this@CliktCommand.classSimpleName()} name=$commandName")

        if (_options.isNotEmpty() || _arguments.isNotEmpty() || _subcommands.isNotEmpty()) {
            append(" ")
        }

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
            _subcommands.joinTo(this, " ", prefix = " subcommands=[", postfix = "]")
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
// save the old config to allow multiple context calls
    val c = _contextConfig
    _contextConfig = {
        c()
        block()
    }
}

private fun Any.classSimpleName(): String = this::class.simpleName.orEmpty().split("$").last()

private fun CliktCommand.inferCommandName(): String {
    val name = classSimpleName()
    if (name == "Command") return "command"
    return name.removeSuffix("Command").replace(Regex("([a-z])([A-Z])")) {
        "${it.groupValues[1]}-${it.groupValues[2]}"
    }.lowercase()
}
