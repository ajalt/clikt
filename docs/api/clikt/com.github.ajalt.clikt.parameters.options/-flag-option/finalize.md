[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.options](../index.md) / [FlagOption](index.md) / [finalize](./finalize.md)

# finalize

`fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocations: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Overrides [Option.finalize](../-option/finalize.md)

Called after this command's argv is parsed to transform and store the option's value.

You cannot refer to other parameter values during this call, since they might not have been
finalized yet.

### Parameters

`context` - The context for this parse

`invocations` - A possibly empty list of invocations of this option.