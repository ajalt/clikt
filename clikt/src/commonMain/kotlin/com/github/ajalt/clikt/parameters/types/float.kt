package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.transform.TransformContext

private val conversion: TransformContext.(String) -> Float =
    { it.toFloatOrNull() ?: fail(context.localization.floatConversionError(it)) }

/** Convert the argument values to a `Float` */
fun RawArgument.float(): ProcessedArgument<Float, Float> = convert(conversion = conversion)

/** Convert the option values to a `Float` */
fun RawOption.float(): OptionWithValues<Float?, Float, Float> =
    convert({ localization.floatMetavar() }, conversion = conversion)
