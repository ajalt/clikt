[clikt](../../../index.md) / [com.github.ajalt.clikt.parsers](../../index.md) / [OptionParser](../index.md) / [ParseResult](index.md) / [consumedCount](./consumed-count.md)

# consumedCount

`val consumedCount: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)

The number of items in argv that were consumed. This number must be &gt;= 1 if the
entire option was consumed, or 0 if there are other options in the same index (e.g. flag options)

