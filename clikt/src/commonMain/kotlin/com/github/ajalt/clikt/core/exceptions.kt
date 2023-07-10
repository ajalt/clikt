package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.ParameterFormatter
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.longestName

/**
 * An exception during command line processing that should be shown to the user.
 *
 * If calling [CliktCommand.main], these exceptions will be caught and the appropriate info will be printed.
 */
open class CliktError(
    message: String? = null,
    cause: Exception? = null,
    /**
     * The value to use as the exit code for the process.
     *
     * If you use [CliktCommand.main], it will pass this value to `exitProcess` after printing
     * [message]. Defaults to 1.
     */
    val statusCode: Int = 1,
    /**
     * If true, the error message should be printed to stderr.
     */
    val printError: Boolean = true,
) : RuntimeException(message, cause)

/** An interface for CliktErrors that have a context attached */
interface ContextCliktError {
    /**
     * The context of the command that raised this error.
     *
     * Will be set automatically if thrown during command line processing.
     */
    var context: Context?
}

/**
 * An exception that indicates that the command's help should be printed.
 *
 * Execution should be immediately halted.
 */
class PrintHelpMessage(
    override var context: Context?,
    /**
     * If true, the error message should be printed to stderr.
     */
    val error: Boolean = false,
) : CliktError(printError = false), ContextCliktError

/**
 * An exception that indicates that a message should be printed.
 *
 * Execution should be immediately halted.
 */
open class PrintMessage(
    /** The message to print */
    message: String,
    /**
     * The value to use as the exit code for the process.
     *
     * If you use [CliktCommand.main], it will pass this value to `exitProcess` after printing
     * [message]. Defaults to 0.
     */
    statusCode: Int = 0,
    /**
     * If true, the error message should be printed to stderr.
     */
    printError: Boolean = false,
) : CliktError(message, statusCode = statusCode, printError = printError)

/**
 * Indicate that the program finished in a controlled manner, and should complete with the given [statusCode]
 */
open class ProgramResult(statusCode: Int) : CliktError(statusCode = statusCode)

/**
 * An internal error that signals Clikt to abort.
 */
class Abort : ProgramResult(statusCode = 1)

/**
 * An exception that indicates that shell completion code should be printed.
 *
 * Execution should be immediately halted without an error.
 */
class PrintCompletionMessage(message: String) : PrintMessage(message, statusCode = 0)

/** An exception that signals a user error. */
open class UsageError(
    /** The error message. Subclasses can leave this null and use [formatMessage] instead. */
    message: String?,
    /**
     * The name of the parameter that caused the error.
     *
     * If possible, this should be set to the actual name used. Will be set automatically if thrown
     * from a `convert` lambda.
     */
    var paramName: String? = null,
    /**
     * The value to use as the exit code for the process.
     *
     * If you use [CliktCommand.main], it will pass this value to `exitProcess` after printing
     * [message]. Defaults to 1.
     */
    statusCode: Int = 1,
) : CliktError(message, statusCode = statusCode), ContextCliktError {
    constructor(message: String, argument: Argument, statusCode: Int = 1)
            : this(message, argument.name, statusCode)

    constructor(message: String, option: Option, statusCode: Int = 1)
            : this(message, option.longestName(), statusCode)

    constructor(argument: Argument, statusCode: Int = 1)
            : this(null, argument.name, statusCode)

    constructor(option: Option, statusCode: Int = 1)
            : this(null, option.longestName(), statusCode)

    open fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return message ?: ""
    }

    override var context: Context? = null
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
            else -> MultiUsageError(errors.flatMap {
                (it as? MultiUsageError)?.errors ?: listOf(it)
            })
        }
    }

    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return errors.joinToString("\n") { it.formatMessage(localization, formatter) }
    }
}

/**
 * A parameter was given the correct number of values, but of invalid format or type.
 */
class BadParameterValue : UsageError {
    private enum class Kind { Argument, Option }

    private val kind: Kind?

    constructor(message: String) : super(message, null) {
        kind = null
    }

    constructor(message: String, argument: Argument) : super(message, argument) {
        kind = Kind.Argument
    }

    constructor(message: String, option: Option) : super(message, option) {
        kind = Kind.Option
    }

    @Suppress("UNUSED_PARAMETER")
    constructor(message: String, option: Option, name: String) : super(message, name) {
        kind = Kind.Option
    }

    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        val m = message.takeUnless { it.isNullOrBlank() }
        val p = paramName?.takeIf { it.isNotBlank() }?.let {
            when (kind) {
                Kind.Argument -> formatter.formatArgument(it)
                Kind.Option -> formatter.formatOption(it)
                null -> it
            }
        }

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
    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.missingOption(paramName?.let(formatter::formatOption) ?: "")
    }
}

/** A required argument was not provided */
class MissingArgument(argument: Argument) : UsageError(argument) {
    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.missingArgument(paramName?.let(formatter::formatArgument) ?: "")
    }
}

/** A subcommand was provided that does not exist. */
class NoSuchSubcommand(
    paramName: String,
    private val possibilities: List<String> = emptyList(),
) : UsageError(null, paramName) {
    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.noSuchSubcommand(
            paramName?.let(formatter::formatSubcommand) ?: "",
            possibilities.map(formatter::formatSubcommand)
        )
    }
}


/** An option was provided that does not exist. */
class NoSuchOption(
    paramName: String,
    private val possibilities: List<String> = emptyList(),
) : UsageError(null, paramName) {
    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.noSuchOption(
            paramName?.let(formatter::formatOption) ?: "",
            possibilities.map(formatter::formatOption)
        )
    }
}


/** An option was supplied but the number of values supplied to the option was incorrect. */
class IncorrectOptionValueCount(
    private val minValues: Int,
    paramName: String,
) : UsageError(null, paramName) {
    constructor(option: Option, paramName: String) : this(option.nvalues.first, paramName)

    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.incorrectOptionValueCount(
            paramName?.let(formatter::formatOption) ?: "",
            minValues
        )
    }
}

/** An argument was supplied but the number of values supplied was incorrect. */
class IncorrectArgumentValueCount(
    val nvalues: Int,
    argument: Argument,
) : UsageError(argument) {
    constructor(argument: Argument) : this(argument.nvalues, argument)

    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.incorrectArgumentValueCount(
            paramName?.let(formatter::formatArgument) ?: "", nvalues
        )
    }
}

/**
 * Multiple mutually exclusive options were supplied.
 */
class MutuallyExclusiveGroupException(
    val names: List<String>,
) : UsageError(null) {
    init {
        require(names.size > 1) { "must provide at least two names" }
    }

    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.mutexGroupException(
            names.first().let(formatter::formatOption),
            names.drop(1).map(formatter::formatOption)
        )
    }
}

/** A required configuration file was not found. */
class FileNotFound(
    val filename: String,
) : UsageError(null) {
    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return localization.fileNotFound(filename)
    }
}

/** A configuration file failed to parse correctly */
class InvalidFileFormat(
    private val filename: String,
    message: String,
    private val lineno: Int? = null,
) : UsageError(message) {
    override fun formatMessage(localization: Localization, formatter: ParameterFormatter): String {
        return when (lineno) {
            null -> localization.invalidFileFormat(filename, message!!)
            else -> localization.invalidFileFormat(filename, lineno, message!!)
        }
    }
}
