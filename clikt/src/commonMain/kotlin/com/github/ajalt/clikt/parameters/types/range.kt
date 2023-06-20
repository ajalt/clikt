package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.transform.TransformContext

private fun <T : Comparable<T>> TransformContext.checkRange(
    it: T, min: T?, max: T?, clamp: Boolean,
): T {
    require(min == null || max == null || min < max) { "min must be less than max" }
    if (clamp) {
        if (min != null && it < min) return min
        if (max != null && it > max) return max
    } else if (min != null && it < min || max != null && it > max) {
        val message = when {
            min == null -> context.localization.rangeExceededMax(it.toString(), max.toString())
            max == null -> context.localization.rangeExceededMin(it.toString(), min.toString())
            else -> context.localization.rangeExceededBoth(
                it.toString(), min.toString(), max.toString()
            )
        }
        fail(message)
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
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * argument().int().restrictTo(max=10, clamp=true).default(10)
 * ```
 */
fun <T : Comparable<T>> ProcessedArgument<T, T>.restrictTo(
    min: T? = null,
    max: T? = null,
    clamp: Boolean = false,
): ProcessedArgument<T, T> = convert { checkRange(it, min, max, clamp) }

/**
 * Restrict the argument values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * argument().int().restrictTo(1..10, clamp=true).default(10)
 * ```
 */
fun <T : Comparable<T>> ProcessedArgument<T, T>.restrictTo(
    range: ClosedRange<T>,
    clamp: Boolean = false,
): ProcessedArgument<T, T> = restrictTo(range.start, range.endInclusive, clamp)

// Options

/**
 * Restrict the option values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * option().int().restrictTo(max=10, clamp=true).default(10)
 * ```
 */
fun <T : Comparable<T>> OptionWithValues<T?, T, T>.restrictTo(
    min: T? = null,
    max: T? = null,
    clamp: Boolean = false,
): OptionWithValues<T?, T, T> = convert { checkRange(it, min, max, clamp) }


/**
 * Restrict the option values to fit into a range.
 *
 * By default, conversion fails if the value is outside the range, but if [clamp] is true, the value will be
 * silently clamped to fit in the range.
 *
 * This must be called before transforms like `pair`, `default`, or `multiple`, since it checks each
 * individual value.
 *
 * ### Example:
 *
 * ```
 * option().int().restrictTo(1..10, clamp=true).default(10)
 * ```
 */
fun <T : Comparable<T>> OptionWithValues<T?, T, T>.restrictTo(
    range: ClosedRange<T>,
    clamp: Boolean = false,
): OptionWithValues<T?, T, T> = restrictTo(range.start, range.endInclusive, clamp)
