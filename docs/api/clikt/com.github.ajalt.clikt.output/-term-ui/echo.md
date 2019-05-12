[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [TermUi](index.md) / [echo](./echo.md)

# echo

`fun echo(message: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?, trailingNewline: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, err: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, console: `[`CliktConsole`](../-clikt-console/index.md)` = defaultCliktConsole(), lineSeparator: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = console.lineSeparator): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Print the [message](echo.md#com.github.ajalt.clikt.output.TermUi$echo(kotlin.Any, kotlin.Boolean, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.String)/message) to the screen.

This is similar to [print](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/print.html) or [println](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/println.html), but converts newlines to the system line separator.

### Parameters

`message` - The message to print.

`trailingNewline` - If true, behave like [println](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/println.html), otherwise behave like [print](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/print.html)

`err` - If true, print to stderr instead of stdout

`console` - The console to echo to

`lineSeparator` - The line separator to use, defaults to the [console](echo.md#com.github.ajalt.clikt.output.TermUi$echo(kotlin.Any, kotlin.Boolean, kotlin.Boolean, com.github.ajalt.clikt.output.CliktConsole, kotlin.String)/console)'s `lineSeparator`