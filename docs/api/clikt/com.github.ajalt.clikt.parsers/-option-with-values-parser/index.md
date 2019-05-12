[clikt](../../index.md) / [com.github.ajalt.clikt.parsers](../index.md) / [OptionWithValuesParser](./index.md)

# OptionWithValuesParser

`object OptionWithValuesParser : `[`OptionParser`](../-option-parser/index.md)

An option that takes one more values

### Functions

| Name | Summary |
|---|---|
| [parseLongOpt](parse-long-opt.md) | `fun parseLongOpt(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, explicitValue: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?): `[`OptionParser.ParseResult`](../-option-parser/-parse-result/index.md)<br>Parse a single long option and its value. |
| [parseShortOpt](parse-short-opt.md) | `fun parseShortOpt(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, optionIndex: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`OptionParser.ParseResult`](../-option-parser/-parse-result/index.md)<br>Parse a single short option and its value. |
