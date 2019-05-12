[clikt](../../index.md) / [com.github.ajalt.clikt.parsers](../index.md) / [FlagOptionParser](index.md) / [parseLongOpt](./parse-long-opt.md)

# parseLongOpt

`fun parseLongOpt(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, explicitValue: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?): `[`OptionParser.ParseResult`](../-option-parser/-parse-result/index.md)

Overrides [OptionParser.parseLongOpt](../-option-parser/parse-long-opt.md)

Parse a single long option and its value.

### Parameters

`name` - The name of the flag used to invoke this option

`argv` - The entire list of command line arguments for the command

`index` - The index of the option flag in [argv](../-option-parser/parse-long-opt.md#com.github.ajalt.clikt.parsers.OptionParser$parseLongOpt(com.github.ajalt.clikt.parameters.options.Option, kotlin.String, kotlin.collections.List((kotlin.String)), kotlin.Int, kotlin.String)/argv), which may contain an '=' with the first value