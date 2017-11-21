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


internal fun helpOption(names: Array<String>) = Option.buildWithoutParameter {
    this.names = names
    parser = FlagOptionParser()
    help = "Show this help message and exit."
    eager = true
    processor = { context, values ->
        if (values.lastOrNull() == true) {
            throw PrintHelpMessage(context.command)
        }
        null
    }
}
