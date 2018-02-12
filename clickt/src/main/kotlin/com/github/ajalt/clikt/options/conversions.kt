package com.github.ajalt.clikt.options

/** A class that converts option and argument values to a different type. */
interface ParamType<out T> { // TODO: make a type alias
    /** Given a value from the command line, return the value converted to this type. */
    fun convert(value: String): T
}

