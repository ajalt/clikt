package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.Option

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
 * Execution should be immediately halted with an error.
 */
class PrintHelpMessage(val command: CliktCommand) : CliktError()

/**
 * An exception that indicates that a message should be printed.
 *
 * Execution should be immediately halted without an error.
 */
class PrintMessage(message: String) : CliktError(message)

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
 */
open class UsageError private constructor(
        val text: String? = null,
        var paramName: String? = null,
        var option: Option? = null,
        var argument: Argument? = null) : CliktError() {
    constructor(text: String, paramName: String? = null)
            : this(text, paramName, null, null)

    constructor(text: String, argument: Argument)
            : this(text, null, null, argument)

    constructor(text: String, option: Option)
            : this(text, null, option, null)

    fun helpMessage(context: Context? = null): String = buildString {
        context?.let { append(it.command.getFormattedUsage()).append("\n\n") }
        append("Error: ").append(formatMessage())
    }

    override val message: String? get() = formatMessage()

    protected open fun formatMessage(): String = text ?: ""

    protected fun inferParamName(): String = when {
        paramName != null -> paramName!!
        option != null -> option?.names?.maxBy { it.length } ?: ""
        argument != null -> argument!!.name
        else -> ""
    }
}

/**
 * A parameter was given the correct number of values, but of invalid format or type.
 */
open class BadParameterValue : UsageError {
    constructor(text: String) : super(text)
    constructor(text: String, paramName: String) : super(text, paramName)
    constructor(text: String, argument: Argument) : super(text, argument)
    constructor(text: String, option: Option) : super(text, option)

    override fun formatMessage(): String {
        if (inferParamName().isEmpty()) return "Invalid value: $text"
        return "Invalid value for \"${inferParamName()}\": $text"
    }
}

/** A required parameter was not provided */
open class MissingParameter : UsageError {
    /**
     * @param paramName The name of the parameter that caused the error
     * @param text Extra text to display in the message
     * @param paramType A string indicating the type of parameter.
     */
    constructor(paramName: String, paramType: String = "parameter") : super("", paramName) {
        this.paramType = paramType
    }

    constructor(argument: Argument) : super("", argument) {
        this.paramType = "argument"
    }

    constructor(option: Option) : super("", option) {
        this.paramType = "option"
    }

    private val paramType: String

    override fun formatMessage(): String {
        return "Missing $paramType \"${inferParamName()}\"."
    }
}

/** An option was provided that does not exist. */
open class NoSuchOption(protected val givenName: String,
                        protected val possibilities: List<String> = emptyList()) : UsageError("") {
    override fun formatMessage(): String {
        return "no such option: \"$givenName\"." + when {
            possibilities.size == 1 -> " Did you mean \"${possibilities[0]}\"?"
            possibilities.size > 1 -> possibilities.joinToString(
                    prefix = " (Possible options: ", postfix = ")")
            else -> ""
        }
    }
}

/** An option was supplied but the number of values supplied to the option was incorrect. */
open class IncorrectOptionValueCount(option: Option,
                                     private val givenName: String) : UsageError("", option) {
    override fun formatMessage(): String {
        return when (option!!.nvalues) {
            0 -> "$givenName option does not take a value"
            1 -> "$givenName option requires an argument"
            else -> "$givenName option requires ${option!!.nvalues} arguments"
        }
    }
}

/** An argument was supplied but the number of values supplied was incorrect. */
open class IncorrectArgumentValueCount(argument: Argument) : UsageError("", argument) {
    override fun formatMessage(): String {
        return "argument ${inferParamName()} takes ${argument!!.nvalues} values"
    }
}
