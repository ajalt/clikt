[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [transformAll](./transform-all.md)

# transformAll

`fun <AllT, EachT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`NullableOption`](-nullable-option.md)`<`[`EachT`](transform-all.md#EachT)`, `[`ValueT`](transform-all.md#ValueT)`>.transformAll(defaultForHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = this.helpTags[HelpFormatter.Tags.DEFAULT], showAsRequired: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = HelpFormatter.Tags.REQUIRED in this.helpTags, transform: `[`CallsTransformer`](-calls-transformer.md)`<`[`EachT`](transform-all.md#EachT)`, `[`AllT`](transform-all.md#AllT)`>): `[`OptionWithValues`](-option-with-values/index.md)`<`[`AllT`](transform-all.md#AllT)`, `[`EachT`](transform-all.md#EachT)`, `[`ValueT`](transform-all.md#ValueT)`>`

Transform all calls to the option to the final option type.

The input is a list of calls, one for each time the option appears on the command line. The values in the
list are the output of calls to [transformValues](transform-values.md). If the option does not appear from any source (command
line or envvar), this will be called with an empty list.

Used to implement functions like [default](default.md) and [multiple](multiple.md).

### Parameters

`defaultForHelp` - The help text for this option's default value if the help formatter is
configured to show them, or null if this option has no default or the default value should not be
shown.This does not affect behavior outside of help formatting.

`showAsRequired` - Tell the help formatter that this option should be marked as required. This
does not affect behavior outside of help formatting.