[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [Abort](./index.md)

# Abort

`class Abort : `[`RuntimeException`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-runtime-exception/index.html)

An internal error that signals Clikt to abort.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Abort(error: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true)`<br>An internal error that signals Clikt to abort. |

### Properties

| Name | Summary |
|---|---|
| [error](error.md) | `val error: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If true, print "Aborted" and exit with an error code. Otherwise, exit with no error code. |
