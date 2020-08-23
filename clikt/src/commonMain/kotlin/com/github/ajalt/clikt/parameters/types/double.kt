package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

private fun valueToDouble(context: Context, it: String): Double {
    return it.toDoubleOrNull() ?: throw BadParameterValue(context.localization.floatConversionError(it))
}

/** Convert the argument values to a `Double` */
fun RawArgument.double() = convert { valueToDouble( context, it) }

/** Convert the option values to a `Double` */
fun RawOption.double() = convert({ localization.floatMetavar() }) { valueToDouble(context, it) }
