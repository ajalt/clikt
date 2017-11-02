package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.Context
import com.github.ajalt.clikt.parser.Parameter

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PassContext


class PassContextParameter : Parameter {
    override fun processValues(context: Context, values: List<*>) = context
    override val exposeValue get() = true
    override val parameterHelp get() = null
}
