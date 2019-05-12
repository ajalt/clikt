[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [Context](index.md) / [tokenTransformer](./token-transformer.md)

# tokenTransformer

`val tokenTransformer: `[`Context`](index.md)`.(`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)

An optional transformation function that is called to transform command line
tokens (options and commands) before parsing. This can be used to implement e.g. case insensitive
behavior.

### Property

`tokenTransformer` - An optional transformation function that is called to transform command line
tokens (options and commands) before parsing. This can be used to implement e.g. case insensitive
behavior.