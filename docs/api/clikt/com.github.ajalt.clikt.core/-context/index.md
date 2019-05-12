[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [Context](./index.md)

# Context

`class Context`

A object used to control command line parsing and pass data between commands.

A new Context instance is created for each command each time the command line is parsed.

### Types

| Name | Summary |
|---|---|
| [Builder](-builder/index.md) | `class Builder` |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `Context(parent: `[`Context`](./index.md)`?, command: `[`CliktCommand`](../-clikt-command/index.md)`, allowInterspersedArgs: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, autoEnvvarPrefix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?, printExtraMessages: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, helpOptionNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, helpOptionMessage: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, helpFormatter: `[`HelpFormatter`](../../com.github.ajalt.clikt.output/-help-formatter/index.md)`, tokenTransformer: `[`Context`](./index.md)`.(`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, console: `[`CliktConsole`](../../com.github.ajalt.clikt.output/-clikt-console/index.md)`)`<br>A object used to control command line parsing and pass data between commands. |

### Properties

| Name | Summary |
|---|---|
| [allowInterspersedArgs](allow-interspersed-args.md) | `val allowInterspersedArgs: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If false, options and arguments cannot be mixed; the first time an argument is encountered, all remaining tokens are parsed as arguments. |
| [autoEnvvarPrefix](auto-envvar-prefix.md) | `val autoEnvvarPrefix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The prefix to add to inferred envvar names. If null, the prefix is based on the parent's prefix, if there is one. If no command specifies, a prefix, envvar lookup is disabled. |
| [command](command.md) | `val command: `[`CliktCommand`](../-clikt-command/index.md)<br>The command that this context associated with. |
| [console](console.md) | `val console: `[`CliktConsole`](../../com.github.ajalt.clikt.output/-clikt-console/index.md)<br>The console to use to print messages. |
| [helpFormatter](help-formatter.md) | `val helpFormatter: `[`HelpFormatter`](../../com.github.ajalt.clikt.output/-help-formatter/index.md)<br>The help formatter for this command. |
| [helpOptionMessage](help-option-message.md) | `val helpOptionMessage: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The description of the help option. |
| [helpOptionNames](help-option-names.md) | `val helpOptionNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The names to use for the help option. If any names in the set conflict with other options, the conflicting name will not be used for the help option. If the set is empty, or contains no unique names, no help option will be added. |
| [invokedSubcommand](invoked-subcommand.md) | `var invokedSubcommand: `[`CliktCommand`](../-clikt-command/index.md)`?` |
| [obj](obj.md) | `var obj: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?` |
| [parent](parent.md) | `val parent: `[`Context`](./index.md)`?`<br>If this context is the child of another command, [parent](parent.md) is the parent command's context. |
| [printExtraMessages](print-extra-messages.md) | `val printExtraMessages: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Set this to false to prevent extra messages from being printed automatically. You can still access them at [CliktCommand.messages](../-clikt-command/messages.md) inside of [CliktCommand.run](../-clikt-command/run.md). |
| [tokenTransformer](token-transformer.md) | `val tokenTransformer: `[`Context`](./index.md)`.(`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>An optional transformation function that is called to transform command line tokens (options and commands) before parsing. This can be used to implement e.g. case insensitive behavior. |

### Functions

| Name | Summary |
|---|---|
| [fail](fail.md) | `fun fail(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ""): `[`Nothing`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)<br>Throw a [UsageError](../-usage-error/index.md) with the given message |
| [findObject](find-object.md) | `fun <T> findObject(): `[`T`](find-object.md#T)`?`<br>Find the closest object of type [T](find-object.md#T)`fun <T> findObject(defaultValue: () -> `[`T`](find-object.md#T)`): `[`T`](find-object.md#T)<br>Find the closest object of type [T](find-object.md#T), setting `this.`[obj](obj.md) if one is not found. |
| [findRoot](find-root.md) | `fun findRoot(): `[`Context`](./index.md)<br>Find the outermost context |

### Companion Object Functions

| Name | Summary |
|---|---|
| [build](build.md) | `fun build(command: `[`CliktCommand`](../-clikt-command/index.md)`, parent: `[`Context`](./index.md)`? = null, block: `[`Context.Builder`](-builder/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Context`](./index.md) |
