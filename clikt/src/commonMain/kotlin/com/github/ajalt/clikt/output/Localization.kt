package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.groups.ChoiceGroup
import com.github.ajalt.clikt.parameters.groups.MutuallyExclusiveOptions

/**
 * Strings to use for help output and error messages
 */
interface Localization {
    /** Prefix for any [UsageError] */
    fun usageError() = "Error:"

    /** Message for [BadParameterValue] */
    fun badParameter() = "invalid value"

    /** Message for [BadParameterValue] */
    fun badParameterWithMessage(message: String) = "invalid value: $message"

    /** Message for [BadParameterValue] */
    fun badParameterWithParam(paramName: String) = "invalid value for $paramName"

    /** Message for [BadParameterValue] */
    fun badParameterWithMessageAndParam(paramName: String, message: String) =
        "invalid value for $paramName: $message"

    /** Message for [MissingOption] */
    fun missingOption(paramName: String) = "missing option $paramName"

    /** Message for [MissingArgument] */
    fun missingArgument(paramName: String) = "missing argument $paramName"

    /** Message for [NoSuchSubcommand] */
    fun noSuchSubcommand(name: String, possibilities: List<String>): String {
        return "no such subcommand $name" + when (possibilities.size) {
            0 -> ""
            1 -> ". Did you mean ${possibilities[0]}?"
            else -> possibilities.joinToString(prefix = ". (Possible subcommands: ", postfix = ")")
        }
    }

    /** Message for [NoSuchOption] */
    fun noSuchOption(name: String, possibilities: List<String>): String {
        return "no such option $name" + when (possibilities.size) {
            0 -> ""
            1 -> ". Did you mean ${possibilities[0]}?"
            else -> possibilities.joinToString(prefix = ". (Possible options: ", postfix = ")")
        }
    }

    /**
     * Message for [NoSuchOption] when a subcommand has an option with the same name
     */
    fun noSuchOptionWithSubCommandPossibility(name: String, subcommand: String): String {
        return "no such option $name. hint: $subcommand has an option $name"
    }

    /**
     * Message for [IncorrectOptionValueCount]
     *
     * @param count non-negative count of required values
     */
    fun incorrectOptionValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> "option $name does not take a value"
            1 -> "option $name requires a value"
            else -> "option $name requires $count values"
        }
    }

    /**
     * Message for [IncorrectArgumentValueCount]
     *
     * @param count non-negative count of required values
     */
    fun incorrectArgumentValueCount(name: String, count: Int): String {
        return when (count) {
            0 -> "argument $name does not take a value"
            1 -> "argument $name requires a value"
            else -> "argument $name requires $count values"
        }
    }

    /**
     * Message for [MutuallyExclusiveGroupException]
     *
     * @param others non-empty list of other options in the group
     */
    fun mutexGroupException(name: String, others: List<String>): String {
        return "option $name cannot be used with ${others.joinToString(" or ")}"
    }

    /** Message for [FileNotFound] */
    fun fileNotFound(filename: String) = "$filename not found"

    /** Message for [InvalidFileFormat]*/
    fun invalidFileFormat(filename: String, message: String) =
        "incorrect format in file $filename: $message"

    /** Message for [InvalidFileFormat]*/
    fun invalidFileFormat(filename: String, lineNumber: Int, message: String) =
        "incorrect format in file $filename line $lineNumber: $message"

    /** Error in message for [InvalidFileFormat] */
    fun unclosedQuote() = "unclosed quote"

    /** Error in message for [InvalidFileFormat] */
    fun fileEndsWithSlash() = "file ends with \\"

    /** One extra argument is present */
    fun extraArgumentOne(name: String) = "got unexpected extra argument $name"

    /** More than one extra argument is present */
    fun extraArgumentMany(name: String, count: Int) = "got unexpected extra arguments $name"

    /** Error message when reading flag option from a file */
    fun invalidFlagValueInFile(name: String) = "invalid flag value in file for option $name"

    /** Error message when reading switch option from environment variable */
    fun switchOptionEnvvar() = "environment variables not supported for switch options"

    /** Required [MutuallyExclusiveOptions] was not provided */
    fun requiredMutexOption(options: String) = "must provide one of $options"

    /**
     * [ChoiceGroup] value was invalid
     *
     * @param choices non-empty list of possible choices
     */
    fun invalidGroupChoice(value: String, choices: List<String>): String {
        return "invalid choice: $value. (choose from ${choices.joinToString()})"
    }

    /** Invalid value for a parameter of type [Double] or [Float] */
    fun floatConversionError(value: String) = "$value is not a valid floating point value"

    /** Invalid value for a parameter of type [Int] or [Long] */
    fun intConversionError(value: String) = "$value is not a valid integer"

    /** Invalid value for a parameter of type [Boolean] */
    fun boolConversionError(value: String) = "$value is not a valid boolean"

    /** Invalid value falls outside range */
    fun rangeExceededMax(value: String, limit: String) =
        "$value is larger than the maximum valid value of $limit."

    /** Invalid value falls outside range */
    fun rangeExceededMin(value: String, limit: String) =
        "$value is smaller than the minimum valid value of $limit."

    /** Invalid value falls outside range */
    fun rangeExceededBoth(value: String, min: String, max: String) =
        "$value is not in the valid range of $min to $max."

    /**
     * A counted option was given more times than its limit
     */
    fun countedOptionExceededLimit(count: Int, limit: Int): String =
        "option was given $count times, but only $limit times are allowed"

    /**
     * Invalid value for `choice` parameter
     *
     * @param choices non-empty list of possible choices
     */
    fun invalidChoice(choice: String, choices: List<String>): String {
        return "invalid choice: $choice. (choose from ${choices.joinToString()})"
    }

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    fun pathTypeFile() = "file"

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    fun pathTypeDirectory() = "directory"

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    fun pathTypeOther() = "path"

    /** Invalid path value given */
    fun pathDoesNotExist(pathType: String, path: String) = "$pathType \"$path\" does not exist."

    /** Invalid path value given */
    fun pathIsFile(pathType: String, path: String) = "$pathType \"$path\" is a file."

    /** Invalid path value given */
    fun pathIsDirectory(pathType: String, path: String) = "$pathType \"$path\" is a directory."

    /** Invalid path value given */
    fun pathIsNotWritable(pathType: String, path: String) = "$pathType \"$path\" is not writable."

    /** Invalid path value given */
    fun pathIsNotReadable(pathType: String, path: String) = "$pathType \"$path\" is not readable."

    /** Invalid path value given */
    fun pathIsSymlink(pathType: String, path: String) = "$pathType \"$path\" is a symlink."

    /** Metavar used for options with unspecified value type */
    fun defaultMetavar() = "value"

    /** Metavar used for options that take [String] values */
    fun stringMetavar() = "text"

    /** Metavar used for options that take [Float] or [Double] values */
    fun floatMetavar() = "float"

    /** Metavar used for options that take [Int] or [Long] values */
    fun intMetavar() = "int"

    /** Metavar used for options that take `File` or `Path` values */
    fun pathMetavar() = "path"

    /** Metavar used for options that take `InputStream` or `OutputStream` values */
    fun fileMetavar() = "file"

    /** The title for the usage section of help output */
    fun usageTitle(): String = "Usage:"

    /** The title for the options section of help output */
    fun optionsTitle(): String = "Options"

    /** The title for the arguments section of help output */
    fun argumentsTitle(): String = "Arguments"

    /** The title for the subcommands section of help output */
    fun commandsTitle(): String = "Commands"

    /** The placeholder that indicates where options may be present in the usage help output */
    fun optionsMetavar(): String = "options"

    /** The placeholder that indicates where subcommands may be present in the usage help output */
    fun commandMetavar(): String = "command"

    /** The placeholder that indicates where arguments may be present in the usage help output */
    fun argumentsMetavar(): String = "args"

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.DEFAULT] */
    fun helpTagDefault(): String = "default"

    /** Text rendered for parameters tagged with [HelpFormatter.Tags.REQUIRED] */
    fun helpTagRequired(): String = "required"

    /** The default message for the `--help` option. */
    fun helpOptionMessage(): String = "Show this message and exit"
}

internal val defaultLocalization = object : Localization {}
