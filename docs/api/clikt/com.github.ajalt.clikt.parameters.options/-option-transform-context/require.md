[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.options](../index.md) / [OptionTransformContext](index.md) / [require](./require.md)

# require

`inline fun require(value: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, lazyMessage: () -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = { "invalid value" }): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

If [value](require.md#com.github.ajalt.clikt.parameters.options.OptionTransformContext$require(kotlin.Boolean, kotlin.Function0((kotlin.String)))/value) is false, call [fail](fail.md) with the output of [lazyMessage](require.md#com.github.ajalt.clikt.parameters.options.OptionTransformContext$require(kotlin.Boolean, kotlin.Function0((kotlin.String)))/lazyMessage)

