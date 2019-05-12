[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [prompt](./prompt.md)

# prompt

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`NullableOption`](-nullable-option.md)`<`[`T`](prompt.md#T)`, `[`T`](prompt.md#T)`>.prompt(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, default: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, hideInput: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, requireConfirmation: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, confirmationPrompt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "Repeat for confirmation: ", promptSuffix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ": ", showDefault: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): `[`OptionWithValues`](-option-with-values/index.md)`<`[`T`](prompt.md#T)`, `[`T`](prompt.md#T)`, `[`T`](prompt.md#T)`>`

If the option isn't given on the command line, prompt the user for manual input.

### Parameters

`text` - The text to prompt the user with

`default` - The default value to use if no input is given. If null, the prompt will be repeated until
input is given.

`hideInput` - If true, user input will not be shown on the screen. Useful for passwords and sensitive
input.

`requireConfirmation` - If true, the user will be required to enter the same value twice before it is
accepted.

`confirmationPrompt` - If [requireConfirmation](prompt.md#com.github.ajalt.clikt.parameters.options$prompt(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.prompt.T, com.github.ajalt.clikt.parameters.options.prompt.T, )), kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean)/requireConfirmation) is true, this will be used to ask for input again.

`promptSuffix` - Text to display directly after [text](prompt.md#com.github.ajalt.clikt.parameters.options$prompt(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.prompt.T, com.github.ajalt.clikt.parameters.options.prompt.T, )), kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean)/text). Defaults to ": ".

`showDefault` - Show [default](prompt.md#com.github.ajalt.clikt.parameters.options$prompt(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.prompt.T, com.github.ajalt.clikt.parameters.options.prompt.T, )), kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean)/default) to the user in the prompt.