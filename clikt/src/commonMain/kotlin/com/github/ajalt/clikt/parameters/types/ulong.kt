package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert


private fun valueToULong(context: Context, it: String): ULong {
    return it.toULongOrNull() ?: throw BadParameterValue(context.localization.intConversionError(it))
}

/** Convert the argument values to a `ULong` */
fun RawArgument.ulong(): ProcessedArgument<ULong, ULong> = convert { valueToULong(context, it) }

/**
 * Convert the option values to a `ULong`
 *
 * @param acceptsValueWithoutName If `true`, this option can be specified like `-2` or `-3` in
 *   addition to `--opt=2` or `-o3`
 */
fun RawOption.ulong(acceptsValueWithoutName: Boolean = false): NullableOption<ULong, ULong> {
    return convert({ localization.intMetavar() }) { valueToULong(context, it) }
        .copy(acceptsNumberValueWithoutName = acceptsValueWithoutName)
}
