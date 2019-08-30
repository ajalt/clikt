package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert

// this function needs to be accessible, because `enum()` needs to be inlined for the reified type.
// see also https://stackoverflow.com/a/41905907
@PublishedApi
internal fun <T> completions(values: Array<T>) =
        CompletionCandidates.Fixed(values.map { it.toString() }.toSet())

// this function needs to be accessible, because `enum()` needs to be inlined for the reified type.
// see also https://stackoverflow.com/a/41905907
@PublishedApi
internal inline fun <reified T : Enum<T>> valueToEnum(value: String): T {
    return try {
        enumValueOf(value.toUpperCase())
    } catch (ex: IllegalArgumentException) {
        // workaround to avoid T::class.simpleName which would require kotlin-reflections.jar
        throw BadParameterValue("Unknown enum constant ${T::class.java.simpleName}.$value")
    }
}

inline fun <reified T : Enum<T>> RawArgument.enum() =
        convert<T>(completionCandidates = completions<T>(enumValues())) { valueToEnum(it) }

inline fun <reified T : Enum<T>> RawOption.enum() =
        convert<T>(completionCandidates = completions<T>(enumValues())) { valueToEnum(it) }

