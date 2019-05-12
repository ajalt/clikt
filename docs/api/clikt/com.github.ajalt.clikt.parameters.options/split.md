[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [split](./split.md)

# split

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](split.md#EachT)`, `[`ValueT`](split.md#ValueT)`>.split(regex: `[`Regex`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/index.html)`): `[`OptionWithValues`](-option-with-values/index.md)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`ValueT`](split.md#ValueT)`>?, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`ValueT`](split.md#ValueT)`>, `[`ValueT`](split.md#ValueT)`>`

Change to option to take any number of values, separated by a [regex](split.md#com.github.ajalt.clikt.parameters.options$split(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.split.EachT, com.github.ajalt.clikt.parameters.options.split.EachT, com.github.ajalt.clikt.parameters.options.split.ValueT)), kotlin.text.Regex)/regex).

This must be called after converting the value type, and before other transforms.

### Example:

``` kotlin
val opt: List<Int>? by option().int().split(Regex(","))
```

Which can be called like this:

```
./program --opt 1,2,3
```

`fun <EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](split.md#EachT)`, `[`ValueT`](split.md#ValueT)`>.split(delimiter: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`OptionWithValues`](-option-with-values/index.md)`<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`ValueT`](split.md#ValueT)`>?, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`ValueT`](split.md#ValueT)`>, `[`ValueT`](split.md#ValueT)`>`

Change to option to take any number of values, separated by a string [delimiter](split.md#com.github.ajalt.clikt.parameters.options$split(com.github.ajalt.clikt.parameters.options.OptionWithValues((com.github.ajalt.clikt.parameters.options.split.EachT, com.github.ajalt.clikt.parameters.options.split.EachT, com.github.ajalt.clikt.parameters.options.split.ValueT)), kotlin.String)/delimiter).

This must be called after converting the value type, and before other transforms.

### Example:

``` kotlin
val opt: List<Int>? by option().int().split(Regex(","))
```

Which can be called like this:

```
./program --opt 1,2,3
```

