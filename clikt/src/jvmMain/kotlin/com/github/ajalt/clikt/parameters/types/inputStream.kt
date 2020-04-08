package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

fun RawOption.inputStream(): NullableOption<InputStream, InputStream> {
    return convert("FILE", completionCandidates = CompletionCandidates.Path) {
        if (it == "-") {
            System.`in`
        } else {
            Files.newInputStream(Paths.get(it))
        }
    }
}

fun NullableOption<InputStream, InputStream>.defaultStdin(): OptionWithValues<InputStream, InputStream, InputStream> {
    return default(System.`in`, "-")
}
