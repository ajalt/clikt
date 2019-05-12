[clikt](../../../../index.md) / [com.github.ajalt.clikt.output](../../../index.md) / [HelpFormatter](../../index.md) / [ParameterHelp](../index.md) / [Option](./index.md)

# Option

`data class Option : `[`HelpFormatter.ParameterHelp`](../index.md)

### Parameters

`names` - The names that can be used to invoke this option

`secondaryNames` - Secondary names that can be used to e.g. disable the option

`metavar` - The metavar to display for the option if it takes values

`help` - The option's description

`nvalues` - The number of values that this option takes

`tags` - Any extra tags to display with the help message for this option

`groupName` - The name of the group this option belongs to, if there is one and its name should be shown in the help message

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Option(names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, secondaryNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, tags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?)` |

### Properties

| Name | Summary |
|---|---|
| [groupName](group-name.md) | `val groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group this option belongs to, if there is one and its name should be shown in the help message |
| [help](help.md) | `val help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The option's description |
| [metavar](metavar.md) | `val metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The metavar to display for the option if it takes values |
| [names](names.md) | `val names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The names that can be used to invoke this option |
| [nvalues](nvalues.md) | `val nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The number of values that this option takes |
| [secondaryNames](secondary-names.md) | `val secondaryNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Secondary names that can be used to e.g. disable the option |
| [tags](tags.md) | `val tags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Any extra tags to display with the help message for this option |
