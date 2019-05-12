[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [optional](./optional.md)

# optional

`fun <AllT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`AllT`](optional.md#AllT)`, `[`ValueT`](optional.md#ValueT)`>.optional(): `[`ProcessedArgument`](-processed-argument/index.md)`<`[`AllT`](optional.md#AllT)`?, `[`ValueT`](optional.md#ValueT)`>`

Return null instead of throwing an error if no value is given.

This must be called after all other transforms.

### Example:

``` kotlin
val arg: Int? by argument().int().optional()
```

