package com.github.ajalt.clikt.parser

// TODO docs, params, and formatting for help

open class CliktError(message: String) : Exception(message)

/**
 * An internal exception that signals a usage error.
 *
 * This typically aborts any further handling.
 */
open class UsageError(message: String) : CliktError(message)

/** Base class for parameter usage errors. */
open class BadParameter(message: String) : UsageError("Error: $message")

/** A required option or argument was not provided */
open class MissingParameter(paramType: String, paramNames: List<String>, message: String = "") :
        BadParameter("Missing $paramType${paramNames.joinToString(" / ", " ").inb()}.${message.inb(" $message.")}")

/** An option was provided that does not exist. */
open class NoSuchOption(optionName: String) : UsageError("no such option $optionName")

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
