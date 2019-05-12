[clikt](../index.md) / [com.github.ajalt.clikt.parameters.groups](index.md) / [mutuallyExclusiveOptions](./mutually-exclusive-options.md)

# mutuallyExclusiveOptions

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ParameterHolder`](../com.github.ajalt.clikt.core/-parameter-holder/index.md)`.mutuallyExclusiveOptions(option1: `[`OptionDelegate`](../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](mutually-exclusive-options.md#T)`?>, option2: `[`OptionDelegate`](../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](mutually-exclusive-options.md#T)`?>, vararg options: `[`OptionDelegate`](../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](mutually-exclusive-options.md#T)`?>, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`MutuallyExclusiveOptions`](-mutually-exclusive-options/index.md)`<`[`T`](mutually-exclusive-options.md#T)`, `[`T`](mutually-exclusive-options.md#T)`?>`

Declare a set of two or more mutually exclusive options.

If none of the options are given on the command line, the value of this delegate will be null.
If one option is given, the value will be that option's value.
If more than one option is given, the value of the last one is used.

All options in the group must have a name specified. All options must be nullable (they cannot
use [flag](../com.github.ajalt.clikt.parameters.options/flag.md), [required](required.md) etc.). If you want flags, you should use [switch](../com.github.ajalt.clikt.parameters.options/switch.md) instead.

### Example:

``` kotlin
val fruits: Int? by mutuallyExclusiveOptions(
  option("--apples").int(),
  option("--oranges").int()
)
```

**See Also**

[com.github.ajalt.clikt.parameters.options.switch](../com.github.ajalt.clikt.parameters.options/switch.md)

[com.github.ajalt.clikt.parameters.types.choice](../com.github.ajalt.clikt.parameters.types/choice.md)

