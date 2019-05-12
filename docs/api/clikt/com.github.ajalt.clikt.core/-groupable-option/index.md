[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [GroupableOption](./index.md)

# GroupableOption

`interface GroupableOption : `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)

An option that can be added to a [ParameterGroup](../../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md)

### Properties

| Name | Summary |
|---|---|
| [groupName](group-name.md) | `abstract var groupName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the group, or null if this option should not be grouped in the help output. |
| [parameterGroup](parameter-group.md) | `abstract var parameterGroup: `[`ParameterGroup`](../../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md)`?`<br>The group that this option belongs to, or null. Set by the group. |

### Inherited Properties

| Name | Summary |
|---|---|
| [completionCandidates](../../com.github.ajalt.clikt.parameters.options/-option/completion-candidates.md) | `open val completionCandidates: `[`CompletionCandidates`](../../com.github.ajalt.clikt.completion/-completion-candidates/index.md)<br>Optional set of strings to use when the user invokes shell autocomplete on a value for this option. |
| [help](../../com.github.ajalt.clikt.parameters.options/-option/help.md) | `abstract val help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The description of this option, usually a single line. |
| [helpTags](../../com.github.ajalt.clikt.parameters.options/-option/help-tags.md) | `abstract val helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Extra information about this option to pass to the help formatter. |
| [hidden](../../com.github.ajalt.clikt.parameters.options/-option/hidden.md) | `abstract val hidden: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If true, this option should not appear in help output. |
| [metavar](../../com.github.ajalt.clikt.parameters.options/-option/metavar.md) | `abstract val metavar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>A name representing the values for this option that can be displayed to the user. |
| [names](../../com.github.ajalt.clikt.parameters.options/-option/names.md) | `abstract val names: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>The names that can be used to invoke this option. They must start with a punctuation character. |
| [nvalues](../../com.github.ajalt.clikt.parameters.options/-option/nvalues.md) | `abstract val nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The number of values that must be given to this option. |
| [parameterHelp](../../com.github.ajalt.clikt.parameters.options/-option/parameter-help.md) | `open val parameterHelp: `[`HelpFormatter.ParameterHelp.Option`](../../com.github.ajalt.clikt.output/-help-formatter/-parameter-help/-option/index.md)`?`<br>Information about this option for the help output. |
| [parser](../../com.github.ajalt.clikt.parameters.options/-option/parser.md) | `abstract val parser: `[`OptionParser`](../../com.github.ajalt.clikt.parsers/-option-parser/index.md)<br>The parser for this option's values. |
| [secondaryNames](../../com.github.ajalt.clikt.parameters.options/-option/secondary-names.md) | `abstract val secondaryNames: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Names that can be used for a secondary purpose, like disabling flag options. |

### Inherited Functions

| Name | Summary |
|---|---|
| [finalize](../../com.github.ajalt.clikt.parameters.options/-option/finalize.md) | `abstract fun finalize(context: `[`Context`](../-context/index.md)`, invocations: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`OptionParser.Invocation`](../../com.github.ajalt.clikt.parsers/-option-parser/-invocation/index.md)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed to transform and store the option's value. |
| [postValidate](../../com.github.ajalt.clikt.parameters.options/-option/post-validate.md) | `abstract fun postValidate(context: `[`Context`](../-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](../../com.github.ajalt.clikt.parameters.options/-option/finalize.md)d to perform validation of the final value. |

### Inheritors

| Name | Summary |
|---|---|
| [OptionDelegate](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md) | `interface OptionDelegate<T> : `[`GroupableOption`](./index.md)`, `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`ParameterHolder`](../-parameter-holder/index.md)`, `[`T`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md#T)`>`<br>An option that functions as a property delegate |
| [OptionWithValues](../../com.github.ajalt.clikt.parameters.options/-option-with-values/index.md) | `class OptionWithValues<AllT, EachT, ValueT> : `[`OptionDelegate`](../../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md)`<`[`AllT`](../../com.github.ajalt.clikt.parameters.options/-option-with-values/index.md#AllT)`>, `[`GroupableOption`](./index.md)<br>An [Option](../../com.github.ajalt.clikt.parameters.options/-option/index.md) that takes one or more values. |
