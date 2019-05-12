[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [unique](./unique.md)

# unique

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`OptionWithValues`](-option-with-values/index.md)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`EachT`](unique.md#EachT)`>, `[`EachT`](unique.md#EachT)`, `[`ValueT`](unique.md#ValueT)`>.unique(): `[`OptionWithValues`](-option-with-values/index.md)`<`[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`EachT`](unique.md#EachT)`>, `[`EachT`](unique.md#EachT)`, `[`ValueT`](unique.md#ValueT)`>`

Make the [multiple](multiple.md) option return a unique set of calls

### Example:

``` kotlin
val opt: Set<Int> by option().int().multiple().unique()
```

