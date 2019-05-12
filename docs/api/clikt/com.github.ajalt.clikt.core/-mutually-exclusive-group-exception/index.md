[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [MutuallyExclusiveGroupException](./index.md)

# MutuallyExclusiveGroupException

`open class MutuallyExclusiveGroupException : `[`UsageError`](../-usage-error/index.md)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `MutuallyExclusiveGroupException(names: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>, context: `[`Context`](../-context/index.md)`? = null)` |

### Properties

| Name | Summary |
|---|---|
| [names](names.md) | `val names: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` |

### Inherited Properties

| Name | Summary |
|---|---|
| [argument](../-usage-error/argument.md) | `var argument: `[`Argument`](../../com.github.ajalt.clikt.parameters.arguments/-argument/index.md)`?`<br>The argument that caused this error. This may be set after the error is thrown. |
| [context](../-usage-error/context.md) | `var context: `[`Context`](../-context/index.md)`?` |
| [message](../-usage-error/message.md) | `open val message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [option](../-usage-error/option.md) | `var option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`?`<br>The option that caused this error. This may be set after the error is thrown. |
| [paramName](../-usage-error/param-name.md) | `var paramName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the parameter that caused the error. If possible, this should be set to the actual name used. If not set, it will be inferred from [argument](../-usage-error/argument.md) or [option](../-usage-error/option.md) if either is set. |
| [text](../-usage-error/text.md) | `val text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>Extra text to add to the message. Not all subclasses uses this. |

### Functions

| Name | Summary |
|---|---|
| [formatMessage](format-message.md) | `open fun formatMessage(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Inherited Functions

| Name | Summary |
|---|---|
| [helpMessage](../-usage-error/help-message.md) | `fun helpMessage(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [inferParamName](../-usage-error/infer-param-name.md) | `fun inferParamName(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
