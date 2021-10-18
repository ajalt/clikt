package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption

/**
 * Convert the argument to the values of an enum.
 *
 * If [ignoreCase] is `false`, the argument will only accept values that match the case of the enum values.
 *
 * ### Example:
 *
 * ```
 * enum class Size { SMALL, LARGE }
 * argument().enum<Size>()
 * ```
 *
 * @param key A block that returns the command line value to use for an enum value. The default is
 *   the enum name.
 */
inline fun <reified T : Enum<T>> RawArgument.enum(
    ignoreCase: Boolean = true,
    key: (T) -> String = { it.name },
): ProcessedArgument<T, T> {
    return choice(enumValues<T>().associateBy { key(it) }, ignoreCase)
}

/**
 * Convert the option to the values of an enum.
 *
 * If [ignoreCase] is `false`, the option will only accept values that match the case of the enum values.
 *
 * ### Example:
 *
 * ```
 * enum class Size { SMALL, LARGE }
 * option().enum<Size>()
 * ```
 *
 * @param key A block that returns the command line value to use for an enum value. The default is
 *   the enum name.
 */
inline fun <reified T : Enum<T>> RawOption.enum(
    ignoreCase: Boolean = true,
    key: (T) -> String = { it.name },
): NullableOption<T, T> {
    return choice(enumValues<T>().associateBy { key(it) }, ignoreCase = ignoreCase)
}
