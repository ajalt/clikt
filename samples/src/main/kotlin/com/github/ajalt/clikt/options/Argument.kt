package com.github.ajalt.clikt.options

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Argument(val name: String, val shortName: String = "", val nargs: Int=1, val required: Boolean=false)

class ArgumentParser(val nargs: Int, val required: Boolean) {
}
