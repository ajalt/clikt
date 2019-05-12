[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [NonInteractiveCliktConsole](index.md) / [promptForLine](./prompt-for-line.md)

# promptForLine

`fun promptForLine(prompt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, hideInput: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)

Overrides [CliktConsole.promptForLine](../-clikt-console/prompt-for-line.md)

Show the [prompt](../-clikt-console/prompt-for-line.md#com.github.ajalt.clikt.output.CliktConsole$promptForLine(kotlin.String, kotlin.Boolean)/prompt) to the user, and return a line of their response.

This function will block until a line of input has been read.

### Parameters

`prompt` - The text to display to the user

`hideInput` - If true, the user's input should not be echoed to the screen. If the current console
does not support hidden input, this argument may be ignored..

**Return**
A line of user input, or null if an error occurred.

