package com.github.ajalt.clikt.options

object StringParamType : ParamType<String> {
    override fun convert(value: String) = value
}
