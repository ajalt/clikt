package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.parameters.options.Option

interface OptionParser {
    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain multiple short parameters.
     * @param optionIndex The index of the option within `argv\[index]`
     */
    fun parseShortOpt(option: Option, name: String, argv: Array<String>, index: Int, optionIndex: Int): ParseResult

    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain an '=' with the first value
     */
    fun parseLongOpt(option: Option, name: String, argv: Array<String>, index: Int, explicitValue: String?): ParseResult

    // TODO docs, note about purity
    data class Invocation(val name: String, val values: List<String>)

    /**
     * @param consumedCount The number of items in argv that were consumed. This number must be >= 1 if the
     *   entire option was consumed, or 0 if there are other options in the same index (e.g. flag options)
     * @param invocation The data from this invocation.
     */
    data class ParseResult(val consumedCount: Int, val invocation: Invocation) {
        constructor(consumedCount: Int, name: String, values: List<String>)
                : this(consumedCount, Invocation(name, values))
    }
}
