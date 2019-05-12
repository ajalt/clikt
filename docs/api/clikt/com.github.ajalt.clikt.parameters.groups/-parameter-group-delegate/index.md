[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [ParameterGroupDelegate](./index.md)

# ParameterGroupDelegate

`interface ParameterGroupDelegate<out T> : `[`ParameterGroup`](../-parameter-group/index.md)`, `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`T`](index.md#T)`>`

### Inherited Properties

| Name | Summary |
|---|---|
| [groupHelp](../-parameter-group/group-help.md) | `abstract val groupHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A help message to display for this group. |
| [groupName](../-parameter-group/group-name.md) | `abstract val groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group, or null if parameters in the group should not be separated from other parameters in the help output. |
| [parameterHelp](../-parameter-group/parameter-help.md) | `open val parameterHelp: `[`HelpFormatter.ParameterHelp.Group`](../../com.github.ajalt.clikt.output/-help-formatter/-parameter-help/-group/index.md)`?` |

### Functions

| Name | Summary |
|---|---|
| [provideDelegate](provide-delegate.md) | `abstract operator fun provideDelegate(thisRef: `[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, prop: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`T`](index.md#T)`>`<br>Implementations must call [CliktCommand.registerOptionGroup](../../com.github.ajalt.clikt.core/-clikt-command/register-option-group.md) |

### Inherited Functions

| Name | Summary |
|---|---|
| [finalize](../-parameter-group/finalize.md) | `abstract fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocationsByOption: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed and all options are validated to validate the group constraints. |
| [postValidate](../-parameter-group/post-validate.md) | `abstract fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](../-parameter-group/finalize.md)d to perform validation of the final values. |

### Inheritors

| Name | Summary |
|---|---|
| [ChoiceGroup](../-choice-group/index.md) | `class ChoiceGroup<GroupT : `[`OptionGroup`](../-option-group/index.md)`, OutT> : `[`ParameterGroupDelegate`](./index.md)`<`[`OutT`](../-choice-group/index.md#OutT)`>` |
| [CoOccurringOptionGroup](../-co-occurring-option-group/index.md) | `class CoOccurringOptionGroup<GroupT : `[`OptionGroup`](../-option-group/index.md)`, OutT> : `[`ParameterGroupDelegate`](./index.md)`<`[`OutT`](../-co-occurring-option-group/index.md#OutT)`>` |
| [MutuallyExclusiveOptions](../-mutually-exclusive-options/index.md) | `class MutuallyExclusiveOptions<OptT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, OutT> : `[`ParameterGroupDelegate`](./index.md)`<`[`OutT`](../-mutually-exclusive-options/index.md#OutT)`>` |
