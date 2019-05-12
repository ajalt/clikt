[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [parse](./parse.md)

# parse

`fun parse(argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, parentContext: `[`Context`](../-context/index.md)`? = null): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Parse the command line and throw an exception if parsing fails.

You should use [main](main.md) instead unless you want to handle output yourself.

`fun parse(argv: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, parentContext: `[`Context`](../-context/index.md)`? = null): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)