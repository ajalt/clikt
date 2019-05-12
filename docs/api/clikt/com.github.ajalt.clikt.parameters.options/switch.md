[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [switch](./switch.md)

# switch

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`RawOption`](-raw-option.md)`.switch(choices: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](switch.md#T)`>): `[`FlagOption`](-flag-option/index.md)`<`[`T`](switch.md#T)`?>`
`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`RawOption`](-raw-option.md)`.switch(vararg choices: `[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](switch.md#T)`>): `[`FlagOption`](-flag-option/index.md)`<`[`T`](switch.md#T)`?>`

Turn an option into a set of flags that each map to a value.

