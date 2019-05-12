[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [run](./run.md)

# run

`abstract fun run(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Perform actions after parsing is complete and this command is invoked.

This is called after command line parsing is complete. If this command is a subcommand, this will only
be called if the subcommand is invoked.

If one of this command's subcommands is invoked, this is called before the subcommand's arguments are
parsed.

