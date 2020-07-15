package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import kotlin.jvm.JvmOverloads

internal fun valueToLong(it: String): Long {
    return it.toLongOrNull() ?: throw BadParameterValue("$it is not a valid integer")
}

/** Convert the argument values to a `Long` */
fun RawArgument.long() = convert { valueToLong(it) }

/** Convert the option values to a `Long` */
@JvmOverloads
fun RawOption.long(metavar: String = "INT") = convert(metavar) { valueToLong(it) }
