[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [ParameterGroup](./index.md)

# ParameterGroup

`interface ParameterGroup`

### Properties

| Name | Summary |
|---|---|
| [groupHelp](group-help.md) | `abstract val groupHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A help message to display for this group. |
| [groupName](group-name.md) | `abstract val groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group, or null if parameters in the group should not be separated from other parameters in the help output. |
| [parameterHelp](parameter-help.md) | `open val parameterHelp: `[`HelpFormatter.ParameterHelp.Group`](../../com.github.ajalt.clikt.output/-help-formatter/-parameter-help/-group/index.md)`?` |

### Functions

| Name | Summary |
|---|---|
| [finalize](finalize.md) | `abstract fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocationsByOption: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed and all options are validated to validate the group constraints. |
| [postValidate](post-validate.md) | `abstract fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](finalize.md)d to perform validation of the final values. |

### Inheritors

| Name | Summary |
|---|---|
| [OptionGroup](../-option-group/index.md) | `open class OptionGroup : `[`ParameterGroup`](./index.md)`, `[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)<br>A group of options that can be shown together in help output, or restricted to be [cooccurring](../cooccurring.md). |
| [ParameterGroupDelegate](../-parameter-group-delegate/index.md) | `interface ParameterGroupDelegate<out T> : `[`ParameterGroup`](./index.md)`, `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`T`](../-parameter-group-delegate/index.md#T)`>` |
