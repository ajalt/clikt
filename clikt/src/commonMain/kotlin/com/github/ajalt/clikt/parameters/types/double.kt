package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.transform.TransformContext

private val conversion: TransformContext.(String) -> Double =
    { it.toDoubleOrNull() ?: fail(context.localization.floatConversionError(it)) }

/** Convert the argument values to a `Double` */
fun RawArgument.double(): ProcessedArgument<Double, Double> = convert(conversion = conversion)

/** Convert the option values to a `Double` */
fun RawOption.double(): OptionWithValues<Double?, Double, Double> {
    return convert({ localization.floatMetavar() }, conversion = conversion)
}
