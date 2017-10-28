package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.Context
import com.github.ajalt.clikt.parser.Parameter

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PassContext


object PassContextParameter : Parameter() {
    override fun getDefaultValue(context: Context): Any? {
        return context
    }
}
