package com.github.ajalt.clikt.core

// TODO docs, params, and formatting for output

/** An internal error that signals Clickt to abort. */
class Abort: RuntimeException()

open class CliktError(message: String) : RuntimeException(message)

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
open class UsageError(message: String) : CliktError(message) {
    fun formatMessage(context: Context? = null): String = buildString {
        context?.let { append(it.command.getFormattedUsage()).append("\n\n") }
        append("Error: ").append(message)
    }
}

/** Base class for parameter usage errors. */
open class BadParameter(message: String) : UsageError(message)

/** A required option or argument was not provided */
open class MissingParameter(paramType: String, paramNames: List<String>, message: String = "") :
        BadParameter("Missing $paramType${paramNames.joinToString(" / ", " ").inb()}.${message.inb(" $message.")}")

/** An option was provided that does not exist. */
open class NoSuchOption(optionName: String, possibilities: List<String> = emptyList())
    : UsageError("no such option $optionName" + when {
    possibilities.size == 1 -> ". Did you mean ${possibilities[0]}?"
    possibilities.size > 1 -> possibilities.joinToString(prefix = " (Possible parameters: ", postfix = ")")
    else -> ""
})

/**
 * Raised if an option is supplied but the use of the option was incorrect.
 *
 * This is for instance raised if the number of arguments for an option is not correct
 */
open class BadOptionUsage(message: String) : UsageError(message)

/**
 * Raised if an argument is supplied but the use of the argument was incorrect.
 *
 * This is for instance raised if the number of arguments for an argument is not correct
 */
open class BadArgumentUsage(message: String) : UsageError(message)


// "ifNotBlank"
private fun String.inb(nonempty: String = this, empty: String = "") = if (isBlank()) empty else nonempty
