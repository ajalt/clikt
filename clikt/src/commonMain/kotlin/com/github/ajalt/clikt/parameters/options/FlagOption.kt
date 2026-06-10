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
 * @param acceptsValue If true, this flag also accepts an explicit attached value like `--flag=true` or
 *   `--flag=false`. The value is parsed leniently (`true/t/1/yes/y/on` ↔ `false/f/0/no/n/off`, case
 *   insensitive; an empty value is an error). The value must be attached with `=`; a bare `--flag` still means
 *   true (or false for a [secondaryNames] negation name), and giving a value to a negation name is an error.
 *
 * ### Example:
 *
 * ```
 * val flag by option(help = "flag option").flag("--no-flag", default = true, defaultForHelp = "enable")
 * // Options:
 * // --flag / --no-flag  flag option (default: enable)
 *
 * // The same flag also accepting an explicit value: --flag, --no-flag, --flag=true, --flag=false
 * val flag by option("--flag", help = "flag option").flag("--no-flag", default = true, acceptsValue = true)
 * ```
 */
fun RawOption.flag(
    vararg secondaryNames: String,
    default: Boolean = false,
    defaultForHelp: String = "",
    acceptsValue: Boolean = false,
): OptionWithValues<Boolean, Boolean, Boolean> {
    return nullableFlag(*secondaryNames, acceptsValue = acceptsValue)
        .default(default, defaultForHelp = defaultForHelp)
}

/**
 * A [flag] that doesn't have a default value.
 *
 * You will usually want [flag] instead of this function, but this can be useful if you need to use
 * a [transformAll] method like [required] or `prompt`.
 *
 * See [flag] for the meaning of [acceptsValue].
 */
fun RawOption.nullableFlag(
    vararg secondaryNames: String,
    acceptsValue: Boolean = false,
): NullableOption<Boolean, Boolean> {
    val secondary = secondaryNames.toSet()
    return boolean()
        .transformValues(if (acceptsValue) 0..1 else 0..0) { values ->
            if (values.size > 1) {
                fail(context.localization.invalidFlagValueInFile(name))
            }
            val value = values.lastOrNull()
            if (value != null && name in secondary) {
                val positiveName = names.firstOrNull { it.startsWith("--") } ?: name
                fail(context.localization.valueOnFlagNegation(name, positiveName, value.toString()))
            }
            value ?: (name !in secondary)
        }
        .copy(secondaryNames = secondary, acceptsUnattachedValue = !acceptsValue)
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
