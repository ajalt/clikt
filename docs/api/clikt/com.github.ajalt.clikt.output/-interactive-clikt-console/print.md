[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [InteractiveCliktConsole](index.md) / [print](./print.md)

# print

`fun print(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, error: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Overrides [CliktConsole.print](../-clikt-console/print.md)

Show some [text](../-clikt-console/print.md#com.github.ajalt.clikt.output.CliktConsole$print(kotlin.String, kotlin.Boolean)/text) to the user.

### Parameters

`text` - The text to display. May or may not contain a tailing newline.

`error` - If true, the [text](../-clikt-console/print.md#com.github.ajalt.clikt.output.CliktConsole$print(kotlin.String, kotlin.Boolean)/text) is an error message, and should be printed in an alternate stream or
format, if applicable.