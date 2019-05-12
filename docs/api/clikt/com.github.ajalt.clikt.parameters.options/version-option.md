[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [versionOption](./version-option.md)

# versionOption

`inline fun <T : `[`CliktCommand`](../com.github.ajalt.clikt.core/-clikt-command/index.md)`> `[`T`](version-option.md#T)`.versionOption(version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "Show the version and exit", names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = setOf("--version"), crossinline message: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = { "$commandName version $it" }): `[`T`](version-option.md#T)

Add an eager option to this command that, when invoked, prints a version message and exits.

