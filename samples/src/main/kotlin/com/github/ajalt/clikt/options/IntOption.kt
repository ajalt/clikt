package com.github.ajalt.clikt.options

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntOption(val name: String, val shortName: String = "", val default: Int = 0)

class IntOptParser(commandArgIndex: Int) : OptionParser<Int>(commandArgIndex) {
    override fun convertValue(value: String) = value.toInt()
}
