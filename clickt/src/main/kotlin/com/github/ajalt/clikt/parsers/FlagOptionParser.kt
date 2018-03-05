package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.parameters.Option
import com.github.ajalt.clikt.core.BadOptionUsage


class FlagOptionParser : OptionParser {
    private val _rawValues = mutableListOf<Boolean>()
    val rawValues: List<Boolean> get() = _rawValues
    private fun offNames(option: Option): List<String> = option.names.mapNotNull {
        if ("/" in it) {
            val split = it.split("/", limit = 2)
            split[1].let { if (it.isBlank()) null else it }
        } else null
    }

    override fun repeatableForHelp(option: Option) = false

    override fun parseLongOpt(option: Option, name: String, argv: Array<String>, index: Int, explicitValue: String?): Int {
        if (explicitValue != null) throw BadOptionUsage("$name option does not take a value")
        _rawValues += name !in offNames(option)
        return 1
    }

    override fun parseShortOpt(option: Option, name: String, argv: Array<String>, index: Int, optionIndex: Int): Int {
        _rawValues += name !in offNames(option)
        return if (optionIndex == argv[index].lastIndex) 1 else 0
    }
}
