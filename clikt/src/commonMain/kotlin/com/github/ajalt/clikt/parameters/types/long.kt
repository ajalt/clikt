package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

internal fun valueToLong(context: Context, it: String): Long {
    return it.toLongOrNull() ?: throw BadParameterValue(context.localization.intConversionError(it))
}

/** Convert the argument values to a `Long` */
fun RawArgument.long() = convert { valueToLong(context, it) }

/** Convert the option values to a `Long` */
fun RawOption.long() = convert({ localization.intMetavar() }) { valueToLong(context, it) }
