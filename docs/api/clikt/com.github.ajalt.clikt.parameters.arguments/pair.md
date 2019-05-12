[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [pair](./pair.md)

# pair

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`T`](pair.md#T)`, `[`T`](pair.md#T)`>.pair(): `[`ProcessedArgument`](-processed-argument/index.md)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`T`](pair.md#T)`, `[`T`](pair.md#T)`>, `[`T`](pair.md#T)`>`

Require exactly two values to this argument, and store them in a [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html).

This must be called after converting the value type, and before other transforms.

### Example:

``` kotlin
val arg: Pair<Int, Int> by argument().int().pair()
```

