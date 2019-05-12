[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.options](../index.md) / [OptionTransformContext](./index.md)

# OptionTransformContext

`class OptionTransformContext : `[`Option`](../-option/index.md)

A receiver for options transformers.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `OptionTransformContext(option: `[`Option`](../-option/index.md)`, context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`)`<br>A receiver for options transformers. |

### Properties

| Name | Summary |
|---|---|
| [context](context.md) | `val context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md) |
| [option](option.md) | `val option: `[`Option`](../-option/index.md)<br>The option that was invoked |

### Functions

| Name | Summary |
|---|---|
| [fail](fail.md) | `fun fail(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Nothing`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)<br>Throw an exception indicating that usage was incorrect. |
| [message](message.md) | `fun message(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Issue a message that can be shown to the user |
| [require](require.md) | `fun require(value: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, lazyMessage: () -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = { "invalid value" }): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>If [value](require.md#com.github.ajalt.clikt.parameters.options.OptionTransformContext$require(kotlin.Boolean, kotlin.Function0((kotlin.String)))/value) is false, call [fail](fail.md) with the output of [lazyMessage](require.md#com.github.ajalt.clikt.parameters.options.OptionTransformContext$require(kotlin.Boolean, kotlin.Function0((kotlin.String)))/lazyMessage) |
