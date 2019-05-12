[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [deprecated](./deprecated.md)

# deprecated

`fun <T> `[`FlagOption`](-flag-option/index.md)`<`[`T`](deprecated.md#T)`>.deprecated(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = "", tagName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = "deprecated", tagValue: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", error: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`OptionDelegate`](-option-delegate/index.md)`<`[`T`](deprecated.md#T)`>`

Mark this option as deprecated in the help output.

By default, a tag is added to the help message and a warning is printed if the option is used.

This should be called after any validation.

### Parameters

`message` - The message to show in the warning or error. If null, no warning is issued.

`tagName` - The tag to add to the help message

`tagValue` - An extra message to add to the tag

`error` - If true, when the option is invoked, a [CliktError](../com.github.ajalt.clikt.core/-clikt-error/index.md) is raised immediately instead of issuing a warning.`fun <AllT, EachT, ValueT> `[`OptionWithValues`](-option-with-values/index.md)`<`[`AllT`](deprecated.md#AllT)`, `[`EachT`](deprecated.md#EachT)`, `[`ValueT`](deprecated.md#ValueT)`>.deprecated(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = "", tagName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = "deprecated", tagValue: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", error: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`OptionDelegate`](-option-delegate/index.md)`<`[`AllT`](deprecated.md#AllT)`>`

Mark this option as deprecated in the help output.

By default, a tag is added to the help message and a warning is printed if the option is used.

This should be called after any conversion and validation.

### Example:

``` kotlin
val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
   .deprecated("WARNING: --opt is deprecated, use --new-opt instead")
```

### Parameters

`message` - The message to show in the warning or error. If null, no warning is issued.

`tagName` - The tag to add to the help message

`tagValue` - An extra message to add to the tag

`error` - If true, when the option is invoked, a [CliktError](../com.github.ajalt.clikt.core/-clikt-error/index.md) is raised immediately instead of issuing a warning.