[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [argument](./argument.md)

# argument

`fun `[`CliktCommand`](../com.github.ajalt.clikt.core/-clikt-command/index.md)`.argument(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap()): RawArgument`

Create a property delegate argument.

The order that these delegates are created is the order that arguments must appear. By default, the
argument takes one value and throws an error if no value is given. The behavior can be changed with
functions like [int](../com.github.ajalt.clikt.parameters.types/int.md) and [optional](optional.md).

### Parameters

`name` - The metavar for this argument. If not given, the name is inferred form the property name.

`help` - The description of this argument for help output.

`helpTags` - Extra information about this option to pass to the help formatter