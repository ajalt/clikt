package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.options.OptionWithValues

private inline fun <T> checkRange(it: T, min: T? = null, max: T? = null,
                                  clamp: Boolean, fail: (String) -> Unit): T
        where T : Number, T : Comparable<T> {
    require(min == null || max == null || min < max) { "min must be less than max" }
    if (clamp) {
        if (min != null && it < min) return min
        if (max != null && it > max) return max
    } else if (min != null && it < min || max != null && it > max) {
        fail(when {
            min == null -> "$it is larger than the maximum valid value of $max."
            max == null -> "$it is smaller than the minimum valid value of $min."
            else -> "$it is not in the valid range of $min to $max."
        })
    }
    return it
}


// Arguments
/**
 * Restrict the argument values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * Example:
 *
 * ```kotlin
 * argument().int().restrictTo(max=10, clamp=true)
 * ```
 */
fun <T> ProcessedArgument<T, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : ProcessedArgument<T, T> where T : Number, T : Comparable<T> {
    return copy({ checkRange(transformValue(it), min, max, clamp) { fail(it) } }, transformAll)
}

/**
 * Restrict the argument values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * Example:
 *
 * ```kotlin
 * argument().int().restrictTo(1..10, clamp=true)
 * ```
 */
fun <T> ProcessedArgument<T, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)

// Options

/**
 * Restrict the option values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * Example:
 *
 * ```kotlin
 * option().int().restrictTo(max=10, clamp=true)
 * ```
 */
fun <T> OptionWithValues<T?, T, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : OptionWithValues<T?, T, T> where T : Number, T : Comparable<T> {
    return copy({ checkRange(transformValue(it), min, max, clamp) { fail(it) } }, transformEach, transformAll)
}


/**
 * Restrict the option values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * Example:
 *
 * ```kotlin
 * option().int().restrictTo(1..10, clamp=true)
 * ```
 */
fun <T> OptionWithValues<T?, T, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)
