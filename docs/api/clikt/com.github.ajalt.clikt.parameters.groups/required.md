[clikt](../index.md) / [com.github.ajalt.clikt.parameters.groups](index.md) / [required](./required.md)

# required

`fun <T : `[`OptionGroup`](-option-group/index.md)`> `[`ChoiceGroup`](-choice-group/index.md)`<`[`T`](required.md#T)`, `[`T`](required.md#T)`?>.required(): `[`ChoiceGroup`](-choice-group/index.md)`<`[`T`](required.md#T)`, `[`T`](required.md#T)`>`

If a [groupChoice](group-choice.md) option is not called on the command line, throw a [MissingParameter](../com.github.ajalt.clikt.core/-missing-parameter/index.md) exception.

### Example:

``` kotlin
option().choice("foo" to FooOptionGroup(), "bar" to BarOptionGroup()).required()
```

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`MutuallyExclusiveOptions`](-mutually-exclusive-options/index.md)`<`[`T`](required.md#T)`, `[`T`](required.md#T)`?>.required(): `[`MutuallyExclusiveOptions`](-mutually-exclusive-options/index.md)`<`[`T`](required.md#T)`, `[`T`](required.md#T)`>`

Make a [mutuallyExclusiveOptions](mutually-exclusive-options.md) group required. If none of the options in the group are given,
a [UsageError](../com.github.ajalt.clikt.core/-usage-error/index.md) is thrown.

