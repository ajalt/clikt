[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [CliktConsole](./index.md)

# CliktConsole

`interface CliktConsole`

An object that is used by commands and parameters to show text to the user and read input.

By default, stdin and stdout are used, but you can provide an implementation of this interface to
[Context.console](../../com.github.ajalt.clikt.core/-context/console.md) to customize the behavior.

### Properties

| Name | Summary |
|---|---|
| [lineSeparator](line-separator.md) | `abstract val lineSeparator: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The line separator to use. (Either "\n" or "\r\n") |

### Functions

| Name | Summary |
|---|---|
| [print](print.md) | `abstract fun print(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, error: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Show some [text](print.md#com.github.ajalt.clikt.output.CliktConsole$print(kotlin.String, kotlin.Boolean)/text) to the user. |
| [promptForLine](prompt-for-line.md) | `abstract fun promptForLine(prompt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, hideInput: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>Show the [prompt](prompt-for-line.md#com.github.ajalt.clikt.output.CliktConsole$promptForLine(kotlin.String, kotlin.Boolean)/prompt) to the user, and return a line of their response. |

### Inheritors

| Name | Summary |
|---|---|
| [InteractiveCliktConsole](../-interactive-clikt-console/index.md) | `class InteractiveCliktConsole : `[`CliktConsole`](./index.md) |
| [NonInteractiveCliktConsole](../-non-interactive-clikt-console/index.md) | `class NonInteractiveCliktConsole : `[`CliktConsole`](./index.md) |
