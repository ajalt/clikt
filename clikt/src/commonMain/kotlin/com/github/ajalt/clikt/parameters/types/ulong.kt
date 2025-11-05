package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.transform.TransformContext


private val conversion: TransformContext.(String) -> ULong =
    { it.toULongOrNull() ?: fail(context.localization.uintConversionError(it)) }

/** Convert the argument values to a `ULong` */
fun RawArgument.ulong(): ProcessedArgument<ULong, ULong> = convert(conversion = conversion)

/**
 * Convert the option values to a `ULong`
 *
 * @param acceptsValueWithoutName If `true`, this option can be specified like `-2` or `-3` in
 *   addition to `--opt=2` or `-o3`
 */
fun RawOption.ulong(acceptsValueWithoutName: Boolean = false): NullableOption<ULong, ULong> {
    return convert({ localization.intMetavar() }, conversion = conversion)
        .copy(acceptsNumberValueWithoutName = acceptsValueWithoutName)
}
