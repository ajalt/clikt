[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [main](./main.md)

# main

`fun main(argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Parse the command line and print helpful output if any errors occur.

This function calls [parse](parse.md) and catches and [CliktError](../-clikt-error/index.md)s that are thrown. Other error are allowed to
pass through.

`fun main(argv: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)