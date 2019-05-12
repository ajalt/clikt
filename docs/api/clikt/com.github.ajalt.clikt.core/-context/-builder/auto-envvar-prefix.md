[clikt](../../../index.md) / [com.github.ajalt.clikt.core](../../index.md) / [Context](../index.md) / [Builder](index.md) / [autoEnvvarPrefix](./auto-envvar-prefix.md)

# autoEnvvarPrefix

`var autoEnvvarPrefix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`

The prefix to add to inferred envvar names.

If null, the prefix is based on the parent's prefix, if there is one. If no command specifies, a
prefix, envvar lookup is disabled.

