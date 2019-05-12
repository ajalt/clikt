[clikt](../../index.md) / [com.github.ajalt.clikt.parsers](../index.md) / [OptionParser](./index.md)

# OptionParser

`interface OptionParser`

A parser for [Option](../../com.github.ajalt.clikt.parameters.options/-option/index.md)s.

All functions should be pure, since the same command instance can parse arguments multiple times.

### Types

| Name | Summary |
|---|---|
| [Invocation](-invocation/index.md) | `data class Invocation`<br>The input from a single instance of an option input. |
| [ParseResult](-parse-result/index.md) | `data class ParseResult` |

### Functions

| Name | Summary |
|---|---|
| [parseLongOpt](parse-long-opt.md) | `abstract fun parseLongOpt(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, explicitValue: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?): `[`OptionParser.ParseResult`](-parse-result/index.md)<br>Parse a single long option and its value. |
| [parseShortOpt](parse-short-opt.md) | `abstract fun parseShortOpt(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, index: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, optionIndex: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`OptionParser.ParseResult`](-parse-result/index.md)<br>Parse a single short option and its value. |

### Inheritors

| Name | Summary |
|---|---|
| [FlagOptionParser](../-flag-option-parser/index.md) | `object FlagOptionParser : `[`OptionParser`](./index.md)<br>A parser for options that take no values. |
| [OptionWithValuesParser](../-option-with-values-parser/index.md) | `object OptionWithValuesParser : `[`OptionParser`](./index.md)<br>An option that takes one more values |
