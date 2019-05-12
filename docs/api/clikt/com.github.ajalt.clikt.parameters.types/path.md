[clikt](../index.md) / [com.github.ajalt.clikt.parameters.types](index.md) / [path](./path.md)

# path

`fun RawArgument.path(exists: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, fileOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, folderOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, writable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, readable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, fileSystem: FileSystem = FileSystems.getDefault()): `[`ProcessedArgument`](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md)`<Path, Path>`

Convert the argument to a [Path](#).

### Parameters

`exists` - If true, fail if the given path does not exist

`fileOkay` - If false, fail if the given path is a file

`folderOkay` - If false, fail if the given path is a directory

`writable` - If true, fail if the given path is not writable

`readable` - If true, fail if the given path is not readable

`fileSystem` - If specified, the [FileSystem](#) with which to resolve paths.`fun `[`RawOption`](../com.github.ajalt.clikt.parameters.options/-raw-option.md)`.path(exists: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, fileOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, folderOkay: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, writable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, readable: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, fileSystem: FileSystem = FileSystems.getDefault()): `[`NullableOption`](../com.github.ajalt.clikt.parameters.options/-nullable-option.md)`<Path, Path>`

Convert the option to a [Path](#).

### Parameters

`exists` - If true, fail if the given path does not exist

`fileOkay` - If false, fail if the given path is a file

`folderOkay` - If false, fail if the given path is a directory

`writable` - If true, fail if the given path is not writable

`readable` - If true, fail if the given path is not readable

`fileSystem` - If specified, the [FileSystem](#) with which to resolve paths.