package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameter
import com.github.ajalt.clikt.parameters.*

private fun defaultChoiceMetavar(choices: Map<String, *>): String {
    return choices.keys.joinToString("|", prefix = "[", postfix = "]")
}

private fun <T : Any> convertChoices(value: String, name: String, choices: Map<String, T>): T {
    return choices[value] ?: throw BadParameter(
            "Invalid value for \"$name\" (choose from ${choices.keys.joinToString(", ")})")
}

fun <T : Any> RawArgument.choice(choices: Map<String, T>): ProcessedArgument<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert { convertChoices(it, name, choices) }
}

fun <T : Any> RawArgument.choice(vararg choices: Pair<String, T>): ProcessedArgument<T, T> {
    return choice(mapOf(*choices))
}

fun RawArgument.choice(vararg choices: String): ProcessedArgument<String, String> {
    return choice(choices.associateBy { it })
}

fun <T : Any> RawOption.choice(choices: Map<String, T>,
                               metavar: String = defaultChoiceMetavar(choices)): NullableOption<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert(metavar) { convertChoices(it, name, choices) }
}

fun <T : Any> RawOption.choice(vararg choices: Pair<String, T>,
                               metavar: String? = null): NullableOption<T, T> {
    return mapOf(*choices).let { choice(it, metavar ?: defaultChoiceMetavar(it)) }
}

fun RawOption.choice(vararg choices: String,
                     metavar: String? = null): NullableOption<String, String> {
    return choices.associateBy { it }.let { choice(it, metavar ?: defaultChoiceMetavar(it)) }
}
