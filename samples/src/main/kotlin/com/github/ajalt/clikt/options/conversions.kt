package com.github.ajalt.clikt.options

interface ParamType<out T> {
    fun convert(value: String): T
}

