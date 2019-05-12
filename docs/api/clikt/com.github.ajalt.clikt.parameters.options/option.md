[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [option](./option.md)

# option

`fun `[`ParameterHolder`](../com.github.ajalt.clikt.core/-parameter-holder/index.md)`.option(vararg names: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, envvar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, envvarSplit: `[`Regex`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/index.html)`? = null, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap()): `[`RawOption`](-raw-option.md)

Create a property delegate option.

By default, the property will return null if the option does not appear on the command line. If the option
is invoked multiple times, the value from the last invocation will be used The option can be modified with
functions like [int](../com.github.ajalt.clikt.parameters.types/int.md), [pair](pair.md), and [multiple](multiple.md).

### Parameters

`names` - The names that can be used to invoke this option. They must start with a punctuation character.
If not given, a name is inferred from the property name.

`help` - The description of this option, usually a single line.

`metavar` - A name representing the values for this option that can be displayed to the user.
Automatically inferred from the type.

`hidden` - Hide this option from help outputs.

`envvar` - The environment variable that will be used for the value if one is not given on the command
line.

`envvarSplit` - The pattern to split the value of the [envvar](option.md#com.github.ajalt.clikt.parameters.options$option(com.github.ajalt.clikt.core.ParameterHolder, kotlin.Array((kotlin.String)), kotlin.String, kotlin.String, kotlin.Boolean, kotlin.String, kotlin.text.Regex, kotlin.collections.Map((kotlin.String, )))/envvar) on. Defaults to whitespace,
although some conversions like `file` change the default.

`helpTags` - Extra information about this option to pass to the help formatter