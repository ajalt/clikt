[clikt](../../../../index.md) / [com.github.ajalt.clikt.output](../../../index.md) / [HelpFormatter](../../index.md) / [ParameterHelp](../index.md) / [Argument](./index.md)

# Argument

`data class Argument : `[`HelpFormatter.ParameterHelp`](../index.md)

### Parameters

`name` - The name / metavar for this argument

`help` - The arguments's description

`required` - True if this argument must be specified

`repeatable` - True if this argument takes an unlimited number of values

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Argument(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, required: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, repeatable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, tags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)` |

### Properties

| Name | Summary |
|---|---|
| [help](help.md) | `val help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The arguments's description |
| [name](name.md) | `val name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The name / metavar for this argument |
| [repeatable](repeatable.md) | `val repeatable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>True if this argument takes an unlimited number of values |
| [required](required.md) | `val required: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>True if this argument must be specified |
| [tags](tags.md) | `val tags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |
