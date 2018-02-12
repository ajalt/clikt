package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.BadOptionUsage

interface OptionParser2<out T> {
    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain multiple short options.
     * @param optionIndex The index of the option within `argv\[index]`
     * @return An int > 0 if the entire option has been consumed, or 0 if there are more values in the option
     */ // TODO docs about side effect of setting value
    fun parseShortOpt(name: String, argv: Array<String>, index: Int, optionIndex: Int): Int

    /**
     * @param name The name of the flag used to invoke this option
     * @param argv The entire list of command line arguments for the command
     * @param index The index of the option flag in [argv], which may contain an '=' with the first value
     */
    fun parseLongOpt(name: String, argv: Array<String>, index: Int, explicitValue: String?): Int

    /** Return true if this parser should be displayed as repeatable by the help formatter. */
    val repeatableForHelp: Boolean

    val rawValues: List<T> // TODO docs
}


class OptionWithValuesParser2(private val nargs: Int) : OptionParser2<List<String>> {
    init {
        require(nargs >= 1) { "Options cannot have nargs < 1" }
    }

    private val _rawValues = mutableListOf<List<String>>()
    override val rawValues: List<List<String>> get() = _rawValues // TODO: can parsers be pure?

    override val repeatableForHelp: Boolean get() = nargs > 1

    override fun parseLongOpt(name: String, argv: Array<String>, index: Int, explicitValue: String?): Int {
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

        _rawValues += if (nargs > 1) {
            var args = argv.slice((index + 1)..endIndex)
            if (explicitValue != null) args = listOf(explicitValue) + args
            args

        } else {
            listOf(explicitValue ?: argv[index + 1])
        }
        return consumedCount
    }

    override fun parseShortOpt(name: String, argv: Array<String>, index: Int, optionIndex: Int): Int {
        val option = argv[index]
        val hasIncludedValue = optionIndex != option.lastIndex
        val explicitValue = if (hasIncludedValue) option.substring(optionIndex + 1) else null
        return parseLongOpt(name, argv, index, explicitValue)
    }
}


