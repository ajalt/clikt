package com.github.ajalt.clikt.options

import kotlin.reflect.KParameter

interface ArgumentParser {
    val name: String
    val nargs: Int
    val required: Boolean
    fun parse(args: List<String>): Any?
    /**
     * Throw an exception if the given [KParameter] accepts does not accept arguments of the type
     * returned by this parser.
     *
     * Due to type erasure, it's not possible to know the exact type of the parameter in all cases.
     * This function should not throw an exception unless the parameter type definitely does not
     * accept the values from this parser.
     *
     * @throws IllegalArgumentException if the type of [param] definitely does not accept the values
     *     from this parser.
     */
    fun checkTarget(param: KParameter)
}

