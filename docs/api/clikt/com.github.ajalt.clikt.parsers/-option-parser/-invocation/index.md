[clikt](../../../index.md) / [com.github.ajalt.clikt.parsers](../../index.md) / [OptionParser](../index.md) / [Invocation](./index.md)

# Invocation

`data class Invocation`

The input from a single instance of an option input.

### Parameters

`name` - The name that was used to invoke the option. May be empty if the value was not retrieved
from the command line (e.g. values from environment variables).

`values` - The values provided to the option. All instances passed to [Option.finalize](../../../com.github.ajalt.clikt.parameters.options/-option/finalize.md)
will have a size equal to [Option.nvalues](../../../com.github.ajalt.clikt.parameters.options/-option/nvalues.md).

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Invocation(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, values: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)`<br>The input from a single instance of an option input. |

### Properties

| Name | Summary |
|---|---|
| [name](name.md) | `val name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The name that was used to invoke the option. May be empty if the value was not retrieved from the command line (e.g. values from environment variables). |
| [values](values.md) | `val values: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The values provided to the option. All instances passed to [Option.finalize](../../../com.github.ajalt.clikt.parameters.options/-option/finalize.md) will have a size equal to [Option.nvalues](../../../com.github.ajalt.clikt.parameters.options/-option/nvalues.md). |
