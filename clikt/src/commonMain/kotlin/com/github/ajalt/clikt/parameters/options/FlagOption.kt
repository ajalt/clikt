package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.YesNoPrompt

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
    return boolean()
        .transformValues(0..0) { it.lastOrNull() ?: (name !in secondaryNames) }
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
    return int().transformValues(0..0) { it.lastOrNull() ?: 1 }.transformAll { it.sum() }
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
    return transformValues(0..0) { choices[name] }.copy(names = choices.keys)
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


/**
 * If the option isn't given on the command line, prompt the user for manual input.
 *
 * @param text The message asking for input to show the user
 * @param default The value to return if the user enters an empty line, or `null` to require a value
 * @param uppercaseDefault If true and [default] is not `null`, the default choice will be shown in uppercase.
 * @param showChoices If true, the choices will be added to the [prompt]
 * @param choiceStrings The strings to accept for `true` and `false` inputs
 * @param promptSuffix A string to append after [prompt] when showing the user the prompt
 * @param invalidChoiceMessage The message to show the user if they enter a value that isn't one of the [choiceStrings].
 */
fun OptionWithValues<Boolean, Boolean, Boolean>.prompt(
    text: String,
    default: Boolean? = null,
    uppercaseDefault: Boolean = true,
    showChoices: Boolean = true,
    choiceStrings: List<String> = listOf("y", "n"),
    promptSuffix: String = ": ",
    invalidChoiceMessage: String = "Invalid value, choose from ",
): OptionWithValues<Boolean, Boolean, Boolean> {
    return copy(
        transformValue = transformValue,
        transformEach = transformEach,
        transformAll = { invocations ->
            when (val provided = invocations.lastOrNull()) {
                null -> {
                    YesNoPrompt(
                        prompt = text,
                        terminal = context.terminal,
                        default = default,
                        uppercaseDefault = uppercaseDefault,
                        showChoices = showChoices,
                        choiceStrings = choiceStrings,
                        promptSuffix = promptSuffix,
                        invalidChoiceMessage = invalidChoiceMessage,
                    ).ask()
                }
                else -> provided
            } ?: throw Abort()
        },
        validator = defaultValidator(),
    )
}
