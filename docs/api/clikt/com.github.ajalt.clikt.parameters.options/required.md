[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [required](./required.md)

# required

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](required.md#EachT)`, `[`ValueT`](required.md#ValueT)`>.required(): `[`OptionWithValues`](-option-with-values/index.md)`<`[`EachT`](required.md#EachT)`, `[`EachT`](required.md#EachT)`, `[`ValueT`](required.md#ValueT)`>`

If the option is not called on the command line (and is not set in an envvar), throw a [MissingParameter](../com.github.ajalt.clikt.core/-missing-parameter/index.md).

This must be applied after all other transforms.

### Example:

``` kotlin
val opt: Pair<Int, Int> by option().int().pair().required()
```

