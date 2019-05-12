[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [default](./default.md)

# default

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`T`](default.md#T)`, `[`T`](default.md#T)`>.default(value: `[`T`](default.md#T)`): `[`ArgumentDelegate`](-argument-delegate/index.md)`<`[`T`](default.md#T)`>`

If the argument is not given, use [value](default.md#com.github.ajalt.clikt.parameters.arguments$default(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.arguments.default.T, )), com.github.ajalt.clikt.parameters.arguments.default.T)/value) instead of throwing an error.

This must be applied after all other transforms.

### Example:

``` kotlin
val arg: Pair<Int, Int> by argument().int().pair().default(1 to 2)
```

