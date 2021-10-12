package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.int

/** A block that converts a flag value from one type to another */
typealias FlagConverter<InT, OutT> = OptionTransformContext.(InT) -> OutT

/**
 * Turn an option into a boolean flag.
 *
 * @param secondaryNames additional names for that option that cause the option value to be false. It's good
 *   practice to provide secondary names so that users can disable an option that was previously enabled.
 * @param default the value for this property if the option is not given on the command line.
 * @param defaultForHelp The help text for this option's default value if the help formatter is configured
 *   to show them.
 *
 * ### Example:
 *
 * ```
 * val flag by option(help = "flag option").flag("--no-flag", default = true, defaultForHelp = "enable")
 * // Options:
 * // --flag / --no-flag  flag option (default: enable)
 * ```
 */
fun RawOption.flag(
    vararg secondaryNames: String,
    default: Boolean = false,
    defaultForHelp: String = "",
): OptionWithValues<Boolean, Boolean, Boolean> {
    val tags = helpTags + mapOf(HelpFormatter.Tags.DEFAULT to defaultForHelp)
    return boolean().toFlag { it.lastOrNull() ?: (name !in secondaryNames) }
        .default(default)
        .copy(secondaryNames = secondaryNames.toSet(), helpTags = helpTags + tags)
}


/**
 * Convert this flag's value type.
 *
 * The [conversion] is called once with the final value of the option. If any errors are thrown,
 * they are caught and a [BadParameterValue] is thrown with the error message. You can call
 * [fail][OptionTransformContext.fail] to throw a [BadParameterValue] manually.
 *
 * ## Example
 *
 * ```
 * val loud by option().flag().convert { if (it) Volume.Loud else Volume.Soft }
 * ```
 */
inline fun <OutT> OptionWithValues<Boolean, Boolean, Boolean>.convert(
    crossinline conversion: FlagConverter<Boolean, OutT>,
): OptionWithValues<OutT, Boolean, Boolean> {
    return copy(
        transformValue = transformValue,
        transformEach = transformEach,
        transformAll = { conversion(transformAll(it)) },
        validator = defaultValidator(),
    )
}

/**
 * Turn an option into a flag that counts the number of times it occurs on the command line.
 */
fun RawOption.counted(): OptionWithValues<Int, Int, Int> {
    return int().toFlag { it.lastOrNull() ?: 1 }.transformAll { it.sum() }
}

/**
 * Turn an option into a set of flags that each map to a value.
 *
 * ### Example:
 *
 * ```
 * option().switch(mapOf("--foo" to Foo(), "--bar" to Bar()))
 * ```
 */
fun <T : Any> RawOption.switch(choices: Map<String, T>): OptionWithValues<T?, T?, String> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return toFlag { choices[name] }.copy(names = choices.keys)
}

/**
 * Turn an option into a set of flags that each map to a value.
 *
 * ### Example:
 *
 * ```
 * option().switch("--foo" to Foo(), "--bar" to Bar())
 * ```
 */
fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): OptionWithValues<T?, T?, String> {
    return switch(mapOf(*choices))
}

private fun <EachT, ValueT> NullableOption<ValueT, ValueT>.toFlag(
    transform: ValuesTransformer<ValueT, EachT>,
): OptionWithValues<EachT?, EachT, ValueT> = copy(
    transformValue = transformValue,
    transformEach = transform,
    metavarGetter = { null },
    transformAll = defaultAllProcessor(),
    validator = defaultValidator(),
    nvalues = 0..0
)
