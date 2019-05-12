[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [defaultLazy](./default-lazy.md)

# defaultLazy

`inline fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`T`](default-lazy.md#T)`, `[`T`](default-lazy.md#T)`>.defaultLazy(crossinline value: () -> `[`T`](default-lazy.md#T)`): `[`ArgumentDelegate`](-argument-delegate/index.md)`<`[`T`](default-lazy.md#T)`>`

If the argument is not given, call [value](default-lazy.md#com.github.ajalt.clikt.parameters.arguments$defaultLazy(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.arguments.defaultLazy.T, )), kotlin.Function0((com.github.ajalt.clikt.parameters.arguments.defaultLazy.T)))/value) and use its return value instead of throwing an error.

This must be applied after all other transforms. If the argument is given on the command line, [value](default-lazy.md#com.github.ajalt.clikt.parameters.arguments$defaultLazy(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.arguments.defaultLazy.T, )), kotlin.Function0((com.github.ajalt.clikt.parameters.arguments.defaultLazy.T)))/value) will
not be called.

### Example:

``` kotlin
val arg: Pair<Int, Int> by argument().int().pair().defaultLazy { expensiveOperation() }
```

