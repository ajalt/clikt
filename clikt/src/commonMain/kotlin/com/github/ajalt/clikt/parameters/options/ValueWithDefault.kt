package com.github.ajalt.clikt.parameters.options

/** A container for a value that can have a default value and can be manually set */
data class ValueWithDefault<out T>(val explicit: T?, val default: T) {
    val value: T get() = explicit ?: default
}

/** Create a copy with a new [default] value */
fun <T> ValueWithDefault<T>.withDefault(default: T) = copy(default = default)

/** Create a copy with a new [explicit] value */
fun <T> ValueWithDefault<T>.withExplicit(explicit: T) = copy(explicit = explicit)
