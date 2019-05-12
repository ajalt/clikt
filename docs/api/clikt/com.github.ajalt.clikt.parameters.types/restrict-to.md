[clikt](../index.md) / [com.github.ajalt.clikt.parameters.types](index.md) / [restrictTo](./restrict-to.md)

# restrictTo

`fun <T> `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`>.restrictTo(min: `[`T`](restrict-to.md#T)`? = null, max: `[`T`](restrict-to.md#T)`? = null, clamp: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`> where T : `[`Number`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-number/index.html)`, T : `[`Comparable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-comparable/index.html)`<`[`T`](restrict-to.md#T)`>`

Restrict the argument values to fit into a range.

By default, conversion fails if the value is outside the range, but if [clamp](restrict-to.md#com.github.ajalt.clikt.parameters.types$restrictTo(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.types.restrictTo.T, )), com.github.ajalt.clikt.parameters.types.restrictTo.T, com.github.ajalt.clikt.parameters.types.restrictTo.T, kotlin.Boolean)/clamp) is true, the value will be
silently clamped to fit in the range.

### Example:

``` kotlin
argument().int().restrictTo(max=10, clamp=true)
```

`fun <T> `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`>.restrictTo(range: `[`ClosedRange`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-closed-range/index.html)`<`[`T`](restrict-to.md#T)`>, clamp: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`> where T : `[`Number`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-number/index.html)`, T : `[`Comparable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-comparable/index.html)`<`[`T`](restrict-to.md#T)`>`

Restrict the argument values to fit into a range.

By default, conversion fails if the value is outside the range, but if [clamp](restrict-to.md#com.github.ajalt.clikt.parameters.types$restrictTo(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.types.restrictTo.T, )), kotlin.ranges.ClosedRange((com.github.ajalt.clikt.parameters.types.restrictTo.T)), kotlin.Boolean)/clamp) is true, the value will be
silently clamped to fit in the range.

### Example:

``` kotlin
argument().int().restrictTo(1..10, clamp=true)
```

`fun <T> `[`OptionWithValues`](../com.github.ajalt.clikt.parameters.options/-option-with-values/index.md)`<`[`T`](restrict-to.md#T)`?, `[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`>.restrictTo(min: `[`T`](restrict-to.md#T)`? = null, max: `[`T`](restrict-to.md#T)`? = null, clamp: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`OptionWithValues`](../com.github.ajalt.clikt.parameters.options/-option-with-values/index.md)`<`[`T`](restrict-to.md#T)`?, `[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`> where T : `[`Number`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-number/index.html)`, T : `[`Comparable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-comparable/index.html)`<`[`T`](restrict-to.md#T)`>`

Restrict the option values to fit into a range.

By default, conversion fails if the value is outside the range, but if [clamp](restrict-to.md#com.github.ajalt.clikt.parameters.types$restrictTo(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.types.restrictTo.T, com.github.ajalt.clikt.parameters.types.restrictTo.T, )), com.github.ajalt.clikt.parameters.types.restrictTo.T, com.github.ajalt.clikt.parameters.types.restrictTo.T, kotlin.Boolean)/clamp) is true, the value will be
silently clamped to fit in the range.

### Example:

``` kotlin
option().int().restrictTo(max=10, clamp=true)
```

`fun <T> `[`OptionWithValues`](../com.github.ajalt.clikt.parameters.options/-option-with-values/index.md)`<`[`T`](restrict-to.md#T)`?, `[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`>.restrictTo(range: `[`ClosedRange`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/-closed-range/index.html)`<`[`T`](restrict-to.md#T)`>, clamp: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`OptionWithValues`](../com.github.ajalt.clikt.parameters.options/-option-with-values/index.md)`<`[`T`](restrict-to.md#T)`?, `[`T`](restrict-to.md#T)`, `[`T`](restrict-to.md#T)`> where T : `[`Number`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-number/index.html)`, T : `[`Comparable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-comparable/index.html)`<`[`T`](restrict-to.md#T)`>`

Restrict the option values to fit into a range.

By default, conversion fails if the value is outside the range, but if [clamp](restrict-to.md#com.github.ajalt.clikt.parameters.types$restrictTo(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.types.restrictTo.T, com.github.ajalt.clikt.parameters.types.restrictTo.T, )), kotlin.ranges.ClosedRange((com.github.ajalt.clikt.parameters.types.restrictTo.T)), kotlin.Boolean)/clamp) is true, the value will be
silently clamped to fit in the range.

### Example:

``` kotlin
option().int().restrictTo(1..10, clamp=true)
```

