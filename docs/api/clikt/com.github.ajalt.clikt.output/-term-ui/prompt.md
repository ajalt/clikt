[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [TermUi](index.md) / [prompt](./prompt.md)

# prompt

`fun <T> prompt(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, hideInput: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, requireConfirmation: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, confirmationPrompt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "Repeat for confirmation: ", promptSuffix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ": ", showDefault: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, console: `[`CliktConsole`](../-clikt-console/index.md)` = defaultCliktConsole(), convert: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`T`](prompt.md#T)`): `[`T`](prompt.md#T)`?`

Prompt a user for text input.

If the user send a terminate signal (e,g, ctrl-c) while the prompt is active, null will be returned.

### Parameters

`text` - The text to display for the prompt.

`default` - The default value to use for the input. If the user enters a newline without any other
value, [default](prompt.md#com.github.ajalt.clikt.output.TermUi$prompt(kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.Function1((kotlin.String, com.github.ajalt.clikt.output.TermUi.prompt.T)))/default) will be returned. This parameter is a String instead of [T](prompt.md#T), since it will be
displayed to the user.

`hideInput` - If true, the user's input will not be echoed back to the screen. This is commonly used
for password inputs.

`requireConfirmation` - If true, the user will be required to enter the same value twice before it
is accepted.

`confirmationPrompt` - The text to show the user when [requireConfirmation](prompt.md#com.github.ajalt.clikt.output.TermUi$prompt(kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.Function1((kotlin.String, com.github.ajalt.clikt.output.TermUi.prompt.T)))/requireConfirmation) is true.

`promptSuffix` - A delimiter printed between the [text](prompt.md#com.github.ajalt.clikt.output.TermUi$prompt(kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.Function1((kotlin.String, com.github.ajalt.clikt.output.TermUi.prompt.T)))/text) and the user's input.

`showDefault` - If true, the [default](prompt.md#com.github.ajalt.clikt.output.TermUi$prompt(kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.Function1((kotlin.String, com.github.ajalt.clikt.output.TermUi.prompt.T)))/default) value will be shown as part of the prompt.

`convert` - A callback that will convert the text that the user enters to the return value of the
function. If the callback raises a [UsageError](../../com.github.ajalt.clikt.core/-usage-error/index.md), its message will be printed and the user will be
asked to enter a new value. If [default](prompt.md#com.github.ajalt.clikt.output.TermUi$prompt(kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.Function1((kotlin.String, com.github.ajalt.clikt.output.TermUi.prompt.T)))/default) is not null and the user does not input a value, the value
of [default](prompt.md#com.github.ajalt.clikt.output.TermUi$prompt(kotlin.String, kotlin.String, kotlin.Boolean, kotlin.Boolean, kotlin.String, kotlin.String, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.Function1((kotlin.String, com.github.ajalt.clikt.output.TermUi.prompt.T)))/default) will be passed to this callback.

**Return**
the user's input, or null if the stdin is not interactive and EOF was encountered.

`fun prompt(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, hideInput: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, requireConfirmation: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, confirmationPrompt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "Repeat for confirmation: ", promptSuffix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ": ", showDefault: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`