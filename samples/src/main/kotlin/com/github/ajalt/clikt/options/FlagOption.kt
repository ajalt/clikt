package com.github.ajalt.clikt.options

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class FlagOption(vararg val names: String, val help: String= "")

class FlagOptionParser(private val commandArgIndex: Int) : OptionParser {
    override fun parseLongOpt(argv: Array<String>, index: Int, explicitValue: String?): ParseResult {
        require(explicitValue == null) // TODO
        return ParseResult(1, true, commandArgIndex)
    }

    override fun parseShortOpt(argv: Array<String>, index: Int, optionIndex: Int): ParseResult {
        return ParseResult(if (optionIndex == argv[index].lastIndex) 1 else 0, true, commandArgIndex)
    }
}
