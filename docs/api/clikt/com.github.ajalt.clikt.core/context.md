[clikt](../index.md) / [com.github.ajalt.clikt.core](index.md) / [context](./context.md)

# context

`fun <T : `[`CliktCommand`](-clikt-command/index.md)`> `[`T`](context.md#T)`.context(block: `[`Context.Builder`](-context/-builder/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`T`](context.md#T)

Configure this command's [Context](-context/index.md).

Context property values are normally inherited from the parent context, but you can override any of them
here.

