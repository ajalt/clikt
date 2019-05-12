[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [convert](./convert.md)

# convert

`inline fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> RawArgument.convert(completionCandidates: `[`CompletionCandidates`](../com.github.ajalt.clikt.completion/-completion-candidates/index.md)` = this.completionCandidates, crossinline conversion: `[`ArgValueTransformer`](-arg-value-transformer.md)`<`[`T`](convert.md#T)`>): `[`ProcessedArgument`](-processed-argument/index.md)`<`[`T`](convert.md#T)`, `[`T`](convert.md#T)`>`

Convert the argument's values.

The [conversion](convert.md#com.github.ajalt.clikt.parameters.arguments$convert(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((kotlin.String, )), com.github.ajalt.clikt.completion.CompletionCandidates, kotlin.Function2((com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext, kotlin.String, com.github.ajalt.clikt.parameters.arguments.convert.T)))/conversion) is called once for each value given. If any errors are thrown, they are caught and a
[BadParameterValue](../com.github.ajalt.clikt.core/-bad-parameter-value/index.md) is thrown with the error message. You can call `fail` to throw a [BadParameterValue](../com.github.ajalt.clikt.core/-bad-parameter-value/index.md)
manually.

### Parameters

`completionCandidates` - candidates to use when completing this argument in shell autocomplete