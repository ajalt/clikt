[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.options](../index.md) / [EagerOption](./index.md)

# EagerOption

`class EagerOption : `[`Option`](../-option/index.md)

An [Option](../-option/index.md) with no values that is [finalize](finalize.md)d before other types of options.

### Parameters

`callback` - This callback is called when the option is encountered on the command line. If you want to
print a message and halt execution normally, you should throw a [PrintMessage](../../com.github.ajalt.clikt.core/-print-message/index.md) exception. The callback it
passed the current execution context as a parameter.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `EagerOption(vararg names: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap(), callback: `[`OptionTransformContext`](../-option-transform-context/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)``EagerOption(names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, callback: `[`OptionTransformContext`](../-option-transform-context/index.md)`.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)`<br>An [Option](../-option/index.md) with no values that is [finalize](finalize.md)d before other types of options. |

### Properties

| Name | Summary |
|---|---|
| [help](help.md) | `val help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The description of this option, usually a single line. |
| [helpTags](help-tags.md) | `val helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Extra information about this option to pass to the help formatter. |
| [hidden](hidden.md) | `val hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If true, this option should not appear in help output. |
| [metavar](metavar.md) | `val metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A name representing the values for this option that can be displayed to the user. |
| [names](names.md) | `val names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The names that can be used to invoke this option. They must start with a punctuation character. |
| [nvalues](nvalues.md) | `val nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The number of values that must be given to this option. |
| [parser](parser.md) | `val parser: `[`OptionParser`](../../com.github.ajalt.clikt.parsers/-option-parser/index.md)<br>The parser for this option's values. |
| [secondaryNames](secondary-names.md) | `val secondaryNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Names that can be used for a secondary purpose, like disabling flag options. |

### Inherited Properties

| Name | Summary |
|---|---|
| [completionCandidates](../-option/completion-candidates.md) | `open val completionCandidates: `[`CompletionCandidates`](../../com.github.ajalt.clikt.completion/-completion-candidates/index.md)<br>Optional set of strings to use when the user invokes shell autocomplete on a value for this option. |
| [parameterHelp](../-option/parameter-help.md) | `open val parameterHelp: `[`HelpFormatter.ParameterHelp.Option`](../../com.github.ajalt.clikt.output/-help-formatter/-parameter-help/-option/index.md)`?`<br>Information about this option for the help output. |

### Functions

| Name | Summary |
|---|---|
| [finalize](finalize.md) | `fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocations: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed to transform and store the option's value. |
| [postValidate](post-validate.md) | `fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](../-option/finalize.md)d to perform validation of the final value. |
