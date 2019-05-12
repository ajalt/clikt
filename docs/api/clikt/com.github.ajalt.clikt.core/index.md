[clikt](../index.md) / [com.github.ajalt.clikt.core](./index.md)

## Package com.github.ajalt.clikt.core

### Types

| Name | Summary |
|---|---|
| [CliktCommand](-clikt-command/index.md) | `abstract class CliktCommand : `[`ParameterHolder`](-parameter-holder/index.md)<br>The [CliktCommand](-clikt-command/index.md) is the core of command line interfaces in Clikt. |
| [Context](-context/index.md) | `class Context`<br>A object used to control command line parsing and pass data between commands. |
| [GroupableOption](-groupable-option/index.md) | `interface GroupableOption : `[`Option`](../com.github.ajalt.clikt.parameters.options/-option/index.md)<br>An option that can be added to a [ParameterGroup](../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md) |
| [NoRunCliktCommand](-no-run-clikt-command/index.md) | `open class NoRunCliktCommand : `[`CliktCommand`](-clikt-command/index.md)<br>A [CliktCommand](-clikt-command/index.md) that has a default implementation of [CliktCommand.run](-clikt-command/run.md) that is a no-op. |
| [ParameterHolder](-parameter-holder/index.md) | `interface ParameterHolder` |

### Annotations

| Name | Summary |
|---|---|
| [ParameterHolderDsl](-parameter-holder-dsl/index.md) | `annotation class ParameterHolderDsl` |

### Exceptions

| Name | Summary |
|---|---|
| [Abort](-abort/index.md) | `class Abort : `[`RuntimeException`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-runtime-exception/index.html)<br>An internal error that signals Clikt to abort. |
| [BadParameterValue](-bad-parameter-value/index.md) | `open class BadParameterValue : `[`UsageError`](-usage-error/index.md)<br>A parameter was given the correct number of values, but of invalid format or type. |
| [CliktError](-clikt-error/index.md) | `open class CliktError : `[`RuntimeException`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-runtime-exception/index.html)<br>An exception during command line processing that should be shown to the user. |
| [IncorrectArgumentValueCount](-incorrect-argument-value-count/index.md) | `open class IncorrectArgumentValueCount : `[`UsageError`](-usage-error/index.md)<br>An argument was supplied but the number of values supplied was incorrect. |
| [IncorrectOptionValueCount](-incorrect-option-value-count/index.md) | `open class IncorrectOptionValueCount : `[`UsageError`](-usage-error/index.md)<br>An option was supplied but the number of values supplied to the option was incorrect. |
| [MissingParameter](-missing-parameter/index.md) | `open class MissingParameter : `[`UsageError`](-usage-error/index.md)<br>A required parameter was not provided |
| [MutuallyExclusiveGroupException](-mutually-exclusive-group-exception/index.md) | `open class MutuallyExclusiveGroupException : `[`UsageError`](-usage-error/index.md) |
| [NoSuchOption](-no-such-option/index.md) | `open class NoSuchOption : `[`UsageError`](-usage-error/index.md)<br>An option was provided that does not exist. |
| [PrintHelpMessage](-print-help-message/index.md) | `class PrintHelpMessage : `[`CliktError`](-clikt-error/index.md)<br>An exception that indicates that the command's help should be printed. |
| [PrintMessage](-print-message/index.md) | `class PrintMessage : `[`CliktError`](-clikt-error/index.md)<br>An exception that indicates that a message should be printed. |
| [UsageError](-usage-error/index.md) | `open class UsageError : `[`CliktError`](-clikt-error/index.md)<br>An internal exception that signals a usage error. |

### Functions

| Name | Summary |
|---|---|
| [context](context.md) | `fun <T : `[`CliktCommand`](-clikt-command/index.md)`> `[`T`](context.md#T)`.context(block: `[`Context.Builder`](-context/-builder/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`T`](context.md#T)<br>Configure this command's [Context](-context/index.md). |
| [findObject](find-object.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](-clikt-command/index.md)`.findObject(): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](-clikt-command/index.md)`, `[`T`](find-object.md#T)`?>`<br>Find the closest object of type [T](find-object.md#T), or null`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](-clikt-command/index.md)`.findObject(default: () -> `[`T`](find-object.md#T)`): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](-clikt-command/index.md)`, `[`T`](find-object.md#T)`>`<br>Find the closest object of type [T](find-object.md#T), setting `context.obj` if one is not found. |
| [requireObject](require-object.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](-clikt-command/index.md)`.requireObject(): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](-clikt-command/index.md)`, `[`T`](require-object.md#T)`>`<br>Find the closest object of type [T](require-object.md#T), or throw a [NullPointerException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-null-pointer-exception/index.html) |
| [subcommands](subcommands.md) | `fun <T : `[`CliktCommand`](-clikt-command/index.md)`> `[`T`](subcommands.md#T)`.subcommands(commands: `[`Iterable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)`<`[`CliktCommand`](-clikt-command/index.md)`>): `[`T`](subcommands.md#T)<br>`fun <T : `[`CliktCommand`](-clikt-command/index.md)`> `[`T`](subcommands.md#T)`.subcommands(vararg commands: `[`CliktCommand`](-clikt-command/index.md)`): `[`T`](subcommands.md#T)<br>Add the given commands as a subcommand of this command. |
