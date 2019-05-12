[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [Context](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`Context(parent: `[`Context`](index.md)`?, command: `[`CliktCommand`](../-clikt-command/index.md)`, allowInterspersedArgs: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, autoEnvvarPrefix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?, printExtraMessages: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, helpOptionNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, helpOptionMessage: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, helpFormatter: `[`HelpFormatter`](../../com.github.ajalt.clikt.output/-help-formatter/index.md)`, tokenTransformer: `[`Context`](index.md)`.(`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, console: `[`CliktConsole`](../../com.github.ajalt.clikt.output/-clikt-console/index.md)`)`

A object used to control command line parsing and pass data between commands.

A new Context instance is created for each command each time the command line is parsed.

