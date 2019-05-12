[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [UsageError](./index.md)

# UsageError

`open class UsageError : `[`CliktError`](../-clikt-error/index.md)

An internal exception that signals a usage error.

The [option](option.md) and [argument](argument.md) properties are used in message formatting, and can be set after the exception
is created. If this is thrown inside a call to [convert](../../com.github.ajalt.clikt.parameters.arguments/convert.md), the [argument](argument.md) or [option](option.md) value will be set
automatically

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `UsageError(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, paramName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, context: `[`Context`](../-context/index.md)`? = null)`<br>`UsageError(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, argument: `[`Argument`](../../com.github.ajalt.clikt.parameters.arguments/-argument/index.md)`, context: `[`Context`](../-context/index.md)`? = null)`<br>`UsageError(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`, context: `[`Context`](../-context/index.md)`? = null)` |

### Properties

| Name | Summary |
|---|---|
| [argument](argument.md) | `var argument: `[`Argument`](../../com.github.ajalt.clikt.parameters.arguments/-argument/index.md)`?`<br>The argument that caused this error. This may be set after the error is thrown. |
| [context](context.md) | `var context: `[`Context`](../-context/index.md)`?` |
| [message](message.md) | `open val message: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?` |
| [option](option.md) | `var option: `[`Option`](../../com.github.ajalt.clikt.parameters.options/-option/index.md)`?`<br>The option that caused this error. This may be set after the error is thrown. |
| [paramName](param-name.md) | `var paramName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>The name of the parameter that caused the error. If possible, this should be set to the actual name used. If not set, it will be inferred from [argument](argument.md) or [option](option.md) if either is set. |
| [text](text.md) | `val text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`<br>Extra text to add to the message. Not all subclasses uses this. |

### Functions

| Name | Summary |
|---|---|
| [formatMessage](format-message.md) | `open fun formatMessage(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [helpMessage](help-message.md) | `fun helpMessage(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [inferParamName](infer-param-name.md) | `fun inferParamName(): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |

### Inheritors

| Name | Summary |
|---|---|
| [BadParameterValue](../-bad-parameter-value/index.md) | `open class BadParameterValue : `[`UsageError`](./index.md)<br>A parameter was given the correct number of values, but of invalid format or type. |
| [IncorrectArgumentValueCount](../-incorrect-argument-value-count/index.md) | `open class IncorrectArgumentValueCount : `[`UsageError`](./index.md)<br>An argument was supplied but the number of values supplied was incorrect. |
| [IncorrectOptionValueCount](../-incorrect-option-value-count/index.md) | `open class IncorrectOptionValueCount : `[`UsageError`](./index.md)<br>An option was supplied but the number of values supplied to the option was incorrect. |
| [MissingParameter](../-missing-parameter/index.md) | `open class MissingParameter : `[`UsageError`](./index.md)<br>A required parameter was not provided |
| [MutuallyExclusiveGroupException](../-mutually-exclusive-group-exception/index.md) | `open class MutuallyExclusiveGroupException : `[`UsageError`](./index.md) |
| [NoSuchOption](../-no-such-option/index.md) | `open class NoSuchOption : `[`UsageError`](./index.md)<br>An option was provided that does not exist. |
