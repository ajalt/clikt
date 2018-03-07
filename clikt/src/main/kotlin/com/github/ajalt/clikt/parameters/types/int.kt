package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameter
import com.github.ajalt.clikt.parameters.RawArgument
import com.github.ajalt.clikt.parameters.RawOption
import com.github.ajalt.clikt.parameters.convert

private val valueToInt: (String) -> Int = {
    it.toIntOrNull() ?: throw BadParameter("$it is not a valid integer")
}

fun RawArgument.int() = convert(valueToInt)
fun RawOption.int() = convert("INT") { valueToInt(it) }
