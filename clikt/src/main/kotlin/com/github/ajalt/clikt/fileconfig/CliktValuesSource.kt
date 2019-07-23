package com.github.ajalt.clikt.fileconfig

import com.github.ajalt.clikt.core.Context

/**
 * A source of values for options that is used when the option is not present on the command line.
 *
 * Implementations typically read a configuration file.
 */
interface CliktValuesSource {
    data class Invocation(val values: List<String>) {
        companion object {
            /** Create a list of a single Invocation with a single value */
            fun just(value: Any?): List<Invocation> = listOf(value(value))
            /** Create an Invocation with a single value */
            fun value(value: Any?): Invocation = Invocation(listOf(value.toString()))
        }
    }

    /**
     * Called once before the command line is parsed to perform any resource loading necessary.
     *
     * Implementations that read from files typically should do so here.
     */
    fun initialize()

    /**
     * Read values for an option with the given [key].
     *
     * This is called at most once per option. The key may contain `.` characters that denote
     * nesting; it's up to the implementation to interpret the key.
     */
    fun readValues(context: Context, key: String): List<Invocation>

    /**
     * Called once after all option values have been set to free any resources.
     *
     * [readValues] will never be called after this function has been called.
     */
    fun close() {}
}

