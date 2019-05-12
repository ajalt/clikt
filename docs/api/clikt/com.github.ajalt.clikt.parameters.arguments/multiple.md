[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [multiple](./multiple.md)

# multiple

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`T`](multiple.md#T)`, `[`T`](multiple.md#T)`>.multiple(required: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`ProcessedArgument`](-processed-argument/index.md)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`T`](multiple.md#T)`>, `[`T`](multiple.md#T)`>`

Accept any number of values to this argument.

Only one argument in a command may use this function, and the command may not have subcommands. This must
be called after all other transforms.

### Example:

``` kotlin
val arg: List<Int> by argument().int().multiple()
```

