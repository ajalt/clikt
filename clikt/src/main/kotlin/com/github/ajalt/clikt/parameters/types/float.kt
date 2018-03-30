package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

private fun valueToFloat(it: String): Float {
    return it.toFloatOrNull() ?: throw BadParameterValue("$it is not a valid floating point value")
}

/** Convert the argument values to a `Float` */
fun RawArgument.float() = convert { valueToFloat(it) }

/** Convert the option values to a `Float` */
fun RawOption.float() = convert("FLOAT") { valueToFloat(it) }
