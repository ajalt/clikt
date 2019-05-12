[clikt](../../../index.md) / [com.github.ajalt.clikt.parsers](../../index.md) / [OptionParser](../index.md) / [ParseResult](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`ParseResult(consumedCount: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, values: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>)``ParseResult(consumedCount: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, invocation: `[`OptionParser.Invocation`](../-invocation/index.md)`)`

### Parameters

`consumedCount` - The number of items in argv that were consumed. This number must be &gt;= 1 if the
entire option was consumed, or 0 if there are other options in the same index (e.g. flag options)

`invocation` - The data from this invocation.