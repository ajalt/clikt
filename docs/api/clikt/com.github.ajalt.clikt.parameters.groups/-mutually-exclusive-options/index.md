[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [MutuallyExclusiveOptions](./index.md)

# MutuallyExclusiveOptions

`class MutuallyExclusiveOptions<OptT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, OutT> : `[`ParameterGroupDelegate`](../-parameter-group-delegate/index.md)`<`[`OutT`](index.md#OutT)`>`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `MutuallyExclusiveOptions(options: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`OptT`](index.md#OptT)`?>>, groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?, groupHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?, transformAll: (`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptT`](index.md#OptT)`>) -> `[`OutT`](index.md#OutT)`)` |

### Properties

| Name | Summary |
|---|---|
| [groupHelp](group-help.md) | `val groupHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A help message to display for this group. |
| [groupName](group-name.md) | `val groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group, or null if parameters in the group should not be separated from other parameters in the help output. |

### Functions

| Name | Summary |
|---|---|
| [copy](copy.md) | `fun <T> copy(transformAll: (`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptT`](index.md#OptT)`>) -> `[`T`](copy.md#T)`): `[`MutuallyExclusiveOptions`](./index.md)`<`[`OptT`](index.md#OptT)`, `[`T`](copy.md#T)`>` |
| [finalize](finalize.md) | `fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocationsByOption: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed and all options are validated to validate the group constraints. |
| [getValue](get-value.md) | `fun getValue(thisRef: `[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, property: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`OutT`](index.md#OutT) |
| [postValidate](post-validate.md) | `fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](../-parameter-group/finalize.md)d to perform validation of the final values. |
| [provideDelegate](provide-delegate.md) | `operator fun provideDelegate(thisRef: `[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, prop: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`OutT`](index.md#OutT)`>`<br>Implementations must call [CliktCommand.registerOptionGroup](../../com.github.ajalt.clikt.core/-clikt-command/register-option-group.md) |

### Extension Functions

| Name | Summary |
|---|---|
| [default](../default.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`MutuallyExclusiveOptions`](./index.md)`<`[`T`](../default.md#T)`, `[`T`](../default.md#T)`?>.default(value: `[`T`](../default.md#T)`): `[`MutuallyExclusiveOptions`](./index.md)`<`[`T`](../default.md#T)`, `[`T`](../default.md#T)`>`<br>If none of the options in a [mutuallyExclusiveOptions](../mutually-exclusive-options.md) group are given on the command line, us [value](../default.md#com.github.ajalt.clikt.parameters.groups$default(com.github.ajalt.clikt.parameters.groups.MutuallyExclusiveOptions((com.github.ajalt.clikt.parameters.groups.default.T, com.github.ajalt.clikt.parameters.groups.default.T)), com.github.ajalt.clikt.parameters.groups.default.T)/value) for the group. |
| [required](../required.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`MutuallyExclusiveOptions`](./index.md)`<`[`T`](../required.md#T)`, `[`T`](../required.md#T)`?>.required(): `[`MutuallyExclusiveOptions`](./index.md)`<`[`T`](../required.md#T)`, `[`T`](../required.md#T)`>`<br>Make a [mutuallyExclusiveOptions](../mutually-exclusive-options.md) group required. If none of the options in the group are given, a [UsageError](../../com.github.ajalt.clikt.core/-usage-error/index.md) is thrown. |
| [single](../single.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`MutuallyExclusiveOptions`](./index.md)`<`[`T`](../single.md#T)`, `[`T`](../single.md#T)`?>.single(): `[`MutuallyExclusiveOptions`](./index.md)`<`[`T`](../single.md#T)`, `[`T`](../single.md#T)`?>`<br>If more than one of the group's options are given on the command line, throw a [MutuallyExclusiveGroupException](../../com.github.ajalt.clikt.core/-mutually-exclusive-group-exception/index.md) |
