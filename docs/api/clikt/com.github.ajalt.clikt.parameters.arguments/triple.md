[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [triple](./triple.md)

# triple

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`T`](triple.md#T)`, `[`T`](triple.md#T)`>.triple(): `[`ProcessedArgument`](-processed-argument/index.md)`<`[`Triple`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html)`<`[`T`](triple.md#T)`, `[`T`](triple.md#T)`, `[`T`](triple.md#T)`>, `[`T`](triple.md#T)`>`

Require exactly three values to this argument, and store them in a [Triple](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html)

This must be called after converting the value type, and before other transforms.

### Example:

``` kotlin
val arg: Triple<Int, Int, Int> by argument().int().triple()
```

