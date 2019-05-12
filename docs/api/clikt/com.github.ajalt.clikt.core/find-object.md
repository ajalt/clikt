[clikt](../index.md) / [com.github.ajalt.clikt.core](index.md) / [findObject](./find-object.md)

# findObject

`inline fun <reified T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](-clikt-command/index.md)`.findObject(): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](-clikt-command/index.md)`, `[`T`](find-object.md#T)`?>`

Find the closest object of type [T](find-object.md#T), or null

`inline fun <reified T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](-clikt-command/index.md)`.findObject(crossinline default: () -> `[`T`](find-object.md#T)`): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](-clikt-command/index.md)`, `[`T`](find-object.md#T)`>`

Find the closest object of type [T](find-object.md#T), setting `context.obj` if one is not found.

