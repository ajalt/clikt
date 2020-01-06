package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates.Fixed
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

private fun mvar(choices: Iterable<String>): String {
    return choices.joinToString("|", prefix = "[", postfix = "]")
}

private fun errorMessage(choice: String, choices: Map<String, *>): String {
    return "invalid choice: $choice. (choose from ${choices.keys.joinToString(", ")})"
}

// arguments

/**
 * Convert the argument based on a fixed set of values.
 *
 * ### Example:
 *
 * ```kotlin
 * argument().choice(mapOf("foo" to 1, "bar" to 2))
 * ```
 */
fun <T : Any> RawArgument.choice(choices: Map<String, T>): ProcessedArgument<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert(completionCandidates = Fixed(choices.keys)) {
        choices[it] ?: fail(errorMessage(it, choices))
    }
}

/**
 * Convert the argument based on a fixed set of values.
 *
 * ### Example:
 *
 * ```kotlin
 * argument().choice("foo" to 1, "bar" to 2)
 * ```
 */
fun <T : Any> RawArgument.choice(vararg choices: Pair<String, T>): ProcessedArgument<T, T> {
    return choice(choices.toMap())
}

/**
 * Restrict the argument to a fixed set of values.
 *
 * ### Example:
 *
 * ```kotlin
 * argument().choice("foo", "bar")
 * ```
 */
fun RawArgument.choice(vararg choices: String): ProcessedArgument<String, String> {
    return choice(choices.associateBy { it })
}

/**
 * Convert the argument to the values of an enum.
 *
 * ### Example:
 *
 * ```kotlin
 * enum class Size { SMALL, LARGE }
 * argument().enum<Size>()
 * ```
 *
 * @param key A block that returns the command line value to use for an enum value. The default is
 *   the enum name.
 */
inline fun <reified T : Enum<T>> RawArgument.enum(key: (T) -> String = { it.name }): ProcessedArgument<T, T> {
    return choice(enumValues<T>().associateBy { key(it) })
}

// options

/**
 * Convert the option based on a fixed set of values.
 *
 * ### Example:
 *
 * ```kotlin
 * option().choice(mapOf("foo" to 1, "bar" to 2))
 * ```
 *
 * @see com.github.ajalt.clikt.parameters.groups.groupChoice
 */
fun <T : Any> RawOption.choice(choices: Map<String, T>,
                               metavar: String = mvar(choices.keys)): NullableOption<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert(metavar, completionCandidates = Fixed(choices.keys)) {
        choices[it] ?: fail(errorMessage(it, choices))
    }
}

/**
 * Convert the option based on a fixed set of values.
 *
 * ### Example:
 *
 * ```kotlin
 * option().choice("foo" to 1, "bar" to 2)
 * ```
 *
 * @see com.github.ajalt.clikt.parameters.groups.groupChoice
 */
fun <T : Any> RawOption.choice(vararg choices: Pair<String, T>,
                               metavar: String = mvar(choices.map { it.first })): NullableOption<T, T> {
    return choice(choices.toMap(), metavar)
}

/**
 * Restrict the option to a fixed set of values.
 *
 * ### Example:
 *
 * ```kotlin
 * option().choice("foo", "bar")
 * ```
 */
fun RawOption.choice(vararg choices: String,
                     metavar: String = mvar(choices.asIterable())): NullableOption<String, String> {
    return choice(choices.associateBy { it }, metavar)
}

/**
 * Convert the option to the values of an enum.
 *
 * ### Example:
 *
 * ```kotlin
 * enum class Size { SMALL, LARGE }
 * option().enum<Size>()
 * ```
 *
 * @param key A block that returns the command line value to use for an enum value. The default is
 *   the enum name.
 */
inline fun <reified T : Enum<T>> RawOption.enum(key: (T) -> String = { it.name }): NullableOption<T, T> {
    return choice(enumValues<T>().associateBy { key(it) })
}
