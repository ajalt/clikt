[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.arguments](../index.md) / [ProcessedArgument](index.md) / [finalize](./finalize.md)

# finalize

`fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, values: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Overrides [Argument.finalize](../-argument/finalize.md)

Called after this command's argv is parsed to transform and store the argument's value.

You cannot refer to other parameter values during this call, since they might not have been
finalized yet.

### Parameters

`context` - The context for this parse

`values` - A possibly empty list of values provided to this argument.