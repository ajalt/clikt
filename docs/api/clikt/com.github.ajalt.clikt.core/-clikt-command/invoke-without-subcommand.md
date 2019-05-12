[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [invokeWithoutSubcommand](./invoke-without-subcommand.md)

# invokeWithoutSubcommand

`val invokeWithoutSubcommand: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)

Used when this command has subcommands, and this command is called
without a subcommand. If true, [run](run.md) will be called. By default, a [PrintHelpMessage](../-print-help-message/index.md) is thrown instead.

