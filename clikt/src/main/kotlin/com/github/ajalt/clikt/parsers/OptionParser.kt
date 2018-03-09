package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BadOptionUsage
import com.github.ajalt.clikt.parameters.options.Option

interface OptionParser {
    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain multiple short parameters.
     * @param optionIndex The index of the option within `argv\[index]`
     * @return An int > 0 if the entire option has been consumed, or 0 if there are more values in the option
     */ // TODO docs about side effect of setting option.rawValues
    fun parseShortOpt(option: Option, name: String, argv: Array<String>, index: Int, optionIndex: Int): Int

    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain an '=' with the first value
     */
    fun parseLongOpt(option: Option, name: String, argv: Array<String>, index: Int, explicitValue: String?): Int

    /** Return true if this parser should be displayed as repeatable by the output formatter. */
    fun repeatableForHelp(option: Option): Boolean
}


class OptionWithValuesParser : OptionParser {
    data class Invocation(val name: String, val values: List<String>)

    private val _rawValues = mutableListOf<Invocation>()
    val rawValues: List<Invocation> get() = _rawValues // TODO: can parsers be pure?
    override fun repeatableForHelp(option: Option) = option.nargs > 1

    override fun parseLongOpt(option: Option, name: String, argv: Array<String>, index: Int, explicitValue: String?): Int {
        require(option.nargs > 0) {
            "This parser can only be used with a fixed number of arguments. Try the flag parser instead."
        }
        val hasIncludedValue = explicitValue != null
        val consumedCount = if (hasIncludedValue) option.nargs else option.nargs + 1
        val endIndex = index + consumedCount - 1

        if (endIndex > argv.lastIndex) {
            throw BadOptionUsage(if (option.nargs == 1) {
                "$name option requires an argument"
            } else {
                "$name option requires ${option.nargs} arguments"
            })
        }

        _rawValues += if (option.nargs > 1) {
            var args = argv.slice((index + 1)..endIndex)
            if (explicitValue != null) args = listOf(explicitValue) + args
            Invocation(name, args)
        } else {
            Invocation(name, listOf(explicitValue ?: argv[index + 1]))
        }
        return consumedCount
    }

    override fun parseShortOpt(option: Option, name: String, argv: Array<String>, index: Int, optionIndex: Int): Int {
        val opt = argv[index]
        val hasIncludedValue = optionIndex != opt.lastIndex
        val explicitValue = if (hasIncludedValue) opt.substring(optionIndex + 1) else null
        return parseLongOpt(option, name, argv, index, explicitValue)
    }
}


