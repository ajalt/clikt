package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

private fun valueToFloat(context: Context, it: String): Float {
    return it.toFloatOrNull() ?: throw BadParameterValue(context.localization.floatConversionError(it))
}

/** Convert the argument values to a `Float` */
fun RawArgument.float() = convert { valueToFloat(context, it) }

/** Convert the option values to a `Float` */
fun RawOption.float() = convert({ localization.floatMetavar() }) { valueToFloat(context, it) }
