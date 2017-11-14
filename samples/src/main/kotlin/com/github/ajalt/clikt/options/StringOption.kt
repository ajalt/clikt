package com.github.ajalt.clikt.options

internal const val STRING_OPTION_NO_DEFAULT = "STRING_OPTION_NO_DEFAULT"

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class StringOption(vararg val names: String,
                              val default: String = STRING_OPTION_NO_DEFAULT,
                              val help: String = "",
                              val nargs: Int = 1)

/**
 * @param name The name to show in the help message. If not given, defaults to the name of the
 *     parameter being annotated.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class StringArgument(val name: String = "",
                                val nargs: Int = 1,
                                val required: Boolean = false,
                                val default: String = STRING_OPTION_NO_DEFAULT,
                                val help: String = "")

object StringParamType : ParamType<String> {
    override fun convert(value: String): String = value
}
