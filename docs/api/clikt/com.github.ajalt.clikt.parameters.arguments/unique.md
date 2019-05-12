[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [unique](./unique.md)

# unique

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`T`](unique.md#T)`>, `[`T`](unique.md#T)`>.unique(): `[`ProcessedArgument`](-processed-argument/index.md)`<`[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`T`](unique.md#T)`>, `[`T`](unique.md#T)`>`

Only store unique values for this argument

### Example:

```
val arg: Set<Int> by argument().int().multiple().unique()
```

