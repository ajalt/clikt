[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.arguments](../index.md) / [Argument](./index.md)

# Argument

`interface Argument`

A positional parameter to a command.

Arguments can take any number of values.

### Properties

| Name | Summary |
|---|---|
| [completionCandidates](completion-candidates.md) | `open val completionCandidates: `[`CompletionCandidates`](../../com.github.ajalt.clikt.completion/-completion-candidates/index.md)<br>Optional set of strings to use when the user invokes shell autocomplete on a value for this argument. |
| [help](help.md) | `abstract val help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The description of this argument. |
| [helpTags](help-tags.md) | `abstract val helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>`<br>Extra information about this argument to pass to the help formatter. |
| [name](name.md) | `abstract val name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The metavar for this argument. |
| [nvalues](nvalues.md) | `abstract val nvalues: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)<br>The number of values that this argument takes. |
| [parameterHelp](parameter-help.md) | `abstract val parameterHelp: `[`HelpFormatter.ParameterHelp.Argument`](../../com.github.ajalt.clikt.output/-help-formatter/-parameter-help/-argument/index.md)`?`<br>Information about this argument for the help output. |
| [required](required.md) | `abstract val required: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>If true, an error will be thrown if this argument is not given on the command line. |

### Functions

| Name | Summary |
|---|---|
| [finalize](finalize.md) | `abstract fun finalize(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`, values: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after this command's argv is parsed to transform and store the argument's value. |
| [postValidate](post-validate.md) | `abstract fun postValidate(context: `[`Context`](../../com.github.ajalt.clikt.core/-context/index.md)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Called after all of a command's parameters have been [finalize](finalize.md)d to perform validation of the final value. |

### Inheritors

| Name | Summary |
|---|---|
| [ArgumentDelegate](../-argument-delegate/index.md) | `interface ArgumentDelegate<out T> : `[`Argument`](./index.md)`, `[`ReadOnlyProperty`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-only-property/index.html)`<`[`CliktCommand`](../../com.github.ajalt.clikt.core/-clikt-command/index.md)`, `[`T`](../-argument-delegate/index.md#T)`>`<br>An argument that functions as a property delegate |
| [ArgumentTransformContext](../-argument-transform-context/index.md) | `class ArgumentTransformContext : `[`Argument`](./index.md)<br>A receiver for argument transformers. |
