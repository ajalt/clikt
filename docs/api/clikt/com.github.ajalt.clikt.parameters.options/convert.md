[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [convert](./convert.md)

# convert

`inline fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`RawOption`](-raw-option.md)`.convert(metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "VALUE", envvarSplit: `[`Regex`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/index.html)` = this.envvarSplit.default, completionCandidates: `[`CompletionCandidates`](../com.github.ajalt.clikt.completion/-completion-candidates/index.md)` = this.completionCandidates, crossinline conversion: `[`ValueTransformer`](-value-transformer.md)`<`[`T`](convert.md#T)`>): `[`NullableOption`](-nullable-option.md)`<`[`T`](convert.md#T)`, `[`T`](convert.md#T)`>`

Convert the option value type.

The [conversion](convert.md#com.github.ajalt.clikt.parameters.options$convert(com.github.ajalt.clikt.parameters.options.OptionWithValues((kotlin.String, kotlin.String, )), kotlin.String, kotlin.text.Regex, com.github.ajalt.clikt.completion.CompletionCandidates, kotlin.Function2((com.github.ajalt.clikt.parameters.options.OptionCallTransformContext, kotlin.String, com.github.ajalt.clikt.parameters.options.convert.T)))/conversion) is called once for each value in each invocation of the option. If any errors are thrown,
they are caught and a [BadParameterValue](../com.github.ajalt.clikt.core/-bad-parameter-value/index.md) is thrown with the error message. You can call `fail` to throw a
[BadParameterValue](../com.github.ajalt.clikt.core/-bad-parameter-value/index.md) manually.

### Parameters

`metavar` - The metavar for the type. Overridden by a metavar passed to [option](option.md).

`envvarSplit` - If the value is read from an envvar, the pattern to split the value on. The default
splits on whitespace. This value is can be overridden by passing a value to the [option](option.md) function.

`completionCandidates` - candidates to use when completing this option in shell autocomplete