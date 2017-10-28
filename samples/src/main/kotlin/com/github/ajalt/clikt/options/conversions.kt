package com.github.ajalt.clikt.options

interface ParamType<out T> {
    val metavar: String
    fun convert(value: String): T
}

