package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import kotlin.jvm.JvmOverloads

private fun valueToFloat(it: String): Float {
    return it.toFloatOrNull() ?: throw BadParameterValue("$it is not a valid floating point value")
}

/** Convert the argument values to a `Float` */
fun RawArgument.float() = convert { valueToFloat(it) }

/** Convert the option values to a `Float` */
@JvmOverloads
fun RawOption.float(metavar: String = "FLOAT") = convert(metavar) { valueToFloat(it) }
