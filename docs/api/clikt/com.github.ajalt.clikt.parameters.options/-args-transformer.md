[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [ArgsTransformer](./-args-transformer.md)

# ArgsTransformer

`typealias ArgsTransformer<ValueT, EachT> = `[`OptionCallTransformContext`](-option-call-transform-context/index.md)`.(`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`ValueT`](-args-transformer.md#ValueT)`>) -> `[`EachT`](-args-transformer.md#EachT)

A callback that transforms all the values for a call to the call type.

The input list will always have a size equal to `nvalues`

