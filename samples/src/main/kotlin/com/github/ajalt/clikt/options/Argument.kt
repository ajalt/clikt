package com.github.ajalt.clikt.options

interface ArgumentParser {
    val name: String
    val nargs: Int
    val required: Boolean
    fun parse(args: List<String>): ParseResult
}

class TypedArgumentParser<T>(override val name: String,
                             override val nargs: Int,
                             override val required: Boolean,
                             private val commandArgIndex: Int,
                             private val type: ParamType<T>) : ArgumentParser {
    init {
        require(nargs != 0)
        require(commandArgIndex >= 0)
    }

    override fun parse(args: List<String>): ParseResult {
        if (nargs == 1 && !required && args.isEmpty()) return ParseResult.EMPTY // TODO is there a better way to do this?
        val value: Any? = if (nargs == 1) type.convert(args[0]) else args.map { type.convert(it) }
        return ParseResult(0, value, commandArgIndex)
    }
}
