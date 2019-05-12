[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [pair](./pair.md)

# pair

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](pair.md#EachT)`, `[`ValueT`](pair.md#ValueT)`>.pair(): `[`NullableOption`](-nullable-option.md)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`ValueT`](pair.md#ValueT)`, `[`ValueT`](pair.md#ValueT)`>, `[`ValueT`](pair.md#ValueT)`>`

Change to option to take two values, held in a [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html).

This must be called after converting the value type, and before other transforms.

### Example:

``` kotlin
val opt: Pair<Int, Int>? by option().int().pair()
```

