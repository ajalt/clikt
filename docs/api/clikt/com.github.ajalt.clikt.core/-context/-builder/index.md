[clikt](../../../index.md) / [com.github.ajalt.clikt.core](../../index.md) / [Context](../index.md) / [Builder](./index.md)

# Builder

`class Builder`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Builder(command: `[`CliktCommand`](../../-clikt-command/index.md)`, parent: `[`Context`](../index.md)`? = null)` |

### Properties

| Name | Summary |
|---|---|
| [allowInterspersedArgs](allow-interspersed-args.md) | `var allowInterspersedArgs: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If false, options and arguments cannot be mixed; the first time an argument is encountered, all remaining tokens are parsed as arguments. |
| [autoEnvvarPrefix](auto-envvar-prefix.md) | `var autoEnvvarPrefix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The prefix to add to inferred envvar names. |
| [console](console.md) | `var console: `[`CliktConsole`](../../../com.github.ajalt.clikt.output/-clikt-console/index.md)<br>The console that will handle reading and writing text. |
| [helpFormatter](help-formatter.md) | `var helpFormatter: `[`HelpFormatter`](../../../com.github.ajalt.clikt.output/-help-formatter/index.md)<br>The help formatter for this command |
| [helpOptionMessage](help-option-message.md) | `var helpOptionMessage: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The description of the help option. |
| [helpOptionNames](help-option-names.md) | `var helpOptionNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The names to use for the help option. |
| [printExtraMessages](print-extra-messages.md) | `var printExtraMessages: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Set this to false to prevent extra messages from being printed automatically. |
| [tokenTransformer](token-transformer.md) | `var tokenTransformer: `[`Context`](../index.md)`.(`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>An optional transformation function that is called to transform command line |
