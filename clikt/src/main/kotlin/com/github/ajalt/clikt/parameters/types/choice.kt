package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.ProcessedArgument
import com.github.ajalt.clikt.parameters.RawArgument
import com.github.ajalt.clikt.parameters.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

private fun mvar(choices: Iterable<String>): String {
    return choices.joinToString("|", prefix = "[", postfix = "]")
}

private fun errorMessage(choice: String, choices: Map<String, *>): String {
    return "invalid choice: $choice. (choose from ${choices.keys.joinToString(", ")})"
}

fun <T : Any> RawArgument.choice(choices: Map<String, T>): ProcessedArgument<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert { choices[it] ?: fail(errorMessage(it, choices)) }
}

fun <T : Any> RawArgument.choice(vararg choices: Pair<String, T>): ProcessedArgument<T, T> {
    return choice(mapOf(*choices))
}

fun RawArgument.choice(vararg choices: String): ProcessedArgument<String, String> {
    return choice(choices.associateBy { it })
}

fun <T : Any> RawOption.choice(choices: Map<String, T>,
                               metavar: String = mvar(choices.keys)): NullableOption<T, T> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return convert(metavar) { choices[it] ?: fail(errorMessage(it, choices)) }
}

fun <T : Any> RawOption.choice(vararg choices: Pair<String, T>,
                               metavar: String = mvar(choices.map { it.first })): NullableOption<T, T> {
    return choice(mapOf(*choices), metavar)
}

fun RawOption.choice(vararg choices: String,
                     metavar: String = mvar(choices.asIterable())): NullableOption<String, String> {
    return choice(choices.associateBy { it }, metavar)
}
