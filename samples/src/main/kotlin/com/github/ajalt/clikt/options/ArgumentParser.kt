package com.github.ajalt.clikt.options

interface ArgumentParser {
    val name: String
    val nargs: Int
    val required: Boolean
    fun parse(args: List<String>): Any?
}

