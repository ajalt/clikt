[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [TermUi](index.md) / [confirm](./confirm.md)

# confirm

`fun confirm(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, default: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, abort: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, promptSuffix: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ": ", showDefault: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, console: `[`CliktConsole`](../-clikt-console/index.md)` = defaultCliktConsole()): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)`?`

Prompt for user confirmation.

Responses will be read from stdin, even if it's redirected to a file.

### Parameters

`text` - the question to ask

`default` - the default, used if stdin is empty

`abort` - if `true`, a negative answer aborts the program by raising [Abort](../../com.github.ajalt.clikt.core/-abort/index.md)

`promptSuffix` - a string added after the question and choices

`showDefault` - if false, the choices will not be shown in the prompt.

**Return**
the user's response, or null if stdin is not interactive and EOF was encountered.

