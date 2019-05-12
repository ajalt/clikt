[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [CallsTransformer](./-calls-transformer.md)

# CallsTransformer

`typealias CallsTransformer<EachT, AllT> = `[`OptionTransformContext`](-option-transform-context/index.md)`.(`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`EachT`](-calls-transformer.md#EachT)`>) -> `[`AllT`](-calls-transformer.md#AllT)

A callback that transforms all of the calls to the final option type.

The input list will have a size equal to the number of times the option appears on the command line.

