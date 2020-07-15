package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import kotlin.jvm.JvmOverloads

private fun valueToDouble(it: String): Double {
    return it.toDoubleOrNull() ?: throw BadParameterValue("$it is not a valid floating point value")
}

/** Convert the argument values to a `Double` */
fun RawArgument.double() = convert { valueToDouble(it) }

/** Convert the option values to a `Double` */
@JvmOverloads
fun RawOption.double(metavar: String = "FLOAT") = convert(metavar) { valueToDouble(it) }
