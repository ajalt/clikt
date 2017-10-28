package com.github.ajalt.clikt.parser

// TODO docs, params, and formatting for help

open class CliktError(message: String) : Exception(message)

/**
 * An internal exception that signals a usage error.
 *
 * This typically aborts any further handling.
 */
abstract class UsageError(message: String) : CliktError(message)

/** Base class for parameter usage errors. */
open class BadParameter(message: String) : UsageError("Invalid value: $message")

/** A required option or argument was not provided */
open class MissingParameter(message: String) : BadParameter(message)

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
