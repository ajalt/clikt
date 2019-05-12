[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [CoOccurringOptionGroup](./index.md)

# CoOccurringOptionGroup

`class CoOccurringOptionGroup<GroupT : `[`OptionGroup`](../-option-group/index.md)`, OutT> : `[`ParameterGroupDelegate`](../-parameter-group-delegate/index.md)`<`[`OutT`](index.md#OutT)`>`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `CoOccurringOptionGroup(group: `[`GroupT`](index.md#GroupT)`, transform: (occurred: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`?, group: `[`GroupT`](index.md#GroupT)`, context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`) -> `[`OutT`](index.md#OutT)`)` |

### Properties

| Name | Summary |
|---|---|
| [groupHelp](group-help.md) | `val groupHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A help message to display for this group. |
| [groupName](group-name.md) | `val groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group, or null if parameters in the group should not be separated from other parameters in the help output. |

### Functions

| Name | Summary |
|---|---|
| [finalize](finalize.md) | `fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocationsByOption: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed and all options are validated to validate the group constraints. |
| [getValue](get-value.md) | `fun getValue(thisRef: `[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, property: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`OutT`](index.md#OutT) |
| [postValidate](post-validate.md) | `fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](../-parameter-group/finalize.md)d to perform validation of the final values. |
| [provideDelegate](provide-delegate.md) | `fun provideDelegate(thisRef: `[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, prop: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`OutT`](index.md#OutT)`>`<br>Implementations must call [CliktCommand.registerOptionGroup](../../com.github.ajalt.clikt.core/-clikt-command/register-option-group.md) |
