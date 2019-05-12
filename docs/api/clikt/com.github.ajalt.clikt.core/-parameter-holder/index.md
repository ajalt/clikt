[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [ParameterHolder](./index.md)

# ParameterHolder

`interface ParameterHolder`

### Functions

| Name | Summary |
|---|---|
| [registerOption](register-option.md) | `abstract fun registerOption(option: `[`GroupableOption`](../-groupable-option/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Register an option with this command or group. |

### Extension Functions

| Name | Summary |
|---|---|
| [mutuallyExclusiveOptions](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ParameterHolder`](./index.md)`.mutuallyExclusiveOptions(option1: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>, option2: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>, vararg options: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`MutuallyExclusiveOptions`](../../com.github.ajalt.clikt.parameters.groups/-mutually-exclusive-options/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`, `[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>`<br>Declare a set of two or more mutually exclusive options. |
| [option](../../com.github.ajalt.clikt.parameters.options/option.md) | `fun `[`ParameterHolder`](./index.md)`.option(vararg names: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, envvar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, envvarSplit: `[`Regex`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/index.html)`? = null, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap()): `[`RawOption`](../../com.github.ajalt.clikt.parameters.options/-raw-option.md)<br>Create a property delegate option. |

### Inheritors

| Name | Summary |
|---|---|
| [CliktCommand](../-clikt-command/index.md) | `abstract class CliktCommand : `[`ParameterHolder`](./index.md)<br>The [CliktCommand](../-clikt-command/index.md) is the core of command line interfaces in Clikt. |
| [OptionGroup](../../com.github.ajalt.clikt.parameters.groups/-option-group/index.md) | `open class OptionGroup : `[`ParameterGroup`](../../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md)`, `[`ParameterHolder`](./index.md)<br>A group of options that can be shown together in help output, or restricted to be [cooccurring](../../com.github.ajalt.clikt.parameters.groups/cooccurring.md). |
