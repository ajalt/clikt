package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.BadParameter

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntOption(vararg val names: String, val default: Int = 0, val help: String= "")

/**
 * @param name The name to show in the help message. If not given, defaults to the name of the
 *     parameter being annotated.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntArgument(val name: String = "",
                             val nargs: Int = 1,
                             val required: Boolean = false,
                             val default: Int = 0,
                             val help: String= "")

object IntParamType : ParamType<Int> {
    override val metavar = "INTEGER"

    override fun convert(value: String): Int = try {
        value.toInt()
    } catch (e: NumberFormatException) {
        throw BadParameter("$value is not a valid integer")
    }
}
