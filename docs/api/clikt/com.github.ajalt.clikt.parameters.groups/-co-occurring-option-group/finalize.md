[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [CoOccurringOptionGroup](index.md) / [finalize](./finalize.md)

# finalize

`fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocationsByOption: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Overrides [ParameterGroup.finalize](../-parameter-group/finalize.md)

Called after this command's argv is parsed and all options are validated to validate the group constraints.

### Parameters

`context` - The context for this parse

`invocationsByOption` - The invocations of options in this group.