@file:JvmMultifileClass
@file:JvmName("OptionWithValuesKt")

package com.github.ajalt.clikt.parameters.options

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * Change the number of values that this option takes.
 *
 * This overload changes the option to take a variable number of values, with the number of values
 * falling within the [nvalues] range.
 */
fun <EachT, ValueT> NullableOption<ValueT, ValueT>.transformValues(
    nvalues: IntRange,
    transform: ValuesTransformer<ValueT, EachT>,
): NullableOption<EachT, ValueT> {
    require(!nvalues.isEmpty()) { "Cannot set nvalues to empty range." }
    require(nvalues.first >= 0) { "Options cannot have nvalues < 0" }
    require(nvalues != 1..1) { "Cannot set nvalues = 1. Use convert() instead." }
    return copy(
        transformValue = transformValue,
        transformEach = transform,
        transformAll = defaultAllProcessor(),
        validator = defaultValidator(),
        metavarGetter = if (nvalues == 0..0) {
            { null }
        } else {
            metavarGetter
        },
        nvalues = nvalues
    )
}

/**
 * Change the number of values that this option takes.
 *
 * The input will be a list of size [nvalues], with each item in the list being the output of a call to
 * [convert]. [nvalues] must be 2 or greater, since options cannot take a variable number of values, and
 * [option] has [nvalues] = 1 by default. If you want to change the type of an option with one value, use
 * [convert] instead.
 *
 * Used to implement functions like [pair] and [triple]. This must be applied after value
 * [conversions][convert] and before [transformAll].
 *
 * ## Example
 *
 * ```
 * data class Square(val top: Int, val right: Int, val bottom: Int, val left: Int)
 * val square by option().int().transformValues(4) { Square(it[0], it[1], it[2], it[3]) }
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<ValueT, ValueT>.transformValues(
    nvalues: Int,
    transform: ValuesTransformer<ValueT, EachT>,
): NullableOption<EachT, ValueT> = transformValues(nvalues..nvalues, transform)

/**
 * Change this option to take two values, held in a [Pair].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int>? by option().int().pair()
 * ```
 */
fun <ValueT> NullableOption<ValueT, ValueT>.pair()
        : NullableOption<Pair<ValueT, ValueT>, ValueT> {
    return transformValues(nvalues = 2) { it[0] to it[1] }
}

/**
 * Change this option to take three values, held in a [Triple].
 *
 * This must be called after converting the value type, and before other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: Triple<Int, Int, Int>? by option().int().triple()
 * ```
 */
fun <ValueT> NullableOption<ValueT, ValueT>.triple()
        : NullableOption<Triple<ValueT, ValueT, ValueT>, ValueT> {
    return transformValues(nvalues = 3) { Triple(it[0], it[1], it[2]) }
}


/**
 * Change this option to take a variable number of values.
 *
 * You can set the [min] and [max] number of values this option requires. By default, [min] is 1 and
 * [max] is unlimited.
 */
fun <ValueT> NullableOption<ValueT, ValueT>.varargValues(
    min: Int = 1,
    max: Int = Int.MAX_VALUE,
): NullableOption<List<ValueT>, ValueT> {
    return transformValues(nvalues = min..max) { it }
}

/**
 * Allow this option to be specified with or without an explicit value.
 *
 * If the option is specified on the command line without a value, [default] will be used.
 *
 * ## Example
 *
 * ```
 * val log by option().optionalValue("verbose").default("none")
 *
 * > ./tool --log=debug
 * log level == debug
 *
 * > ./tool --log
 * log level == verbose
 *
 * > ./tool
 * log level == none
 * ```
 */
fun <ValueT : Any> NullableOption<ValueT, ValueT>.optionalValue(default: ValueT): NullableOption<ValueT, ValueT> {
    return transformValues(0..1) { it.firstOrNull() ?: default }
}
