[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [multiple](./multiple.md)

# multiple

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](multiple.md#EachT)`, `[`ValueT`](multiple.md#ValueT)`>.multiple(default: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`EachT`](multiple.md#EachT)`> = emptyList()): `[`OptionWithValues`](-option-with-values/index.md)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`EachT`](multiple.md#EachT)`>, `[`EachT`](multiple.md#EachT)`, `[`ValueT`](multiple.md#ValueT)`>`

Make the option return a list of calls; each item in the list is the value of one call.

If the option is never called, the list will be empty. This must be applied after all other transforms.

### Example:

``` kotlin
val opt: List<Pair<Int, Int>> by option().int().pair().multiple()
```

### Parameters

`default` - The value to use if the option is not supplied. Defaults to an empty list.