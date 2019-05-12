[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.options](../index.md) / [OptionDelegate](./index.md)

# OptionDelegate

`interface OptionDelegate<T> : `[`GroupableOption`](../../com.github.ajalt.clikt.core/-groupable-option/index.md)`, `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`, `[`T`](index.md#T)`>`

An option that functions as a property delegate

### Properties

| Name | Summary |
|---|---|
| [value](value.md) | `abstract val value: `[`T`](index.md#T)<br>The value for this option. |

### Inherited Properties

| Name | Summary |
|---|---|
| [groupName](../../com.github.ajalt.clikt.core/-groupable-option/group-name.md) | `abstract var groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group, or null if this option should not be grouped in the help output. |
| [parameterGroup](../../com.github.ajalt.clikt.core/-groupable-option/parameter-group.md) | `abstract var parameterGroup: `[`ParameterGroup`](../../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md)`?`<br>The group that this option belongs to, or null. Set by the group. |

### Functions

| Name | Summary |
|---|---|
| [getValue](get-value.md) | `open fun getValue(thisRef: `[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`, property: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`T`](index.md#T) |
| [provideDelegate](provide-delegate.md) | `abstract operator fun provideDelegate(thisRef: `[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`, prop: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`, `[`T`](index.md#T)`>`<br>Implementations must call [ParameterHolder.registerOption](../../com.github.ajalt.clikt.core/-parameter-holder/register-option.md) |

### Inheritors

| Name | Summary |
|---|---|
| [FlagOption](../-flag-option/index.md) | `class FlagOption<T> : `[`OptionDelegate`](./index.md)`<`[`T`](../-flag-option/index.md#T)`>`<br>An [Option](../-option/index.md) that has no values. |
| [OptionWithValues](../-option-with-values/index.md) | `class OptionWithValues<AllT, EachT, ValueT> : `[`OptionDelegate`](./index.md)`<`[`AllT`](../-option-with-values/index.md#AllT)`>, `[`GroupableOption`](../../com.github.ajalt.clikt.core/-groupable-option/index.md)<br>An [Option](../-option/index.md) that takes one or more values. |
