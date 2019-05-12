[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [flag](./flag.md)

# flag

`fun `[`RawOption`](-raw-option.md)`.flag(vararg secondaryNames: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`FlagOption`](-flag-option/index.md)`<`[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`>`

Turn an option into a boolean flag.

### Parameters

`secondaryNames` - additional names for that option that cause the option value to be false. It's good
practice to provide secondary names so that users can disable an option that was previously enabled.

`default` - the value for this property if the option is not given on the command line.