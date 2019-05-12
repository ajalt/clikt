[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [NonInteractiveCliktConsole](./index.md)

# NonInteractiveCliktConsole

`class NonInteractiveCliktConsole : `[`CliktConsole`](../-clikt-console/index.md)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `NonInteractiveCliktConsole()` |

### Properties

| Name | Summary |
|---|---|
| [lineSeparator](line-separator.md) | `val lineSeparator: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The line separator to use. (Either "\n" or "\r\n") |

### Functions

| Name | Summary |
|---|---|
| [print](print.md) | `fun print(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, error: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Show some [text](../-clikt-console/print.md#com.github.ajalt.clikt.output.CliktConsole$print(kotlin.String, kotlin.Boolean)/text) to the user. |
| [promptForLine](prompt-for-line.md) | `fun promptForLine(prompt: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, hideInput: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Show the [prompt](../-clikt-console/prompt-for-line.md#com.github.ajalt.clikt.output.CliktConsole$promptForLine(kotlin.String, kotlin.Boolean)/prompt) to the user, and return a line of their response. |
