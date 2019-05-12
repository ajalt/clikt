[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.arguments](../index.md) / [ProcessedArgument](index.md) / [provideDelegate](./provide-delegate.md)

# provideDelegate

`operator fun provideDelegate(thisRef: `[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, prop: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`AllT`](index.md#AllT)`>`

Overrides [ArgumentDelegate.provideDelegate](../-argument-delegate/provide-delegate.md)

Implementations must call [CliktCommand.registerArgument](../../com.github.ajalt.clikt.core/-clikt-command/register-argument.md)

