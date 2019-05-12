[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.options](../index.md) / [Option](./index.md)

# Option

`interface Option`

An optional command line parameter that takes a fixed number of values.

Options can take any fixed number of values, including 0.

### Properties

| Name | Summary |
|---|---|
| [completionCandidates](completion-candidates.md) | `open val completionCandidates: `[`CompletionCandidates`](../../com.github.ajalt.clikt.completion/-completion-candidates/index.md)<br>Optional set of strings to use when the user invokes shell autocomplete on a value for this option. |
| [help](help.md) | `abstract val help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The description of this option, usually a single line. |
| [helpTags](help-tags.md) | `abstract val helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Extra information about this option to pass to the help formatter. |
| [hidden](hidden.md) | `abstract val hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If true, this option should not appear in help output. |
| [metavar](metavar.md) | `abstract val metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A name representing the values for this option that can be displayed to the user. |
| [names](names.md) | `abstract val names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The names that can be used to invoke this option. They must start with a punctuation character. |
| [nvalues](nvalues.md) | `abstract val nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The number of values that must be given to this option. |
| [parameterHelp](parameter-help.md) | `open val parameterHelp: `[`HelpFormatter.ParameterHelp.Option`](../../com.github.ajalt.clikt.output/-help-formatter/-parameter-help/-option/index.md)`?`<br>Information about this option for the help output. |
| [parser](parser.md) | `abstract val parser: `[`OptionParser`](../../com.github.ajalt.clikt.parsers/-option-parser/index.md)<br>The parser for this option's values. |
| [secondaryNames](secondary-names.md) | `abstract val secondaryNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Names that can be used for a secondary purpose, like disabling flag options. |

### Functions

| Name | Summary |
|---|---|
| [finalize](finalize.md) | `abstract fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, invocations: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed to transform and store the option's value. |
| [postValidate](post-validate.md) | `abstract fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](finalize.md)d to perform validation of the final value. |

### Inheritors

| Name | Summary |
|---|---|
| [EagerOption](../-eager-option/index.md) | `class EagerOption : `[`Option`](./index.md)<br>An [Option](./index.md) with no values that is [finalize](../-eager-option/finalize.md)d before other types of options. |
| [GroupableOption](../../com.github.ajalt.clikt.core/-groupable-option/index.md) | `interface GroupableOption : `[`Option`](./index.md)<br>An option that can be added to a [ParameterGroup](../../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md) |
| [OptionCallTransformContext](../-option-call-transform-context/index.md) | `class OptionCallTransformContext : `[`Option`](./index.md)<br>A receiver for options transformers. |
| [OptionTransformContext](../-option-transform-context/index.md) | `class OptionTransformContext : `[`Option`](./index.md)<br>A receiver for options transformers. |
