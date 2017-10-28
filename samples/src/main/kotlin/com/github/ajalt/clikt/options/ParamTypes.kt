package com.github.ajalt.clikt.options

object StringParamType : ParamType<String> {
    override val metavar = "STRING"
    override fun convert(value: String) = value
}
