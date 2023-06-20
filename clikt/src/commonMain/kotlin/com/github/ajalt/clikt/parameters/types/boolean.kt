package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.transform.TransformContext

private fun TransformContext.valueToBool(value: String): Boolean {
    return when (value.lowercase()) {
        "true", "t", "1", "yes", "y", "on" -> true
        "false", "f", "0", "no", "n", "off" -> false
        else -> fail(context.localization.boolConversionError(value))
    }
}

/**
 * Convert the argument values to `Boolean`.
 *
 * ## Conversion
 *
 * Conversion is case-insensitive.
 *
 * - `true`: "true", "t", "1", "yes", "y", "on"
 * - `false`: "false", "f", "0", "no", "n", "off"
 *
 * All other values are an error.
 */
fun RawArgument.boolean(): ProcessedArgument<Boolean, Boolean> = convert { valueToBool(it) }

/**
 * Convert the option values to `Boolean`
 *
 * In most cases, you should use [flag] instead of this function, but this allows you to have
 * tri-state delegates of type `Boolean?`.
 *
 * ## Conversion
 *
 * Conversion is case-insensitive.
 *
 * - `true`: "true", "t", "1", "yes", "y", "on"
 * - `false`: "false", "f", "0", "no", "n", "off"
 *
 * All other values are an error.
 */
fun RawOption.boolean(): NullableOption<Boolean, Boolean> =
    convert("[true|false]") { valueToBool(it) }
