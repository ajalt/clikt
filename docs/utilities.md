# Utilities

Writing command line interfaces often involves more than just parsing
the command line. Clikt also provides functions to perform actions
commonly used in command line programs.

## Launching Editors

If you need to ask users for multi-line input, or need to have the user edit a file, you can do so
through [`editText`][editText] and [`editFile`][editFile]. These functions open the
program defined in the `VISUAL` or `EDITOR` environment variables, or a sensible default if neither
are defined. The functions return the edited text if the user saved their changes.

=== "Example"
    ```kotlin
    fun getCommitMessage(): String? {
        val message = """
        # Enter your message.
        # Lines starting with # are ignored
        """.trimIndent()
        return editText(message, requireSave = true)
                ?.replace(Regex("#[^\n]*\n"), "")
    }
    ```

## Input Prompts

Options can [prompt for values automatically][prompting-for-input], but you can also do so manually
by using Mordant's prompt functionality directly. By default, it accepts any input string, but you
can also pass in a conversion function. If the conversion returns a `ConversionResult.Invalid`, the
prompt will ask the user to enter a different value.

=== "Example"
    ```kotlin
    val input = terminal.prompt("Enter a number") {
        it.toIntOrNull()
            ?.let { ConversionResult.Valid(it) }
            ?: ConversionResult.Invalid("$it is not a valid integer")
    }
    echo("Twice your number is ${input * 2}")
    ```

=== "Interactive Session"
    ```text
    Enter a number: foo
    Error: foo is not a valid integer
    Enter a number: 11
    Twice your number is 22
    ```

## Confirmation Prompts

You can also ask the user for a yes or no response with Mordant's [`YesNoPrompt`][YesNoPrompt]:

```kotlin
if (YesNoPrompt("Continue?", terminal).ask() == true) {
    echo("Ok!")
}
```


[confirm]:             api/clikt/com.github.ajalt.clikt.core/-clikt-command/confirm.html
[editFile]:            api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-file.html
[editText]:            api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-text.html
[prompt]:              api/clikt/com.github.ajalt.clikt.core/-clikt-command/prompt.html
[prompting-for-input]: options.md#prompting-for-input
[UsageError]:          api/clikt/com.github.ajalt.clikt.core/-usage-error/index.html
[YesNoPrompt]:         https://ajalt.github.io/mordant/api/mordant/com.github.ajalt.mordant.terminal/-yes-no-prompt/index.html
