[clikt](../index.md) / [com.github.ajalt.clikt.parameters.types](index.md) / [file](./file.md)

# file

`fun RawArgument.file(exists: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, fileOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, folderOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, writable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, readable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<`[`File`](https://docs.oracle.com/javase/6/docs/api/java/io/File.html)`, `[`File`](https://docs.oracle.com/javase/6/docs/api/java/io/File.html)`>`

Convert the argument to a [File](https://docs.oracle.com/javase/6/docs/api/java/io/File.html).

### Parameters

`exists` - If true, fail if the given path does not exist

`fileOkay` - If false, fail if the given path is a file

`folderOkay` - If false, fail if the given path is a directory

`writable` - If true, fail if the given path is not writable

`readable` - If true, fail if the given path is not readable`fun `[`RawOption`](../com.github.ajalt.clikt.parameters.options/-raw-option.md)`.file(exists: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, fileOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, folderOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, writable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, readable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`NullableOption`](../com.github.ajalt.clikt.parameters.options/-nullable-option.md)`<`[`File`](https://docs.oracle.com/javase/6/docs/api/java/io/File.html)`, `[`File`](https://docs.oracle.com/javase/6/docs/api/java/io/File.html)`>`

Convert the option to a [File](https://docs.oracle.com/javase/6/docs/api/java/io/File.html).

### Parameters

`exists` - If true, fail if the given path does not exist

`fileOkay` - If false, fail if the given path is a file

`folderOkay` - If false, fail if the given path is a directory

`writable` - If true, fail if the given path is not writable

`readable` - If true, fail if the given path is not readable