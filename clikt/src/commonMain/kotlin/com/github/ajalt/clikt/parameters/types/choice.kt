package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates.Fixed
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

private fun mvar(choices: Iterable<String>): String {
    return choices.joinToString("|", prefix = "[", postfix = "]")
}

private fun errorMessage(context: Context, choice: String, choices: Map<String, *>): String {
    return context.localization.invalidChoice(choice, choices.keys.toList())
}

// arguments

/**
 * Convert the argument based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the argument will accept values as any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * argument().choice(mapOf("foo" to 1, "bar" to 2))
 * ```
 */
fun <T : Any> RawArgument.choice(choices: Map<String, T>, ignoreCase: Boolean = false): ProcessedArgument<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    val c = if (ignoreCase) choices.mapKeys { it.key.lowercase() } else choices
    return convert(completionCandidates = Fixed(choices.keys)) {
        c[if (ignoreCase) it.lowercase() else it] ?: fail(errorMessage(context, it, choices))
    }
}

/**
 * Convert the argument based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the argument will accept values as any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * argument().choice("foo" to 1, "bar" to 2)
 * ```
 */
fun <T : Any> RawArgument.choice(
    vararg choices: Pair<String, T>,
    ignoreCase: Boolean = false,
): ProcessedArgument<T, T> {
    return choice(choices.toMap(), ignoreCase)
}

/**
 * Restrict the argument to a fixed set of values.
 *
 * If [ignoreCase] is `true`, the argument will accept values as any mix of upper and lower case.
 * The argument's final value will always match the case of the corresponding value in [choices].
 *
 * ### Example:
 *
 * ```
 * argument().choice("foo", "bar")
 * ```
 */
fun RawArgument.choice(vararg choices: String, ignoreCase: Boolean = false): ProcessedArgument<String, String> {
    return choice(choices.associateBy { it }, ignoreCase)
}

// options

/**
 * Convert the option based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the option will accept values as any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * option().choice(mapOf("foo" to 1, "bar" to 2))
 * ```
 *
 * @see com.github.ajalt.clikt.parameters.groups.groupChoice
 */
fun <T : Any> RawOption.choice(
    choices: Map<String, T>,
    metavar: String = mvar(choices.keys),
    ignoreCase: Boolean = false,
): NullableOption<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    val c = if (ignoreCase) choices.mapKeys { it.key.lowercase() } else choices
    return convert(metavar, completionCandidates = Fixed(choices.keys)) {
        c[if (ignoreCase) it.lowercase() else it] ?: fail(errorMessage(context, it, choices))
    }
}

/**
 * Convert the option based on a fixed set of values.
 *
 * If [ignoreCase] is `true`, the option will accept values as any mix of upper and lower case.
 *
 * ### Example:
 *
 * ```
 * option().choice("foo" to 1, "bar" to 2)
 * ```
 *
 * @see com.github.ajalt.clikt.parameters.groups.groupChoice
 */
fun <T : Any> RawOption.choice(
    vararg choices: Pair<String, T>,
    metavar: String = mvar(choices.map { it.first }),
    ignoreCase: Boolean = false,
): NullableOption<T, T> {
    return choice(choices.toMap(), metavar, ignoreCase)
}

/**
 * Restrict the option to a fixed set of values.
 *
 * If [ignoreCase] is `true`, the option will accept values as any mix of upper and lower case.
 * The option's final value will always match the case of the corresponding value in [choices].
 *
 * ### Example:
 *
 * ```
 * option().choice("foo", "bar")
 * ```
 */
fun RawOption.choice(
    vararg choices: String,
    metavar: String = mvar(choices.asIterable()),
    ignoreCase: Boolean = false,
): NullableOption<String, String> {
    return choice(choices.associateBy { it }, metavar, ignoreCase)
}
