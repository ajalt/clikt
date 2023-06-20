package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.transform.TransformContext

private val conversion: TransformContext.(String) -> Long =
    { it.toLongOrNull() ?: fail(context.localization.intConversionError(it)) }

/** Convert the argument values to a `Long` */
fun RawArgument.long(): ProcessedArgument<Long, Long> {
    return convert(conversion = conversion)
}

/**
 * Convert the option values to an `Long`
 *
 * @param acceptsValueWithoutName If `true`, this option can be specified like `-2` or `-3` in
 *   addition to `--opt=2` or `-o3`
 */
fun RawOption.long(acceptsValueWithoutName: Boolean = false): NullableOption<Long, Long> {
    return convert({ localization.intMetavar() }, conversion = conversion)
        .copy(acceptsNumberValueWithoutName = acceptsValueWithoutName)
}
