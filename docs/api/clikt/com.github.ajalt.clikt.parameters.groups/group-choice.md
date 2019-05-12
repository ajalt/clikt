[clikt](../index.md) / [com.github.ajalt.clikt.parameters.groups](index.md) / [groupChoice](./group-choice.md)

# groupChoice

`fun <T : `[`OptionGroup`](-option-group/index.md)`> `[`RawOption`](../com.github.ajalt.clikt.parameters.options/-raw-option.md)`.groupChoice(choices: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](group-choice.md#T)`>): `[`ChoiceGroup`](-choice-group/index.md)`<`[`T`](group-choice.md#T)`, `[`T`](group-choice.md#T)`?>`

Convert the option to an option group based on a fixed set of values.

### Example:

``` kotlin
option().choice(mapOf("foo" to FooOptionGroup(), "bar" to BarOptionGroup()))
```

**See Also**

[com.github.ajalt.clikt.parameters.types.choice](../com.github.ajalt.clikt.parameters.types/choice.md)

`fun <T : `[`OptionGroup`](-option-group/index.md)`> `[`RawOption`](../com.github.ajalt.clikt.parameters.options/-raw-option.md)`.groupChoice(vararg choices: `[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](group-choice.md#T)`>): `[`ChoiceGroup`](-choice-group/index.md)`<`[`T`](group-choice.md#T)`, `[`T`](group-choice.md#T)`?>`

Convert the option to an option group based on a fixed set of values.

### Example:

``` kotlin
option().choice("foo" to FooOptionGroup(), "bar" to BarOptionGroup())
```

**See Also**

[com.github.ajalt.clikt.parameters.types.choice](../com.github.ajalt.clikt.parameters.types/choice.md)

