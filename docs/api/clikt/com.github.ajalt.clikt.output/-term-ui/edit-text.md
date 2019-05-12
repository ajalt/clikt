[clikt](../../index.md) / [com.github.ajalt.clikt.output](../index.md) / [TermUi](index.md) / [editText](./edit-text.md)

# editText

`fun editText(text: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, editor: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, env: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap(), requireSave: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, extension: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = ".txt"): `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?`

Edit [text](edit-text.md#com.github.ajalt.clikt.output.TermUi$editText(kotlin.String, kotlin.String, kotlin.collections.Map((kotlin.String, )), kotlin.Boolean, kotlin.String)/text) in the [editor](edit-text.md#com.github.ajalt.clikt.output.TermUi$editText(kotlin.String, kotlin.String, kotlin.collections.Map((kotlin.String, )), kotlin.Boolean, kotlin.String)/editor).

This blocks until the editor is closed.

### Parameters

`text` - The text to edit.

`editor` - The path to the editor to use. Defaults to automatic detection.

`env` - Environment variables to forward to the editor.

`requireSave` - If the editor is closed without saving, null will be returned if true, otherwise
[text](edit-text.md#com.github.ajalt.clikt.output.TermUi$editText(kotlin.String, kotlin.String, kotlin.collections.Map((kotlin.String, )), kotlin.Boolean, kotlin.String)/text) will be returned.

`extension` - The extension of the temporary file that the editor will open. This can affect syntax
coloring etc.

### Exceptions

`CliktError` - if the editor cannot be opened.

**Return**
The edited text, or null if [requireSave](edit-text.md#com.github.ajalt.clikt.output.TermUi$editText(kotlin.String, kotlin.String, kotlin.collections.Map((kotlin.String, )), kotlin.Boolean, kotlin.String)/requireSave) is true and the editor was closed without saving.

