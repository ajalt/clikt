[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [HelpFormatter](./index.md)

# HelpFormatter

`interface HelpFormatter`

Creates help and usage strings for a command.

You can set the formatter for a command when configuring the context.

### Types

| Name | Summary |
|---|---|
| [ParameterHelp](-parameter-help/index.md) | `sealed class ParameterHelp` |
| [Tags](-tags/index.md) | `object Tags`<br>Standard tag names for parameter help |

### Functions

| Name | Summary |
|---|---|
| [formatHelp](format-help.md) | `abstract fun formatHelp(prolog: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, epilog: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, parameters: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`HelpFormatter.ParameterHelp`](-parameter-help/index.md)`>, programName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ""): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Create the full help string. |
| [formatUsage](format-usage.md) | `abstract fun formatUsage(parameters: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`HelpFormatter.ParameterHelp`](-parameter-help/index.md)`>, programName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ""): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Create the one-line usage information for a command. |

### Inheritors

| Name | Summary |
|---|---|
| [CliktHelpFormatter](../-clikt-help-formatter/index.md) | `open class CliktHelpFormatter : `[`HelpFormatter`](./index.md) |
