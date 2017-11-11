package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.Option
import com.github.ajalt.clikt.parser.Parameter
import com.github.ajalt.clikt.parser.PrintHelpMessage

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PassContext


class PassContextParameter : Parameter {
    override fun processValues(context: Context, values: List<*>) = context
    override val exposeValue get() = true
    override val parameterHelp get() = null
}


class HelpOption(names: List<String>) : Option(
        names, FlagOptionParser(), false, false, null,
        "Show this help message and exit.", exposeValue = false) {
    override fun processValues(context: Context, values: List<*>): Any? {
        if (values.lastOrNull() == true) {
            throw PrintHelpMessage(context.command)
        }
        return null
    }
}
