[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [default](./default.md)

# default

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`FlagOption`](-flag-option/index.md)`<`[`T`](default.md#T)`?>.default(value: `[`T`](default.md#T)`): `[`FlagOption`](-flag-option/index.md)`<`[`T`](default.md#T)`>`

Set a default value for a option.

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](default.md#EachT)`, `[`ValueT`](default.md#ValueT)`>.default(value: `[`EachT`](default.md#EachT)`, defaultForHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = value.toString()): `[`OptionWithValues`](-option-with-values/index.md)`<`[`EachT`](default.md#EachT)`, `[`EachT`](default.md#EachT)`, `[`ValueT`](default.md#ValueT)`>`

If the option is not called on the command line (and is not set in an envvar), use [value](default.md#com.github.ajalt.clikt.parameters.options$default(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.default.EachT, com.github.ajalt.clikt.parameters.options.default.EachT, com.github.ajalt.clikt.parameters.options.default.ValueT)), com.github.ajalt.clikt.parameters.options.default.EachT, kotlin.String)/value) for the option.

This must be applied after all other transforms.

You can customize how the default is shown to the user with [defaultForHelp](default.md#com.github.ajalt.clikt.parameters.options$default(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.default.EachT, com.github.ajalt.clikt.parameters.options.default.EachT, com.github.ajalt.clikt.parameters.options.default.ValueT)), com.github.ajalt.clikt.parameters.options.default.EachT, kotlin.String)/defaultForHelp).

### Example:

``` kotlin
val opt: Pair<Int, Int> by option().int().pair().default(1 to 2)
```

