package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.BadOptionUsage

interface OptionParser {
    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain multiple short options.
     * @param optionIndex The index of the option within `argv\[index]`
     * @return A [ParseResult.consumedCount] > 0 if the entire option has been consumed, or 0 if
     *     there are more values in the option
     */
    fun parseShortOpt(name: String, argv: Array<String>, index: Int, optionIndex: Int): ParseResult

    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain an '=' with the first value
     */
    fun parseLongOpt(name: String, argv: Array<String>, index: Int, explicitValue: String?): ParseResult

    /** Return true if this parser should be displayed as repeatable by the help formatter. */
    val repeatableForHelp: Boolean
}


class TypedOptionParser<out T>(private val type: ParamType<T>,
                               private val nargs: Int) : OptionParser {
    init {
        require(nargs >= 1) { "Options cannot have nargs < 1" }
    }

    override val repeatableForHelp: Boolean get() = nargs > 1

    override fun parseLongOpt(name: String, argv: Array<String>, index: Int, explicitValue: String?): ParseResult {
        val hasIncludedValue = explicitValue != null
        val consumedCount = if (hasIncludedValue) nargs else nargs + 1
        val endIndex = index + consumedCount - 1

        if (endIndex > argv.lastIndex) {
            throw BadOptionUsage(if (nargs == 1) {
                "$name option requires an argument"
            } else {
                "$name option requires $nargs arguments"
            })
        }

        return if (nargs > 1) {
            var args = argv.slice((index + 1)..endIndex)
            if (explicitValue != null) args = listOf(explicitValue) + args
            ParseResult(consumedCount, args.map { type.convert(it) })
        } else {
            val value = explicitValue ?: argv[index + 1]
            ParseResult(consumedCount, type.convert(value))
        }
    }

    override fun parseShortOpt(name: String, argv: Array<String>, index: Int, optionIndex: Int): ParseResult {
        val option = argv[index]
        val hasIncludedValue = optionIndex != option.lastIndex
        val explicitValue = if (hasIncludedValue) option.substring(optionIndex + 1) else null
        return parseLongOpt(name, argv, index, explicitValue)
    }
}

data class ParseResult constructor(val consumedCount: Int, val value: Any?)
