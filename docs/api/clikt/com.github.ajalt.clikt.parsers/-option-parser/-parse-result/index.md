[clikt](../../../index.md) / [com.github.ajalt.clikt.parsers](../../index.md) / [OptionParser](../index.md) / [ParseResult](./index.md)

# ParseResult

`data class ParseResult`

### Parameters

`consumedCount` - The number of items in argv that were consumed. This number must be &gt;= 1 if the
entire option was consumed, or 0 if there are other options in the same index (e.g. flag options)

`invocation` - The data from this invocation.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `ParseResult(consumedCount: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, values: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)`<br>`ParseResult(consumedCount: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, invocation: `[`OptionParser.Invocation`](../-invocation/index.md)`)` |

### Properties

| Name | Summary |
|---|---|
| [consumedCount](consumed-count.md) | `val consumedCount: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The number of items in argv that were consumed. This number must be &gt;= 1 if the entire option was consumed, or 0 if there are other options in the same index (e.g. flag options) |
| [invocation](invocation.md) | `val invocation: `[`OptionParser.Invocation`](../-invocation/index.md)<br>The data from this invocation. |
