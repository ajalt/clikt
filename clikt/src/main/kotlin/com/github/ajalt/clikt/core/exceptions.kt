package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.Argument
import com.github.ajalt.clikt.parameters.options.Option

// TODO docs, params, and formatting for output

/** An internal error that signals Clikt to abort. */
class Abort : RuntimeException()

open class CliktError(message: String? = null, cause: Exception? = null) : RuntimeException(message, cause)

/**
 * An exception that indicates that the command's help should be printed, and does not signal an error.
 */
class PrintHelpMessage(val command: CliktCommand) : CliktError("print output")

/**
 * An exception that indicates that the message should be printed, and does not signal an error.
 */
class PrintMessage(message: String) : CliktError(message)

/**
 * An internal exception that signals a usage error.
 *
 * This typically aborts any further handling.
 */
open class UsageError private constructor(
        val text: String? = null,
        val paramName: String? = null,
        var option: Option? = null,
        var argument: Argument<*>? = null) : CliktError() {
    constructor(message: String, paramName: String? = null)
            : this(message, paramName, null, null)

    constructor(message: String, argument: Argument<*>)
            : this(message, null, null, argument)

    constructor(message: String, option: Option)
            : this(message, null, option, null)

    fun helpMessage(context: Context? = null): String = buildString {
        context?.let { append(it.command.getFormattedUsage()).append("\n\n") }
        append("Error: ").append(formatMessage())
    }

    override val message: String? get() = formatMessage()

    protected open fun formatMessage(): String = text ?: ""

    protected fun inferParamName(): String = when {
        paramName != null -> paramName
        option != null -> option?.names?.maxBy { it.length } ?: ""
        argument != null -> argument!!.name
        else -> ""
    }
}

/** Base class for parameter usage errors. */ // TODO docs
open class BadParameter : UsageError {
    constructor(text: String) : super(text)
    constructor(text: String, paramName: String) : super(text, paramName)
    constructor(text: String, argument: Argument<*>) : super(text, argument)
    constructor(text: String, option: Option) : super(text, option)

    override fun formatMessage(): String {
        if (inferParamName().isEmpty()) return "Invalid value: $text"
        return "Invalid value for \"${inferParamName()}\": $text"
    }
}

/** A required option or argument was not provided */
open class MissingParameter : BadParameter {
    constructor(paramName: String, message: String = "", paramType: String = "parameter")
            : super(message, paramName) {
        this.paramType = paramType
    }

    constructor(argument: Argument<*>, message: String = "")
            : super(message, argument) {
        this.paramType = "argument"
    }

    constructor(option: Option, message: String = "")
            : super(message, option) {
        this.paramType = "option"
    }

    private val paramType: String

    override fun formatMessage(): String {
        return "Missing $paramType ${inferParamName()}." +
                if (text.isNullOrBlank()) "" else " $text."
    }
}

/** An option was provided that does not exist. */
open class NoSuchOption(protected val givenName: String,
                        protected val possibilities: List<String> = emptyList()) : UsageError("") {
    override fun formatMessage(): String {
        return "no such option $givenName" + when {
            possibilities.size == 1 -> ". Did you mean ${possibilities[0]}?"
            possibilities.size > 1 -> possibilities.joinToString(
                    prefix = " (Possible parameters: ", postfix = ")")
            else -> ""
        }
    }
}

/**
 * Raised if an option is supplied but the number of values supplied to the option was incorrect.
 */
open class IncorrectOptionNargs(option: Option,
                                private val givenName: String) : UsageError("", option) {
    override fun formatMessage(): String {
        return when (option!!.nargs) {
            0 -> "$givenName option does not take a value"
            1 -> "$givenName option requires an argument"
            else -> "$givenName option requires ${option!!.nargs} arguments"
        }
    }
}

/**
 * Raised if an argument is supplied but the number of values supplied was incorrect.
 */
open class IncorrectArgumentNargs(argument: Argument<*>) : UsageError("", argument) {
    override fun formatMessage(): String {
        return "argument ${inferParamName()} takes ${argument!!.nargs} values"
    }
}
