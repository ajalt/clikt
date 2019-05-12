[clikt](../index.md) / [com.github.ajalt.clikt.parameters.types](index.md) / [choice](./choice.md)

# choice

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> RawArgument.choice(choices: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](choice.md#T)`>): `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`T`](choice.md#T)`, `[`T`](choice.md#T)`>`

Convert the argument based on a fixed set of values.

### Example:

``` kotlin
argument().choice(mapOf("foo" to 1, "bar" to 2))
```

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> RawArgument.choice(vararg choices: `[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](choice.md#T)`>): `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`T`](choice.md#T)`, `[`T`](choice.md#T)`>`

Convert the argument based on a fixed set of values.

### Example:

``` kotlin
argument().choice("foo" to 1, "bar" to 2)
```

`fun RawArgument.choice(vararg choices: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`

Restrict the argument to a fixed set of values.

### Example:

``` kotlin
argument().choice("foo", "bar")
```

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`RawOption`](../com.github.ajalt.clikt.parameters.options/-raw-option.md)`.choice(choices: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](choice.md#T)`>, metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = mvar(choices.keys)): `[`NullableOption`](../com.github.ajalt.clikt.parameters.options/-nullable-option.md)`<`[`T`](choice.md#T)`, `[`T`](choice.md#T)`>`

Convert the option based on a fixed set of values.

### Example:

``` kotlin
option().choice(mapOf("foo" to 1, "bar" to 2))
```

**See Also**

[com.github.ajalt.clikt.parameters.groups.groupChoice](../com.github.ajalt.clikt.parameters.groups/group-choice.md)

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`RawOption`](../com.github.ajalt.clikt.parameters.options/-raw-option.md)`.choice(vararg choices: `[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`T`](choice.md#T)`>, metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = mvar(choices.map { it.first })): `[`NullableOption`](../com.github.ajalt.clikt.parameters.options/-nullable-option.md)`<`[`T`](choice.md#T)`, `[`T`](choice.md#T)`>`

Convert the option based on a fixed set of values.

### Example:

``` kotlin
option().choice("foo" to 1, "bar" to 2)
```

**See Also**

[com.github.ajalt.clikt.parameters.groups.groupChoice](../com.github.ajalt.clikt.parameters.groups/group-choice.md)

`fun `[`RawOption`](../com.github.ajalt.clikt.parameters.options/-raw-option.md)`.choice(vararg choices: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = mvar(choices.asIterable())): `[`NullableOption`](../com.github.ajalt.clikt.parameters.options/-nullable-option.md)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`

Restrict the option to a fixed set of values.

### Example:

``` kotlin
option().choice("foo", "bar")
```

