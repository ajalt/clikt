package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.BadOptionUsage

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FlagOption(vararg val names: String, val help: String= "")

class FlagOptionParser : OptionParser {
    override fun parseLongOpt(name: String,argv: Array<String>, index: Int, explicitValue: String?): ParseResult {
        if (explicitValue != null) throw BadOptionUsage("$name option does not take a value")
        return ParseResult(1, true)
    }

    override fun parseShortOpt(name: String,argv: Array<String>, index: Int, optionIndex: Int): ParseResult {
        return ParseResult(if (optionIndex == argv[index].lastIndex) 1 else 0, true)
    }
}
