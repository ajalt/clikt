[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [Context](index.md) / [findObject](./find-object.md)

# findObject

`inline fun <reified T> findObject(): `[`T`](find-object.md#T)`?`

Find the closest object of type [T](find-object.md#T)

`inline fun <reified T> findObject(defaultValue: () -> `[`T`](find-object.md#T)`): `[`T`](find-object.md#T)

Find the closest object of type [T](find-object.md#T), setting `this.`[obj](obj.md) if one is not found.

