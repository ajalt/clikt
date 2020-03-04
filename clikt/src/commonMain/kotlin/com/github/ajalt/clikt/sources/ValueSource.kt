package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.longestName
import com.github.ajalt.clikt.parameters.options.splitOptionPrefix

interface ValueSource {
    data class Invocation(val values: List<String>) {
        companion object {
            /** Create a list of a single Invocation with a single value */
            fun just(value: Any?): List<Invocation> = listOf(value(value))

            /** Create an Invocation with a single value */
            fun value(value: Any?): Invocation = Invocation(listOf(value.toString()))
        }
    }

    fun getValues(context: Context, option: Option): List<Invocation>

    companion object {
        /**
         * Get a name for an option that can by useful as a key for a value source.
         *
         * The returned value is the longest option name with its prefix removed
         *
         * ## Examples
         *
         * ```
         * name(option("-h", "--help")) == "help"
         * name(option("/INPUT")) == "INPUT"
         * name(option("--new-name", "--name")) == "new-name
         * ```
         */
        fun name(option: Option): String {
            val longestName = option.longestName()
            requireNotNull(longestName) { "Option must have a name" }
            return splitOptionPrefix(longestName).second
        }
    }
}
