package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.groups.ChoiceGroup
import com.github.ajalt.clikt.parameters.groups.MutuallyExclusiveOptions

/**
 * Strings to use for help output and error messages
 */
interface Localization {
    /** [Abort] was thrown */
    fun aborted() = "Aborted!"

    /** Prefix for any [UsageError] */
    fun usageError(message: String) = "Error: $message"

    /** Message for [BadParameterValue] */
    fun badParameter() = "Invalid value"

    /** Message for [BadParameterValue] */
    fun badParameterWithMessage(message: String) = "Invalid value: $message"

    /** Message for [BadParameterValue] */
    fun badParameterWithParam(paramName: String) = "Invalid value for \"$paramName\""

    /** Message for [BadParameterValue] */
    fun badParameterWithMessageAndParam(paramName: String, message: String) = "Invalid value for \"$paramName\": $message"

    /** Message for [MissingOption] */
    fun missingOption(paramName: String) = "Missing option \"$paramName\""

    /** Message for [MissingArgument] */
    fun missingArgument(paramName: String) = "Missing argument \"$paramName\""

    /** Message for [NoSuchSubcommand] */
    fun noSuchSubcommand(name: String) = "no such subcommand: \"$name\""

    /** Message for [NoSuchSubcommand] */
    fun noSuchSubcommand(name: String, suggestion: String) = "no such subcommand: \"$name\". $suggestion"

    /** Message for [NoSuchOption] */
    fun noSuchOption(name: String) = "no such option: \"$name\""

    /** Message for [NoSuchOption] */
    fun noSuchOption(name: String, suggestion: String) = "no such option: \"$name\". $suggestion"

    /** A single suggestion for [noSuchOption] and [noSuchSubcommand]*/
    fun didYouMean(possibility: String) = "Did you mean \"$possibility\"?"

    /** Multiple suggestions for [noSuchOption] are joined with this prefix */
    fun possibleOptionsPrefix() = "(Possible options: "

    /** Multiple suggestions for [noSuchSubcommand] are joined with this prefix */
    fun possibleSubcommandsPrefix() = "(Possible subcommands: "

    /** Multiple suggestions for [noSuchOption] and [noSuchSubcommand] are join with this postfix */
    fun possibleParameterPostfix() = ")"

    /** Message for [IncorrectOptionValueCount] when the option does not take a value*/
    fun incorrectOptionValueCountZero(name: String) = "$name option does not take a value"

    /** Message for [IncorrectOptionValueCount] when the option requires one value */
    fun incorrectOptionValueCountOne(name: String) = "$name option requires a value"

    /** Message for [IncorrectOptionValueCount] when the option requires more than one value */
    fun incorrectOptionValueCountMany(name: String, count: Int) = "$name option requires $count values"

    /** Message for [IncorrectArgumentValueCount] */
    fun incorrectArgumentValueCount(name: String, count: Int) = "argument $name takes $count values"

    /** Message for [MutuallyExclusiveGroupException] */
    fun mutexGroupException(name: String, others: String) = "option $name cannot be used with $others"

    /** Separator used to join option names for [mutexGroupException]*/
    fun mutexGroupExceptionNameSeparator() = " or "

    /** Message for [FileNotFound] */
    fun fileNotFound(filename: String) = "$filename not found"

    /** Message for [InvalidFileFormat]*/
    fun invalidFileFormat(filename: String, message: String) = "incorrect format in file $filename: $message"

    /** Message for [InvalidFileFormat]*/
    fun invalidFileFormat(filename: String, lineNumber: Int, message: String) = "incorrect format in file $filename line $lineNumber: $message"

    /** Error in message for [InvalidFileFormat] */
    fun unclosedQuote() = "unclosed quote"

    /** Error in message for [InvalidFileFormat] */
    fun fileEndsWithSlash() = "file ends with \\"

    /** One extra argument is present */
    fun extraArgumentOne(name: String) = "Got unexpected extra argument $name"

    /** More than one extra argument is present */
    fun extraArgumentMany(name: String, count: Int) = "Got unexpected extra arguments $name"

    /** Error message when reading flag option from a file */
    fun invalidFlagValueInFile(name: String) = "Invalid flag value in file for option $name"

    /** Error message when reading switch option from environment variable */
    fun switchOptionEnvvar() = "environment variables not supported for switch options"

    /** Required [MutuallyExclusiveOptions] was not provided */
    fun requiredMutexOption(options: String) = "Must provide one of $options"

    /** [ChoiceGroup] value was invalid */
    fun invalidGroupChoice(value: String, choices: String) = "invalid choice: $value. (choose from $choices)"

    /** Invalid value for a parameter of type [Double] or [Float] */
    fun floatConversionError(value: String) = "$value is not a valid floating point value"

    /** Invalid value for a parameter of type [Int] or [Long] */
    fun intConversionError(value: String) = "$value is not a valid integer"

    /** Invalid value for a parameter of type [Boolean] */
    fun boolConversionError(value: String) = "$value is not a valid boolean"

    /** Invalid value falls outside range */
    fun rangeExceededMax(value: String, limit: String) = "$value is larger than the maximum valid value of $limit."

    /** Invalid value falls outside range */
    fun rangeExceededMin(value: String, limit: String) = "$value is smaller than the minimum valid value of $limit."

    /** Invalid value falls outside range */
    fun rangeExceededBoth(value: String, min: String, max: String) = "$value is not in the valid range of $min to $max."

    /** Invalid value for `choice` parameter */
    fun invalidChoice(choice: String, choices: String) = "invalid choice: $choice. (choose from $choices)"

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    fun pathTypeFile() = "File"

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    fun pathTypeDirectory() = "Directory"

    /** The `pathType` parameter to [pathDoesNotExist] and other `path*` errors */
    fun pathTypeOther() = "Path"

    /** Invalid path type */
    fun pathDoesNotExist(pathType: String, path: String) = "$pathType \"$path\" does not exist."

    /** Invalid path type */
    fun pathIsFile(pathType: String, path: String) = "$pathType \"$path\" is a file."

    /** Invalid path type */
    fun pathIsDirectory(pathType: String, path: String) = "$pathType \"$path\" is a directory."

    /** Invalid path type */
    fun pathIsNotWritable(pathType: String, path: String) = "$pathType \"$path\" is not writable."

    /** Invalid path type */
    fun pathIsNotReadable(pathType: String, path: String) = "$pathType \"$path\" is not readable."

    /** Invalid path type */
    fun pathIsSymlink(pathType: String, path: String) = "$pathType \"$path\" is a symlink."

    /** String used to join multiple items into a list */
    fun listSeparator() = ", "
}

internal val defaultLocalization = object : Localization {}
