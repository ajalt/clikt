package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.BadParameter

// TODO Combine these interfaces?
interface ShortOptParser {
    /**
     * @param index The index of the option flag in argv, which may contain multiple short options.
     * @param optionIndex The index of the option within argv[index]
     * @return A [ParseResult.consumedCount] > 0 if the entire option has been consumed, or 0 if
     *     there are more values in the option
     */
    fun parseShortOpt(argv: Array<String>, index: Int, optionIndex: Int): ParseResult
}

interface LongOptParser {
    /**
     * @param index The index of the option flag in argv, which may contain an '=' with the first value
     */
    fun parseLongOpt(argv: Array<String>, index: Int, explicitValue: String?): ParseResult
}


abstract class OptionParser<out T>(private val commandArgIndex: Int) : LongOptParser, ShortOptParser {
    override fun parseLongOpt(argv: Array<String>, index: Int, explicitValue: String?): ParseResult {
        val value = explicitValue ?: argv[index + 1]
        // TODO exceptions
        return ParseResult(if (explicitValue == null) 2 else 1, convertValue(value), commandArgIndex)
    }

    override fun parseShortOpt(argv: Array<String>, index: Int, optionIndex: Int): ParseResult {
        val option = argv[index]
        val hasIncludedOption = optionIndex != option.lastIndex
        val value = if (hasIncludedOption) option.substring(optionIndex + 1)
        else argv[index + 1]
        return ParseResult(if (hasIncludedOption) 1 else 2, convertValue(value), commandArgIndex)
    }

    abstract fun convertValue(value: String): T

    // TODO: add param name
    protected fun fail(message: String): Nothing {
        throw BadParameter(message)
    }
}

data class ParseResult(val consumedCount: Int, val valuesByCommandArgIndex: Map<Int, *>) {
    companion object {
        val EMPTY = ParseResult(0, emptyMap<Int, Any?>())
    }

    constructor(consumedCount: Int, value: Any?, commandArgIndex: Int) :
            this(consumedCount, mapOf(commandArgIndex to value))

    operator fun plus(other: ParseResult) = ParseResult(consumedCount + other.consumedCount,
            valuesByCommandArgIndex + other.valuesByCommandArgIndex)
}
