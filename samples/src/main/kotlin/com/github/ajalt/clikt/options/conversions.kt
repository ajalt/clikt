package com.github.ajalt.clikt.options

interface ParamType<out T> {
    val metavar: String // TODO use or remove this
    fun convert(value: String): T
}

