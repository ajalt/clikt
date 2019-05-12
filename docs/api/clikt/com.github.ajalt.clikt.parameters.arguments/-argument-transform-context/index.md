[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.arguments](../index.md) / [ArgumentTransformContext](./index.md)

# ArgumentTransformContext

`class ArgumentTransformContext : `[`Argument`](../-argument/index.md)

A receiver for argument transformers.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `ArgumentTransformContext(argument: `[`Argument`](../-argument/index.md)`, context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`)`<br>A receiver for argument transformers. |

### Properties

| Name | Summary |
|---|---|
| [argument](argument.md) | `val argument: `[`Argument`](../-argument/index.md)<br>The argument that was invoked |
| [context](context.md) | `val context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md) |

### Functions

| Name | Summary |
|---|---|
| [fail](fail.md) | `fun fail(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Nothing`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)<br>Throw an exception indicating that usage was incorrect. |
| [message](message.md) | `fun message(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Issue a message that can be shown to the user |
| [require](require.md) | `fun require(value: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, lazyMessage: () -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = { "invalid value" }): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>If [value](require.md#com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext$require(kotlin.Boolean, kotlin.Function0((kotlin.String)))/value) is false, call [fail](fail.md) with the output of [lazyMessage](require.md#com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext$require(kotlin.Boolean, kotlin.Function0((kotlin.String)))/lazyMessage) |
