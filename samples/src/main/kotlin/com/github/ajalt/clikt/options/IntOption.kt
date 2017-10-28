package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.BadParameter

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntOption(val name: String, val shortName: String = "", val default: Int = 0)

object IntParamType : ParamType<Int> {
    override val metavar = "INTEGER"

    override fun convert(value: String): Int = try {
        value.toInt()
    } catch (e: NumberFormatException) {
        throw BadParameter("$value is not a valid integer")
    }
}
