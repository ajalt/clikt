[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [OptionGroup](index.md) / [registerOption](./register-option.md)

# registerOption

`open fun registerOption(option: `[`GroupableOption`](../../com.github.ajalt.clikt.core/-groupable-option/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Overrides [ParameterHolder.registerOption](../../com.github.ajalt.clikt.core/-parameter-holder/register-option.md)

Register an option with this command or group.

This is called automatically for the built in options, but you need to call this if you want to add a
custom option.

