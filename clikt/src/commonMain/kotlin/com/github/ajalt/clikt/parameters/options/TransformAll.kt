@file:JvmMultifileClass
@file:JvmName("OptionWithValuesKt")

package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.MissingOption
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.ParameterFormatter
import com.github.ajalt.mordant.terminal.ConfirmationPrompt
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Prompt
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * Transform all calls to the option to the final option type.
 *
 * The input is a list of calls, one for each time the option appears on the command line. The values in the
 * list are the output of calls to [transformValues]. If the option does not appear from any source (command
 * line or envvar), this will be called with an empty list.
 *
 * Used to implement functions like [default] and [multiple].
 *
 * ## Example
 *
 * ```
 * val entries by option().transformAll { it.joinToString() }
 * ```
 *
 * @param defaultForHelp The help text for this option's default value if the help formatter is
 *   configured to show them, or null if this option has no default or the default value should not be
 *   shown.This does not affect behavior outside of help formatting.
 * @param showAsRequired Tell the help formatter that this option should be marked as required. This
 *   does not affect behavior outside of help formatting.
 */
fun <AllT, EachT, ValueT> NullableOption<EachT, ValueT>.transformAll(
    defaultForHelp: String? = this.helpTags[HelpFormatter.Tags.DEFAULT],
    showAsRequired: Boolean = HelpFormatter.Tags.REQUIRED in this.helpTags,
    transform: AllTransformer<EachT, AllT>,
): OptionWithValues<AllT, EachT, ValueT> {
    val tags = this.helpTags.toMutableMap()

    if (showAsRequired) tags[HelpFormatter.Tags.REQUIRED] = ""
    else tags.remove(HelpFormatter.Tags.REQUIRED)

    if (defaultForHelp != null) tags[HelpFormatter.Tags.DEFAULT] = defaultForHelp
    else tags.remove(HelpFormatter.Tags.DEFAULT)

    return copy(transformValue, transformEach, transform, defaultValidator(), helpTags = tags)
}

/**
 * If the option is not called on the command line (and is not set in an envvar), use [value] for the option.
 *
 * This must be applied after all other transforms.
 *
 * You can customize how the default is shown to the user with [defaultForHelp].
 *
 * If you need to compute the default lazily, use [defaultLazy].
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int> by option().int().pair().default(1 to 2)
 * ```
 */
fun <EachT : Any, ValueT> NullableOption<EachT, ValueT>.default(
    value: EachT,
    defaultForHelp: String = value.toString(),
): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(defaultForHelp) { it.lastOrNull() ?: value }
}

/**
 * If the option is not called on the command line (and is not set in an envvar), call the [value] and use its
 * return value for the option.
 *
 * This must be applied after all other transforms. If the option is given on the command line, [value] will
 * not be called.
 *
 * You can customize how the default is shown to the user with [defaultForHelp]. The default value
 * is an empty string, so if you have the help formatter configured to show values, you should set
 * this value manually.
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int> by option().int().pair().defaultLazy { expensiveOperation() }
 * ```
 */
inline fun <EachT, ValueT> NullableOption<EachT, ValueT>.defaultLazy(
    defaultForHelp: String = "",
    crossinline value: () -> EachT,
): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(defaultForHelp) { it.lastOrNull() ?: value() }
}

/**
 * If the option is not called on the command line (and is not set in an envvar), throw a [MissingOption].
 *
 * This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: Pair<Int, Int> by option().int().pair().required()
 * ```
 */
fun <EachT, ValueT> NullableOption<EachT, ValueT>.required(): OptionWithValues<EachT, EachT, ValueT> {
    return transformAll(showAsRequired = true) { it.lastOrNull() ?: throw MissingOption(option) }
}

/**
 * Make the option return a list of calls; each item in the list is the value of one call.
 *
 * If the option is never called, the list will be empty. This must be applied after all other transforms.
 *
 * ### Example:
 *
 * ```
 * val opt: List<Pair<Int, Int>> by option().int().pair().multiple()
 * ```
 *
 * @param default The value to use if the option is not supplied. Defaults to an empty list.
 * @param required If true, [default] is ignored and [MissingOption] will be thrown if no
 *   instances of the option are present on the command line.
 */
fun <EachT, ValueT> NullableOption<EachT, ValueT>.multiple(
    default: List<EachT> = emptyList(),
    required: Boolean = false,
): OptionWithValues<List<EachT>, EachT, ValueT> {
    return transformAll(showAsRequired = required) {
        when {
            it.isEmpty() && required -> throw MissingOption(option)
            it.isEmpty() && !required -> default
            else -> it
        }
    }
}

/**
 * Make a [multiple] option return a unique set of values
 *
 * ### Example:
 *
 * ```
 * val opt: Set<Int> by option().int().multiple().unique()
 * val opt2: Set<Int> by option().int().split(",").default(emptyList()).unique()
 * ```
 */
fun <T, EachT, ValueT> OptionWithValues<List<T>, EachT, ValueT>.unique(): OptionWithValues<Set<T>, EachT, ValueT> {
    return copy(transformValue, transformEach, { transformAll(it).toSet() }, defaultValidator())
}

/**
 * Convert this option's values from a list of pairs into a map with key-value pairs from the list.
 *
 * If the same key appears more than once, the last one will be added to the map.
 */
fun <A, B, EachT, ValueT> OptionWithValues<List<Pair<A, B>>, EachT, ValueT>.toMap(): OptionWithValues<Map<A, B>, EachT, ValueT> {
    return copy(transformValue, transformEach, { transformAll(it).toMap() }, defaultValidator())
}

/**
 * Change this option to take multiple values, each split on a [delimiter], and converted to a map.
 *
 * This is shorthand for [splitPair], [multiple], and [toMap].
 */
fun RawOption.associate(delimiter: String = "="): OptionWithValues<Map<String, String>, Pair<String, String>, Pair<String, String>> {
    return splitPair(delimiter).multiple().toMap()
}

/**
 * If the option isn't given on the command line, prompt the user for manual input.
 *
 * Note that if the option is defined with a [validate] or [check], that validation will be run each
 * time the user enters a value. This means that, unlike normal options, the validation for prompt
 * options cannot reference other parameters.
 *
 * @param text The text to prompt the user with
 * @param default The default value to use if no input is given. If null, the prompt will be repeated until
 *   input is given.
 * @param hideInput If true, user input will not be shown on the screen. Useful for passwords and sensitive
 *   input.
 * @param promptSuffix Text to display directly after [text]. Defaults to ": ".
 * @param showDefault Show [default] to the user in the prompt.
 */
fun <T : Any> NullableOption<T, T>.prompt(
    text: String? = null,
    default: T? = null,
    hideInput: Boolean = false,
    promptSuffix: String = ": ",
    showDefault: Boolean = true,
    requireConfirmation: Boolean = false,
    confirmationPrompt: String = "Repeat for confirmation",
    confirmationMismatchMessage: String = "Values do not match, try again",
): OptionWithValues<T, T, T> = transformAll { invocations ->
    val promptText = text ?: longestName()?.let { splitOptionPrefix(it).second }
        ?.replace(Regex("\\W"), " ")?.capitalize2() ?: "Value"
    val provided = invocations.lastOrNull()
    if (provided != null) return@transformAll provided
    if (context.errorEncountered) throw Abort()

    val builder: (String) -> Prompt<T> = {
        object : Prompt<T>(
            prompt = it,
            terminal = terminal,
            default = default,
            showDefault = showDefault,
            hideInput = hideInput,
            promptSuffix = promptSuffix,
        ) {
            override fun convert(input: String): ConversionResult<T> {
                val ctx = OptionCallTransformContext("", this@transformAll, context)
                try {
                    val v = transformEach(ctx, listOf(transformValue(ctx, input)))

                    @Suppress("UNCHECKED_CAST")
                    val validator = (option as? OptionWithValues<T, T, T>)?.transformValidator
                    validator?.invoke(this@transformAll, v)
                    return ConversionResult.Valid(v)
                } catch (e: UsageError) {
                    e.context = e.context ?: context
                    return ConversionResult.Invalid(
                        e.formatMessage(
                            context.localization,
                            ParameterFormatter.Plain
                        )
                    )
                }
            }
        }
    }
    val result = if (requireConfirmation) {
        ConfirmationPrompt.create(
            promptText,
            confirmationPrompt,
            confirmationMismatchMessage,
            builder
        ).ask()
    } else {
        builder(promptText).ask()
    }
    return@transformAll result ?: throw Abort()
}

// the stdlib capitalize was deprecated without a replacement
private fun String.capitalize2(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
