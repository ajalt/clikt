package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.ArgumentDelegate
import com.github.ajalt.clikt.parameters.ProcessedArgument
import com.github.ajalt.clikt.parameters.options.OptionDelegate
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

fun <T> ProcessedArgument<T, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : ArgumentDelegate<T> where T : Number, T : Comparable<T> {
    return ProcessedArgument(name, nargs, required, help,
            { checkRange(processValue(it), min, max, clamp) { fail(it) } },
            processAll)
}

@JvmName("nullableRestrictTo")
fun <T> ProcessedArgument<T?, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : ArgumentDelegate<T?> where T : Number, T : Comparable<T> {
    return ProcessedArgument(name, nargs, required, help,
            { checkRange(processValue(it), min, max, clamp) { fail(it) } },
            processAll)
}

fun <T> ProcessedArgument<T, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)

@JvmName("nullableRestrictTo")
fun <T> ProcessedArgument<T?, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)


// Options

fun <T> OptionWithValues<T, T, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : OptionDelegate<T> where T : Number, T : Comparable<T> {
    return OptionWithValues(names, explicitMetavar, metavar, nargs, help,
            hidden, envvar, envvarSplit, parser,
            { checkRange(transformValue(it), min, max, clamp) { fail(it) } },
            transformEach, transformAll)
}

@JvmName("nullableRestrictTo")
fun <T> OptionWithValues<T?, T, T>.restrictTo(min: T? = null, max: T? = null, clamp: Boolean = false)
        : OptionDelegate<T?> where T : Number, T : Comparable<T> {
    return OptionWithValues(names, explicitMetavar, metavar, nargs, help,
            hidden, envvar, envvarSplit, parser,
            { checkRange(transformValue(it), min, max, clamp) { fail(it) } },
            transformEach, transformAll)
}

fun <T> OptionWithValues<T, T, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)

@JvmName("nullableRestrictTo")
fun <T> OptionWithValues<T?, T, T>.restrictTo(range: ClosedRange<T>, clamp: Boolean = false)
        where T : Number, T : Comparable<T> = restrictTo(range.start, range.endInclusive, clamp)
