package com.github.ajalt.clikt.parameters.options

/** A container for a value that can have a default value and can be manually set */
data class ValueWithDefault<out T>(val explicit: T?, val default: T) {
    val value: T get() = explicit ?: default
}

