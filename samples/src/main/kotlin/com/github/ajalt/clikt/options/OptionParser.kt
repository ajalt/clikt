package com.github.ajalt.clikt.options

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
}


class TypedOptionParser<out T>(private val type: ParamType<T>) : OptionParser {
    override fun parseLongOpt(name: String, argv: Array<String>, index: Int, explicitValue: String?): ParseResult {
        val value = explicitValue ?: argv[index + 1]
        // TODO exceptions
        return ParseResult(if (explicitValue == null) 2 else 1, type.convert(value))
    }

    override fun parseShortOpt(name: String, argv: Array<String>, index: Int, optionIndex: Int): ParseResult {
        val option = argv[index]
        val hasIncludedOption = optionIndex != option.lastIndex
        val value = if (hasIncludedOption) option.substring(optionIndex + 1) else argv[index + 1]
        return ParseResult(if (hasIncludedOption) 1 else 2, type.convert(value))
    }
}

data class ParseResult constructor(val consumedCount: Int, val value: Any?)
