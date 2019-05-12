[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](./index.md)

# CliktCommand

`abstract class CliktCommand : `[`ParameterHolder`](../-parameter-holder/index.md)

The [CliktCommand](./index.md) is the core of command line interfaces in Clikt.

Command line interfaces created by creating a subclass of [CliktCommand](./index.md) with properties defined with
[option](../../com.github.ajalt.clikt.parameters.options/option.md) and [argument](../../com.github.ajalt.clikt.parameters.arguments/argument.md). You can then parse `argv` by calling [main](main.md), which will take care of printing
errors and help to the user. If you want to handle output yourself, you can use [parse](parse.md) instead.

Once the command line has been parsed and all of the parameters are populated, [run](run.md) is called.

### Parameters

`help` - The help for this command. The first line is used in the usage string, and the entire string is
used in the help output. Paragraphs are automatically re-wrapped to the terminal width.

`epilog` - Text to display at the end of the full help output. It is automatically re-wrapped to the
terminal width.

`name` - The name of the program to use in the help output. If not given, it is inferred from the class
name.

`invokeWithoutSubcommand` - Used when this command has subcommands, and this command is called
without a subcommand. If true, [run](run.md) will be called. By default, a [PrintHelpMessage](../-print-help-message/index.md) is thrown instead.

`printHelpOnEmptyArgs` - If this command is called with no values on the command line, print a
help message (by throwing [PrintHelpMessage](../-print-help-message/index.md)) if this is true, otherwise run normally.

`helpTags` - Extra information about this option to pass to the help formatter.

`autoCompleteEnvvar` - The envvar to use to enable shell autocomplete script generation. Set
to null to disable generation.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `CliktCommand(help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", epilog: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, invokeWithoutSubcommand: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, printHelpOnEmptyArgs: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap(), autoCompleteEnvvar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = "")`<br>The [CliktCommand](./index.md) is the core of command line interfaces in Clikt. |

### Properties

| Name | Summary |
|---|---|
| [commandHelp](command-help.md) | `val commandHelp: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [commandHelpEpilog](command-help-epilog.md) | `val commandHelpEpilog: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [commandName](command-name.md) | `val commandName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [context](context.md) | `val context: `[`Context`](../-context/index.md)<br>This command's context. |
| [helpTags](help-tags.md) | `val helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Extra information about this option to pass to the help formatter. |
| [invokeWithoutSubcommand](invoke-without-subcommand.md) | `val invokeWithoutSubcommand: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Used when this command has subcommands, and this command is called without a subcommand. If true, [run](run.md) will be called. By default, a [PrintHelpMessage](../-print-help-message/index.md) is thrown instead. |
| [messages](messages.md) | `val messages: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>All messages issued during parsing. |
| [printHelpOnEmptyArgs](print-help-on-empty-args.md) | `val printHelpOnEmptyArgs: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If this command is called with no values on the command line, print a help message (by throwing [PrintHelpMessage](../-print-help-message/index.md)) if this is true, otherwise run normally. |

### Functions

| Name | Summary |
|---|---|
| [aliases](aliases.md) | `open fun aliases(): `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>>`<br>A list of command aliases. |
| [echo](echo.md) | `fun echo(message: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`?, trailingNewline: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, err: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, lineSeparator: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = context.console.lineSeparator): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Print the [message](echo.md#com.github.ajalt.clikt.core.CliktCommand$echo(kotlin.Any, kotlin.Boolean, kotlin.Boolean, kotlin.String)/message) to the screen. |
| [getFormattedHelp](get-formatted-help.md) | `open fun getFormattedHelp(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Return the full help string for this command. |
| [getFormattedUsage](get-formatted-usage.md) | `open fun getFormattedUsage(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Return the usage string for this command. |
| [issueMessage](issue-message.md) | `fun issueMessage(message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Add a message to be printed after parsing |
| [main](main.md) | `fun main(argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Parse the command line and print helpful output if any errors occur.`fun main(argv: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [parse](parse.md) | `fun parse(argv: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, parentContext: `[`Context`](../-context/index.md)`? = null): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Parse the command line and throw an exception if parsing fails.`fun parse(argv: `[`Array`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, parentContext: `[`Context`](../-context/index.md)`? = null): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [registerArgument](register-argument.md) | `fun registerArgument(argument: `[`Argument`](../../com.github.ajalt.clikt.parameters.arguments/-argument/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Register an argument with this command. |
| [registeredSubcommandNames](registered-subcommand-names.md) | `fun registeredSubcommandNames(): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The names of all direct children of this command |
| [registerOption](register-option.md) | `fun registerOption(option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Register an option with this command.`open fun registerOption(option: `[`GroupableOption`](../-groupable-option/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Register an option with this command or group. |
| [registerOptionGroup](register-option-group.md) | `fun registerOptionGroup(group: `[`ParameterGroup`](../../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Register a group with this command. |
| [run](run.md) | `abstract fun run(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Perform actions after parsing is complete and this command is invoked. |
| [shortHelp](short-help.md) | `fun shortHelp(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The help displayed in the commands list when this command is used as a subcommand. |

### Extension Functions

| Name | Summary |
|---|---|
| [argument](../../com.github.ajalt.clikt.parameters.arguments/argument.md) | `fun `[`CliktCommand`](./index.md)`.argument(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap()): RawArgument`<br>Create a property delegate argument. |
| [context](../context.md) | `fun <T : `[`CliktCommand`](./index.md)`> `[`T`](../context.md#T)`.context(block: `[`Context.Builder`](../-context/-builder/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`T`](../context.md#T)<br>Configure this command's [Context](../-context/index.md). |
| [findObject](../find-object.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](./index.md)`.findObject(): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](./index.md)`, `[`T`](../find-object.md#T)`?>`<br>Find the closest object of type [T](../find-object.md#T), or null`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](./index.md)`.findObject(default: () -> `[`T`](../find-object.md#T)`): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](./index.md)`, `[`T`](../find-object.md#T)`>`<br>Find the closest object of type [T](../find-object.md#T), setting `context.obj` if one is not found. |
| [mutuallyExclusiveOptions](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`ParameterHolder`](../-parameter-holder/index.md)`.mutuallyExclusiveOptions(option1: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>, option2: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>, vararg options: `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>, name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): `[`MutuallyExclusiveOptions`](../../com.github.ajalt.clikt.parameters.groups/-mutually-exclusive-options/index.md)`<`[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`, `[`T`](../../com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md#T)`?>`<br>Declare a set of two or more mutually exclusive options. |
| [option](../../com.github.ajalt.clikt.parameters.options/option.md) | `fun `[`ParameterHolder`](../-parameter-holder/index.md)`.option(vararg names: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, envvar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, envvarSplit: `[`Regex`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/index.html)`? = null, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap()): `[`RawOption`](../../com.github.ajalt.clikt.parameters.options/-raw-option.md)<br>Create a property delegate option. |
| [requireObject](../require-object.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`CliktCommand`](./index.md)`.requireObject(): `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](./index.md)`, `[`T`](../require-object.md#T)`>`<br>Find the closest object of type [T](../require-object.md#T), or throw a [NullPointerException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-null-pointer-exception/index.html) |
| [subcommands](../subcommands.md) | `fun <T : `[`CliktCommand`](./index.md)`> `[`T`](../subcommands.md#T)`.subcommands(commands: `[`Iterable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-iterable/index.html)`<`[`CliktCommand`](./index.md)`>): `[`T`](../subcommands.md#T)<br>`fun <T : `[`CliktCommand`](./index.md)`> `[`T`](../subcommands.md#T)`.subcommands(vararg commands: `[`CliktCommand`](./index.md)`): `[`T`](../subcommands.md#T)<br>Add the given commands as a subcommand of this command. |
| [versionOption](../../com.github.ajalt.clikt.parameters.options/version-option.md) | `fun <T : `[`CliktCommand`](./index.md)`> `[`T`](../../com.github.ajalt.clikt.parameters.options/version-option.md#T)`.versionOption(version: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "Show the version and exit", names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = setOf("--version"), message: (`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`) -> `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = { "$commandName version $it" }): `[`T`](../../com.github.ajalt.clikt.parameters.options/version-option.md#T)<br>Add an eager option to this command that, when invoked, prints a version message and exits. |

### Inheritors

| Name | Summary |
|---|---|
| [NoRunCliktCommand](../-no-run-clikt-command/index.md) | `open class NoRunCliktCommand : `[`CliktCommand`](./index.md)<br>A [CliktCommand](./index.md) that has a default implementation of [CliktCommand.run](run.md) that is a no-op. |
