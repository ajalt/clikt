[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktError](./index.md)

# CliktError

`open class CliktError : `[`RuntimeException`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-runtime-exception/index.html)

An exception during command line processing that should be shown to the user.

If calling [CliktCommand.main](../-clikt-command/main.md), these exceptions will be caught and the appropriate info will be printed.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `CliktError(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, cause: `[`Exception`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html)`? = null)`<br>An exception during command line processing that should be shown to the user. |

### Inheritors

| Name | Summary |
|---|---|
| [PrintHelpMessage](../-print-help-message/index.md) | `class PrintHelpMessage : `[`CliktError`](./index.md)<br>An exception that indicates that the command's help should be printed. |
| [PrintMessage](../-print-message/index.md) | `class PrintMessage : `[`CliktError`](./index.md)<br>An exception that indicates that a message should be printed. |
| [UsageError](../-usage-error/index.md) | `open class UsageError : `[`CliktError`](./index.md)<br>An internal exception that signals a usage error. |
