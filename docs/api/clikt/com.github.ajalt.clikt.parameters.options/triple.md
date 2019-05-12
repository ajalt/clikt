[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [triple](./triple.md)

# triple

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](triple.md#EachT)`, `[`ValueT`](triple.md#ValueT)`>.triple(): `[`NullableOption`](-nullable-option.md)`<`[`Triple`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html)`<`[`ValueT`](triple.md#ValueT)`, `[`ValueT`](triple.md#ValueT)`, `[`ValueT`](triple.md#ValueT)`>, `[`ValueT`](triple.md#ValueT)`>`

Change to option to take three values, held in a [Triple](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-triple/index.html).

This must be called after converting the value type, and before other transforms.

### Example:

``` kotlin
val opt: Triple<Int, Int, Int>? by option().int().triple()
```

