[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [defaultLazy](./default-lazy.md)

# defaultLazy

`inline fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](default-lazy.md#EachT)`, `[`ValueT`](default-lazy.md#ValueT)`>.defaultLazy(defaultForHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", crossinline value: () -> `[`EachT`](default-lazy.md#EachT)`): `[`OptionWithValues`](-option-with-values/index.md)`<`[`EachT`](default-lazy.md#EachT)`, `[`EachT`](default-lazy.md#EachT)`, `[`ValueT`](default-lazy.md#ValueT)`>`

If the option is not called on the command line (and is not set in an envvar), call the [value](default-lazy.md#com.github.ajalt.clikt.parameters.options$defaultLazy(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.defaultLazy.EachT, com.github.ajalt.clikt.parameters.options.defaultLazy.EachT, com.github.ajalt.clikt.parameters.options.defaultLazy.ValueT)), kotlin.String, kotlin.Function0((com.github.ajalt.clikt.parameters.options.defaultLazy.EachT)))/value) and use its
return value for the option.

This must be applied after all other transforms. If the option is given on the command line, [value](default-lazy.md#com.github.ajalt.clikt.parameters.options$defaultLazy(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.defaultLazy.EachT, com.github.ajalt.clikt.parameters.options.defaultLazy.EachT, com.github.ajalt.clikt.parameters.options.defaultLazy.ValueT)), kotlin.String, kotlin.Function0((com.github.ajalt.clikt.parameters.options.defaultLazy.EachT)))/value) will
not be called.

You can customize how the default is shown to the user with [defaultForHelp](default-lazy.md#com.github.ajalt.clikt.parameters.options$defaultLazy(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.defaultLazy.EachT, com.github.ajalt.clikt.parameters.options.defaultLazy.EachT, com.github.ajalt.clikt.parameters.options.defaultLazy.ValueT)), kotlin.String, kotlin.Function0((com.github.ajalt.clikt.parameters.options.defaultLazy.EachT)))/defaultForHelp). The default value
is an empty string, so if you have the help formatter configured to show values, you should set
this value manually.

### Example:

``` kotlin
val opt: Pair<Int, Int> by option().int().pair().defaultLazy { expensiveOperation() }
```

