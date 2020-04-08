package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths

fun RawOption.outputStream(): NullableOption<OutputStream, OutputStream> {
    return convert("FILE", completionCandidates = CompletionCandidates.Path) {
        if (it == "-") {
            System.out
        } else {
            Files.newOutputStream(Paths.get(it))
        }
    }
}

fun NullableOption<OutputStream, OutputStream>.defaultStdout(): OptionWithValues<OutputStream, OutputStream, OutputStream> {
    return default(System.out, "-")
}
