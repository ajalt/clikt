package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.longestName

/**
 * An exception during command line processing that should be shown to the user.
 *
 * If calling [CliktCommand.main], these exceptions will be caught and the appropriate info will be printed.
 *
 * @property statusCode The value to use as the exit code for the process. If you use
 *   [CliktCommand.main], it will pass this value to `exitProcess` after printing [message]. Defaults
 *   to 1.
 */
open class CliktError(
    message: String? = null,
    cause: Exception? = null,
    val statusCode: Int = 0,
    val printError: Boolean = true,
) : RuntimeException(message, cause)

/**
 * An exception that indicates that the command's help should be printed.
 *
 * Execution should be immediately halted.
 *
 * @property error If true, execution should halt with an error. Otherwise, execution halt with no error code.
 */
class PrintHelpMessage(val command: CliktCommand, val error: Boolean = false) : CliktError(printError = false)

/**
 * An exception that indicates that a message should be printed.
 *
 * Execution should be immediately halted.
 */
open class PrintMessage(
    message: String,
    statusCode: Int = 0,
    printError: Boolean = false,
) : CliktError(message, statusCode = statusCode, printError = printError)

/**
 * Indicate that the program finished in a controlled manner, and should complete with the given [statusCode]
 */
class ProgramResult(statusCode: Int) : CliktError(statusCode = statusCode)

/**
 * An internal error that signals Clikt to abort.
 */
class Abort : CliktError(statusCode = 1)

/**
 * An exception that indicates that shell completion code should be printed.
 *
 * Execution should be immediately halted without an error.
 */
class PrintCompletionMessage(message: String) : PrintMessage(message)

/**
 * An internal exception that signals a usage error.
 *
 * @property message The error message. Subclasses can leave this null and use [formatMessage] instead.
 * @property paramName The name of the parameter that caused the error. If possible, this should be
 *   set to the actual name used. Will be set automatically if thrown from a `convert` lambda.
 * @property statusCode The process status code to use if exiting the process as a result of this error.
 */
open class UsageError(
    message: String?,
    var paramName: String? = null,
    statusCode: Int = 1,
) : CliktError(message, statusCode = statusCode) {
    constructor(message: String, argument: Argument, statusCode: Int = 1)
            : this(message, argument.name, statusCode)

    constructor(message: String, option: Option, statusCode: Int = 1)
            : this(message, option.longestName(), statusCode)

    constructor(argument: Argument, statusCode: Int = 1)
            : this(null, argument.name, statusCode)

    constructor(option: Option, statusCode: Int = 1)
            : this(null, option.longestName(), statusCode)

    open fun formatMessage(localization: Localization): String = message ?: ""

    /**
     *  The context of the command that raised this error.
     *
     *  Will be set automatically if thrown during command line processing.
     */
    var context: Context? = null
}

/**
 * Multiple usage [errors] occurred.
 */
class MultiUsageError(
    val errors: List<UsageError>,
) : UsageError(null, statusCode = errors.first().statusCode) {
    companion object {
        /**
         * Given a list of UsageErrors, return `null` if it's empty, the error if there's only one, and a
         * [MultiUsageError] containing all the errors otherwise.
         */
        fun buildOrNull(errors: List<UsageError>): UsageError? = when (errors.size) {
            0 -> null
            1 -> errors[0]
            else -> MultiUsageError(errors.flatMap { (it as? MultiUsageError)?.errors ?: listOf(it) })
        }
    }

    override fun formatMessage(localization: Localization): String {
        return errors.joinToString("\n") { it.formatMessage(localization) }
    }
}

/**
 * A parameter was given the correct number of values, but of invalid format or type.
 */
class BadParameterValue : UsageError {
    constructor(message: String) : super(message, null)
    constructor(message: String, paramName: String) : super(message, paramName)
    constructor(message: String, argument: Argument) : super(message, argument)
    constructor(message: String, option: Option) : super(message, option)

    override fun formatMessage(localization: Localization): String {
        val m = message.takeUnless { it.isNullOrBlank() }
        val p = paramName?.takeIf { it.isNotBlank() }

        return when {
            m == null && p == null -> localization.badParameter()
            m == null && p != null -> localization.badParameterWithParam(p)
            m != null && p == null -> localization.badParameterWithMessage(m)
            m != null && p != null -> localization.badParameterWithMessageAndParam(p, m)
            else -> error("impossible")
        }
    }
}

/** A required option was not provided */
class MissingOption(option: Option) : UsageError(option) {
    override fun formatMessage(localization: Localization) = localization.missingOption(paramName ?: "")
}

/** A required argument was not provided */
class MissingArgument(argument: Argument) : UsageError(argument) {
    override fun formatMessage(localization: Localization) = localization.missingArgument(paramName ?: "")
}

/** A subcommand was provided that does not exist. */
class NoSuchSubcommand(
    paramName: String,
    private val possibilities: List<String> = emptyList(),
) : UsageError(null, paramName) {
    override fun formatMessage(localization: Localization): String {
        return localization.noSuchSubcommand(paramName ?: "", possibilities)
    }
}


/** An option was provided that does not exist. */
class NoSuchOption(
    paramName: String,
    private val possibilities: List<String> = emptyList(),
) : UsageError(null, paramName) {
    override fun formatMessage(localization: Localization): String {
        return localization.noSuchOption(paramName ?: "", possibilities)
    }
}


/** An option was supplied but the number of values supplied to the option was incorrect. */
class IncorrectOptionValueCount(
    private val minValues: Int,
    paramName: String,
) : UsageError(null, paramName) {
    constructor(option: Option, paramName: String) : this(option.nvalues.first, paramName)

    override fun formatMessage(localization: Localization): String {
        return localization.incorrectOptionValueCount(paramName ?: "", minValues)
    }
}

/** An argument was supplied but the number of values supplied was incorrect. */
class IncorrectArgumentValueCount(
    val nvalues: Int,
    argument: Argument,
) : UsageError(argument) {
    constructor(argument: Argument) : this(argument.nvalues, argument)

    override fun formatMessage(localization: Localization): String {
        return localization.incorrectArgumentValueCount(paramName ?: "", nvalues)
    }
}

class MutuallyExclusiveGroupException(
    val names: List<String>,
) : UsageError(null) {
    init {
        require(names.size > 1) { "must provide at least two names" }
    }

    override fun formatMessage(localization: Localization): String {
        return localization.mutexGroupException(names.first(), names.drop(1))
    }
}

/** A required configuration file was not found. */
class FileNotFound(
    val filename: String,
) : UsageError(null) {
    override fun formatMessage(localization: Localization): String {
        return localization.fileNotFound(filename)
    }
}

/** A configuration file failed to parse correctly */
class InvalidFileFormat(
    private val filename: String,
    message: String,
    private val lineno: Int? = null,
) : UsageError(message) {
    override fun formatMessage(localization: Localization): String {
        return when (lineno) {
            null -> localization.invalidFileFormat(filename, message!!)
            else -> localization.invalidFileFormat(filename, lineno, message!!)
        }
    }
}
