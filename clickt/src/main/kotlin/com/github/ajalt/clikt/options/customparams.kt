package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.Option
import com.github.ajalt.clikt.parser.PrintHelpMessage

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PassContext


internal fun helpOption(names: Array<String>) = Option.buildWithoutParameter {
    this.names = names
    parser = FlagOptionParser()
    help = "Show this help message and exit."
    eager = true
    processor = { context, values ->
        if (values.lastOrNull() == true) {
//            throw PrintHelpMessage(context.command)
        }
        null
    }
}
