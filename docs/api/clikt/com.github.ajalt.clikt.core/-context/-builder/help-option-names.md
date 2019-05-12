[clikt](../../../index.md) / [com.github.ajalt.clikt.core](../../index.md) / [Context](../index.md) / [Builder](index.md) / [helpOptionNames](./help-option-names.md)

# helpOptionNames

`var helpOptionNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`

The names to use for the help option.

If any names in the set conflict with other options, the conflicting name will not be used for the
help option. If the set is empty, or contains no unique names, no help option will be added.

