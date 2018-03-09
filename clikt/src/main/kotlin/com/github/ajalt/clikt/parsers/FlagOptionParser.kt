package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.BadOptionUsage
import com.github.ajalt.clikt.parameters.options.Option


class FlagOptionParser : OptionParser {
    private val _rawValues = mutableListOf<String>()
    /** The option names for each invocation of the flag. */
    val rawValues: List<String> get() = _rawValues

    override fun repeatableForHelp(option: Option) = false

    override fun parseLongOpt(option: Option, name: String, argv: Array<String>, index: Int, explicitValue: String?): Int {
        if (explicitValue != null) throw BadOptionUsage("$name option does not take a value")
        _rawValues += name
        return 1
    }

    override fun parseShortOpt(option: Option, name: String, argv: Array<String>, index: Int, optionIndex: Int): Int {
        _rawValues += name
        return if (optionIndex == argv[index].lastIndex) 1 else 0
    }
}
