package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert


private fun valueToUInt(context: Context, it: String): UInt {
    return it.toUIntOrNull() ?: throw BadParameterValue(context.localization.intConversionError(it))
}

/** Convert the argument values to an `UInt` */
fun RawArgument.uint(): ProcessedArgument<UInt, UInt> = convert { valueToUInt(context, it) }

/**
 * Convert the option values to a `UInt`
 *
 * @param acceptsValueWithoutName If `true`, this option can be specified like `-2` or `-3` in
 *   addition to `--opt=2` or `-o3`
 */
fun RawOption.uint(acceptsValueWithoutName: Boolean = false): NullableOption<UInt, UInt> {
    return convert({ localization.intMetavar() }) { valueToUInt(context, it) }
        .copy(acceptsNumberValueWithoutName = acceptsValueWithoutName)
}
