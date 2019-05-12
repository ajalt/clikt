[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.options](../index.md) / [OptionWithValues](index.md) / [provideDelegate](./provide-delegate.md)

# provideDelegate

`operator fun provideDelegate(thisRef: `[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`, prop: `[`KProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-property/index.html)`<*>): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`ParameterHolder`](../../com.github.ajalt.clikt.core/-parameter-holder/index.md)`, `[`AllT`](index.md#AllT)`>`

Overrides [OptionDelegate.provideDelegate](../-option-delegate/provide-delegate.md)

Implementations must call [ParameterHolder.registerOption](../../com.github.ajalt.clikt.core/-parameter-holder/register-option.md)

