[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktError](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`CliktError(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, cause: `[`Exception`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-exception/index.html)`? = null)`

An exception during command line processing that should be shown to the user.

If calling [CliktCommand.main](../-clikt-command/main.md), these exceptions will be caught and the appropriate info will be printed.

