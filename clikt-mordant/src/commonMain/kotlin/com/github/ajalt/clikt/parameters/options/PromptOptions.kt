package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.MissingOption
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.output.ParameterFormatter
import com.github.ajalt.clikt.parameters.transform.terminal
import com.github.ajalt.mordant.terminal.ConfirmationPrompt
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Prompt
import com.github.ajalt.mordant.terminal.YesNoPrompt

/**
 * If the option isn't given on the command line, prompt the user for manual input.
 *
 * Note that if the option is defined with a [validate] or [check], that validation will be run each
 * time the user enters a value. This means that, unlike normal options, the validation for prompt
 * options cannot reference other parameters.
 *
 * Note that if the terminal's input is non-interactive, this function is effectively identical to [required].
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
    val promptText = text ?: names.maxByOrNull { it.length }
        ?.let { splitOptionPrefix(it).second }
        ?.replace(Regex("\\W"), " ")?.capitalize2() ?: "Value"
    val provided = invocations.lastOrNull()
    if (provided != null) return@transformAll provided
    if (!terminal.terminalInfo.inputInteractive) throw MissingOption(option)
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


/**
 * If the option isn't given on the command line, prompt the user for manual input.
 *
 * Note that if the terminal's input is non-interactive, this function is effectively identical to [required].
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
                    if (!terminal.terminalInfo.inputInteractive) throw MissingOption(option)
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
        validator = {},
    )
}


// the stdlib capitalize was deprecated without a replacement
private fun String.capitalize2(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private fun splitOptionPrefix(name: String): Pair<String, String> =
    when {
        name.length < 2 || name[0] !in "-@/+" -> "" to name
        name.length > 2 && name[0] == name[1] -> name.slice(0..1) to name.substring(2)
        else -> name.substring(0, 1) to name.substring(1)
    }
