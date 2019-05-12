[clikt](../../index.md) / [com.github.ajalt.clikt.parsers](../index.md) / [FlagOptionParser](index.md) / [parseShortOpt](./parse-short-opt.md)

# parseShortOpt

`fun parseShortOpt(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, optionIndex: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`OptionParser.ParseResult`](../-option-parser/-parse-result/index.md)

Overrides [OptionParser.parseShortOpt](../-option-parser/parse-short-opt.md)

Parse a single short option and its value.

### Parameters

`name` - The name of the flag used to invoke this option

`argv` - The entire list of command line arguments for the command

`index` - The index of the option flag in [argv](../-option-parser/parse-short-opt.md#com.github.ajalt.clikt.parsers.OptionParser$parseShortOpt(com.github.ajalt.clikt.parameters.options.Option, kotlin.String, kotlin.collections.List((kotlin.String)), kotlin.Int, kotlin.Int)/argv), which may contain multiple short parameters.

`optionIndex` - The index of the option within `argv\[index]`