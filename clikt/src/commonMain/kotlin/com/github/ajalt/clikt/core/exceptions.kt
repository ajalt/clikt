package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.longestName

/**
 * An internal error that signals Clikt to abort.
 *
 * @property error If true, print "Aborted" and exit with an error code. Otherwise, exit with no error code.
 */
class Abort(val error: Boolean = true) : RuntimeException()

/**
 * An exception during command line processing that should be shown to the user.
 *
 * If calling [CliktCommand.main], these exceptions will be caught and the appropriate info will be printed.
 */
open class CliktError(message: String? = null, cause: Exception? = null) : RuntimeException(message, cause)

/**
 * An exception that indicates that the command's help should be printed.
 *
 * Execution should be immediately halted.
 *
 * @property error If true, execution should halt with an error. Otherwise, execution halt with no error code.
 */
class PrintHelpMessage(val command: CliktCommand, val error: Boolean = false) : CliktError()

/**
 * An exception that indicates that a message should be printed.
 *
 * Execution should be immediately halted.
 *
 * @property error If true, execution should halt with an error. Otherwise, execution halt with no error code.
 */
open class PrintMessage(message: String, val error: Boolean = false) : CliktError(message)

/**
 * Indicate that that the program finished in a controlled manner, and should complete with the given [statusCode]
 */
class ProgramResult(val statusCode: Int) : CliktError()

/**
 * An exception that indicates that shell completion code should be printed.
 *
 * Execution should be immediately halted without an error.
 *
 * @param forceUnixLineEndings if true, all line endings in the message should be `\n`, regardless
 *   of the current operating system.
 */
class PrintCompletionMessage(message: String, val forceUnixLineEndings: Boolean) : PrintMessage(message)

/**
 * An internal exception that signals a usage error.
 *
 * The [option] and [argument] properties are used in message formatting, and can be set after the exception
 * is created. If this is thrown inside a call to [convert], the [argument] or [option] value will be set
 * automatically
 *
 * @property text Extra text to add to the message. Not all subclasses uses this.
 * @property paramName The name of the parameter that caused the error. If possible, this should be set to the
 *   actual name used. If not set, it will be inferred from [argument] or [option] if either is set.
 * @property option The option that caused this error. This may be set after the error is thrown.
 * @property argument The argument that caused this error. This may be set after the error is thrown.
 * @property statusCode The value to use as the exit code for the process. If you use
 *   [CliktCommand.main], it will pass this value to `exitProcess` after printing [message]. Defaults to 1.
 */
open class UsageError private constructor(
        val text: String? = null,
        var paramName: String? = null,
        var option: Option? = null,
        var argument: Argument? = null,
        var context: Context? = null,
        val statusCode: Int = 1) : CliktError() {
    constructor(text: String, paramName: String? = null, context: Context? = null, statusCode: Int = 1)
            : this(text, paramName, null, null, context, statusCode)

    constructor(text: String, argument: Argument, context: Context? = null, statusCode: Int = 1)
            : this(text, null, null, argument, context, statusCode)

    constructor(text: String, option: Option, context: Context? = null, statusCode: Int = 1)
            : this(text, null, option, null, context, statusCode)

    fun helpMessage(): String = buildString {
        context?.let { append(it.command.getFormattedUsage()).append("\n\n") }
        append("Error: ").append(formatMessage())
    }

    override val message: String? get() = formatMessage()

    protected open fun formatMessage(): String = text ?: ""

    protected fun inferParamName(): String = when {
        paramName != null -> paramName!!
        option != null -> option?.longestName() ?: ""
        argument != null -> argument!!.name
        else -> ""
    }
}

/**
 * A parameter was given the correct number of values, but of invalid format or type.
 */
open class BadParameterValue : UsageError {
    constructor(text: String, context: Context? = null) : super(text, null, context)
    constructor(text: String, paramName: String, context: Context? = null) : super(text, paramName, context)
    constructor(text: String, argument: Argument, context: Context? = null) : super(text, argument, context)
    constructor(text: String, option: Option, context: Context? = null) : super(text, option, context)

    override fun formatMessage(): String {
        if (inferParamName().isEmpty()) return "Invalid value: $text"
        return "Invalid value for \"${inferParamName()}\": $text"
    }
}

/** A required parameter was not provided */
open class MissingParameter : UsageError {
    constructor(argument: Argument, context: Context? = null) : super("", argument, context) {
        this.paramType = "argument"
    }

    constructor(option: Option, context: Context? = null) : super("", option, context) {
        this.paramType = "option"
    }

    private val paramType: String

    override fun formatMessage(): String {
        return "Missing $paramType \"${inferParamName()}\"."
    }
}

/** A parameter was provided that does not exist. */
open class NoSuchParameter(
        protected val parameterType: String,
        protected val givenName: String,
        protected val possibilities: List<String> = emptyList(),
        context: Context? = null
) : UsageError("", context = context) {
    override fun formatMessage(): String {
        return "no such ${parameterType}: \"$givenName\"." + when {
            possibilities.size == 1 -> " Did you mean \"${possibilities[0]}\"?"
            possibilities.size > 1 -> possibilities.joinToString(
                    prefix = " (Possible ${parameterType}s: ", postfix = ")")
            else -> ""
        }
    }
}

/** A subcommand was provided that does not exist. */
open class NoSuchSubcommand(
        givenName: String,
        possibilities: List<String> = emptyList(),
        context: Context? = null
) : NoSuchParameter("subcommand", givenName, possibilities, context)


/** An option was provided that does not exist. */
open class NoSuchOption(
        givenName: String,
        possibilities: List<String> = emptyList(),
        context: Context? = null
) : NoSuchParameter("option", givenName, possibilities, context)

/** An option was supplied but the number of values supplied to the option was incorrect. */
open class IncorrectOptionValueCount(
        option: Option,
        private val givenName: String,
        context: Context? = null
) : UsageError("", option, context) {
    override fun formatMessage(): String {
        return when (option!!.nvalues) {
            0 -> "$givenName option does not take a value"
            1 -> "$givenName option requires an argument"
            else -> "$givenName option requires ${option!!.nvalues} arguments"
        }
    }
}

/** An argument was supplied but the number of values supplied was incorrect. */
open class IncorrectArgumentValueCount(
        argument: Argument,
        context: Context? = null
) : UsageError("", argument, context) {
    override fun formatMessage(): String {
        return "argument ${inferParamName()} takes ${argument!!.nvalues} values"
    }
}

open class MutuallyExclusiveGroupException(
        protected val names: List<String>,
        context: Context? = null
) : UsageError("", context = context) {
    init {
        require(names.size > 1) { "must provide at least two names" }
    }

    override fun formatMessage(): String {
        return "option ${names.first()} cannot be used with ${names.drop(1).joinToString(" or ")}"
    }
}

/** A required configuration file was not found. */
class FileNotFound(filename: String) : UsageError("$filename not found")

/** A configuration file failed to parse correctly */
class InvalidFileFormat(filename: String, message: String, lineno: Int? = null) : UsageError(
        "incorrect format in file $filename${lineno?.let { " line $it" } ?: ""}}: $message"
)
