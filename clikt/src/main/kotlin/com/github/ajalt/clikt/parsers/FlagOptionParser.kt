package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.IncorrectOptionValueCount
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parsers.OptionParser.ParseResult


/** A parser for options that take no values. */
object FlagOptionParser : OptionParser {
    override fun parseLongOpt(option: Option, name: String, argv: List<String>,
                              index: Int, explicitValue: String?): ParseResult {
        if (explicitValue != null) throw IncorrectOptionValueCount(option, name)
        return ParseResult(1, name, emptyList())
    }

    override fun parseShortOpt(option: Option, name: String, argv: List<String>,
                               index: Int, optionIndex: Int): ParseResult {
        val consumed = if (optionIndex == argv[index].lastIndex) 1 else 0
        return ParseResult(consumed, name, emptyList())
    }
}
