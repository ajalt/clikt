package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.BadParameterValue
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
 * @param negatable If true and no [secondaryNames] are given, derive a `--no-` prefixed negation from each
 *   long name of this option (e.g. `--cache` gets a `--no-cache` that sets the value to false). Requires at
 *   least one long (`--`) name to derive from. Pass explicit [secondaryNames] instead for names where the
 *   `--no-` convention doesn't fit (e.g. `--enable` paired with `--disable`).
 *
 * ### Example:
 *
 * ```
 * val flag by option(help = "flag option").flag("--no-flag", default = true, defaultForHelp = "enable")
 * // Options:
 * // --flag / --no-flag  flag option (default: enable)
 *
 * // The same flag with the negation derived automatically from the --long name:
 * val cache by option("--cache", help = "flag option").flag(default = true, negatable = true)
 * ```
 */
fun RawOption.flag(
    vararg secondaryNames: String,
    default: Boolean = false,
    defaultForHelp: String = "",
    negatable: Boolean = false,
): OptionWithValues<Boolean, Boolean, Boolean> {
    return nullableFlag(*secondaryNames, negatable = negatable)
        .default(default, defaultForHelp = defaultForHelp)
}

/**
 * A [flag] that doesn't have a default value.
 *
 * You will usually want [flag] instead of this function, but this can be useful if you need to use
 * a [transformAll] method like [required] or `prompt`.
 *
 * See [flag] for the meaning of [negatable].
 */
fun RawOption.nullableFlag(
    vararg secondaryNames: String,
    negatable: Boolean = false,
): NullableOption<Boolean, Boolean> {
    val negations: Set<String> = when {
        secondaryNames.isNotEmpty() -> secondaryNames.toSet()
        negatable -> {
            val longNames = names.filter { it.startsWith("--") }
            require(longNames.isNotEmpty()) {
                "negatable requires an explicit long option name to derive a \"--no-\" negation from"
            }
            longNames.mapTo(mutableSetOf()) { "--no-" + it.removePrefix("--") }
        }

        else -> emptySet()
    }
    return boolean()
        .transformValues(0..0) {
            if (it.size > 1) {
                fail(context.localization.invalidFlagValueInFile(name))
            }
            it.lastOrNull() ?: (name !in negations)
        }
        .copy(secondaryNames = negations)
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
 *
 * @param limit The maximum number of times the option can be given. (defaults to no limit)
 * @param clamp If `true`, the counted value will be clamped to the [limit] if it is exceeded. If
 *   `false`, an error will be shown isntead of clamping.
 */
fun RawOption.counted(
    limit: Int = Int.MAX_VALUE,
    clamp: Boolean = true,
): OptionWithValues<Int, Int, Int> {
    return int().transformValues(0..0) { it.lastOrNull() ?: 1 }.transformAll {
        val s = it.sum()
        if (!clamp && s > limit) {
            fail(context.localization.countedOptionExceededLimit(s, limit))
        }
        s.coerceAtMost(limit)
    }
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
fun <T : Any> RawOption.switch(choices: Map<String, T>): NullableOption<T, String> {
    require(choices.isNotEmpty()) { "Must specify at least one choice" }
    return transformValues(0..0) {
        require(it.isEmpty()) { context.localization.switchOptionEnvvar() }
        choices.getValue(name)
    }.copy(names = choices.keys)
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
fun <T : Any> RawOption.switch(vararg choices: Pair<String, T>): NullableOption<T, String> {
    return switch(mapOf(*choices))
}
