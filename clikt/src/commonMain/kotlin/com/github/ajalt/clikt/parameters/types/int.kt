package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

internal fun valueToInt(context: Context, it: String): Int {
    return it.toIntOrNull() ?: throw BadParameterValue(context.localization.intConversionError(it))
}

/** Convert the argument values to an `Int` */
fun RawArgument.int() = convert { valueToInt(context, it) }

/** Convert the option values to an `Int` */
fun RawOption.int() = convert({ localization.intMetavar() }) { valueToInt(context, it) }
