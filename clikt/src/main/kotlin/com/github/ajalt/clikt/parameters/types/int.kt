package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameter
import com.github.ajalt.clikt.parameters.RawArgument
import com.github.ajalt.clikt.parameters.RawOption
import com.github.ajalt.clikt.parameters.convert

private fun valueToInt(it: String): Int {
    return it.toIntOrNull() ?: throw BadParameter("$it is not a valid integer")
}

fun RawArgument.int() = convert { valueToInt(it) }
fun RawOption.int() = convert("INT") { valueToInt(it) }
