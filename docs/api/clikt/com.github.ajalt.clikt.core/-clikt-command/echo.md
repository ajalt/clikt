[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [echo](./echo.md)

# echo

`protected fun echo(message: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?, trailingNewline: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, err: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, lineSeparator: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = context.console.lineSeparator): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Print the [message](echo.md#com.github.ajalt.clikt.core.CliktCommand$echo(kotlin.Any, kotlin.Boolean, kotlin.Boolean, kotlin.String)/message) to the screen.

This is similar to [print](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/print.html) or [println](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/println.html), but converts newlines to the system line separator.

This is equivalent to calling [TermUi.echo](../../com.github.ajalt.clikt.output/-term-ui/echo.md) with the console from the current context.

### Parameters

`message` - The message to print.

`trailingNewline` - If true, behave like [println](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/println.html), otherwise behave like [print](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/print.html)

`err` - If true, print to stderr instead of stdout