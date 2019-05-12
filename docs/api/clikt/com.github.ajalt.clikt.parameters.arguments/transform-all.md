[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [transformAll](./transform-all.md)

# transformAll

`fun <AllInT, ValueT, AllOutT> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`AllInT`](transform-all.md#AllInT)`, `[`ValueT`](transform-all.md#ValueT)`>.transformAll(nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`? = null, required: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`? = null, transform: `[`ArgCallsTransformer`](-arg-calls-transformer.md)`<`[`AllOutT`](transform-all.md#AllOutT)`, `[`ValueT`](transform-all.md#ValueT)`>): `[`ProcessedArgument`](-processed-argument/index.md)`<`[`AllOutT`](transform-all.md#AllOutT)`, `[`ValueT`](transform-all.md#ValueT)`>`

Transform all values to the final argument type.

The input is a list of values, one for each value on the command line. The values in the
list are the output of calls to [convert](convert.md). The input list will have a size of [nvalues](transform-all.md#com.github.ajalt.clikt.parameters.arguments$transformAll(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.arguments.transformAll.AllInT, com.github.ajalt.clikt.parameters.arguments.transformAll.ValueT)), kotlin.Int, kotlin.Boolean, kotlin.Function2((com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext, kotlin.collections.List((com.github.ajalt.clikt.parameters.arguments.transformAll.ValueT)), com.github.ajalt.clikt.parameters.arguments.transformAll.AllOutT)))/nvalues) if [nvalues](transform-all.md#com.github.ajalt.clikt.parameters.arguments$transformAll(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.arguments.transformAll.AllInT, com.github.ajalt.clikt.parameters.arguments.transformAll.ValueT)), kotlin.Int, kotlin.Boolean, kotlin.Function2((com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext, kotlin.collections.List((com.github.ajalt.clikt.parameters.arguments.transformAll.ValueT)), com.github.ajalt.clikt.parameters.arguments.transformAll.AllOutT)))/nvalues) is &gt; 0.

Used to implement functions like [pair](pair.md) and [multiple](multiple.md).

### Parameters

`nvalues` - The number of values required by this argument. A negative [nvalues](transform-all.md#com.github.ajalt.clikt.parameters.arguments$transformAll(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.arguments.transformAll.AllInT, com.github.ajalt.clikt.parameters.arguments.transformAll.ValueT)), kotlin.Int, kotlin.Boolean, kotlin.Function2((com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext, kotlin.collections.List((com.github.ajalt.clikt.parameters.arguments.transformAll.ValueT)), com.github.ajalt.clikt.parameters.arguments.transformAll.AllOutT)))/nvalues) indicates a variable number
of values.

`required` - If true, an error with be thrown if no values are provided to this argument.