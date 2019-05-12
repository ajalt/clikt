[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [aliases](./aliases.md)

# aliases

`open fun aliases(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>`

A list of command aliases.

If the user enters a command that matches the a key in this map, the command is replaced with the
corresponding value in in map. The aliases aren't recursive, so aliases won't be looked up again while
tokens from an existing alias are being parsed.

