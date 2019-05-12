[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [registerOption](./register-option.md)

# registerOption

`fun registerOption(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Register an option with this command.

This is called automatically for the built in options, but you need to call this if you want to add a
custom option.

`open fun registerOption(option: `[`GroupableOption`](../-groupable-option/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Overrides [ParameterHolder.registerOption](../-parameter-holder/register-option.md)

Register an option with this command or group.

This is called automatically for the built in options, but you need to call this if you want to add a
custom option.

