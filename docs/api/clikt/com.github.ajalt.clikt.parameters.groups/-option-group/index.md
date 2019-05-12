[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [OptionGroup](./index.md)

# OptionGroup

`open class OptionGroup : `[`ParameterGroup`](../-parameter-group/index.md)`, `[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)

A group of options that can be shown together in help output, or restricted to be [cooccurring](../cooccurring.md).

Declare a subclass with option delegate properties, then use an instance of your subclass is a
delegate property in your command with [provideDelegate](../provide-delegate.md).

### Example:

``` kotlin
class UserOptions : OptionGroup(name = "User Options", help = "Options controlling the user") {
  val name by option()
  val age by option().int()
}

class Tool : CliktCommand() {
  val userOptions by UserOptions()
}
```

### Note:

If you're using IntelliJ, bug KT-31319 prevents [provideDelegate](../provide-delegate.md) from being imported
automatically, so until that's fixed, you'll need to add this import manually:

`import com.github.ajalt.clikt.parameters.groups.provideDelegate`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `OptionGroup(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null)`<br>A group of options that can be shown together in help output, or restricted to be [cooccurring](../cooccurring.md). |

### Properties

| Name | Summary |
|---|---|
| [groupHelp](group-help.md) | `open val groupHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A help message to display for this group. |
| [groupName](group-name.md) | `open val groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group, or null if parameters in the group should not be separated from other parameters in the help output. |

### Inherited Properties

| Name | Summary |
|---|---|
| [parameterHelp](../-parameter-group/parameter-help.md) | `open val parameterHelp: `[`HelpFormatter.ParameterHelp.Group`](../../com.github.ajalt.clikt.output/-help-formatter/-parameter-help/-group/index.md)`?` |

### Functions

| Name | Summary |
|---|---|
| [finalize](finalize.md) | `open fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocationsByOption: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed and all options are validated to validate the group constraints. |
| [postValidate](post-validate.md) | `open fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](../-parameter-group/finalize.md)d to perform validation of the final values. |
| [registerOption](register-option.md) | `open fun registerOption(option: `[`GroupableOption`](../../com.github.ajalt.clikt.core/-groupable-option/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Register an option with this command or group. |

### Extension Functions

| Name | Summary |
|---|---|
| [cooccurring](../cooccurring.md) | `fun <T : `[`OptionGroup`](./index.md)`> `[`T`](../cooccurring.md#T)`.cooccurring(): `[`CoOccurringOptionGroup`](../-co-occurring-option-group/index.md)`<`[`T`](../cooccurring.md#T)`, `[`T`](../cooccurring.md#T)`?>`<br>Make this group a co-occurring group. |
| [mutuallyExclusiveOptions](../mutually-exclusive-options.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`.mutuallyExclusiveOptions(option1: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../mutually-exclusive-options.md#T)`?>, option2: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../mutually-exclusive-options.md#T)`?>, vararg options: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../mutually-exclusive-options.md#T)`?>, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`MutuallyExclusiveOptions`](../-mutually-exclusive-options/index.md)`<`[`T`](../mutually-exclusive-options.md#T)`, `[`T`](../mutually-exclusive-options.md#T)`?>`<br>Declare a set of two or more mutually exclusive options. |
| [option](../../com.github.ajalt.clikt.parameters.options/option.md) | `fun `[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`.option(vararg names: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, envvar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, envvarSplit: `[`Regex`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/index.html)`? = null, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap()): `[`RawOption`](../../com.github.ajalt.clikt.parameters.options/-raw-option.md)<br>Create a property delegate option. |
| [provideDelegate](../provide-delegate.md) | `operator fun <T : `[`OptionGroup`](./index.md)`> `[`T`](../provide-delegate.md#T)`.provideDelegate(thisRef: `[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, prop: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`T`](../provide-delegate.md#T)`>` |
