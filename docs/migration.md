# Upgrading to Newer Releases

## Upgrading to 5.0

## `main` is now an extension

The `CliktCommand.main` and `CliktCommand.parse` methods are now a extension functions, so you'll
need to import them.

```diff
+ import com.github.ajalt.clikt.core.main
fun main(args: Array<String>) = MyCommand().main(args)
```

## `CliktCommand` constructor no longer takes most parameters

All parameters of the `CliktCommand` except for `name` have been moved to open properties.

=== "In 5.0"
    ```kotlin
    class MyCommand : CliktCommand(name="mycommand") {
        override fun help(context: Context) = "command help"
        override fun helpEpilog(context: Context) = "command epilog"
        override val invokeWithoutSubcommand = true
        override val printHelpOnEmptyArgs = true
        override val helpTags = mapOf("tag" to "value")
        override val autoCompleteEnvvar = "MYCOMMAND_COMPLETE"
        override val allowMultipleSubcommands = true
        override val treatUnknownOptionsAsArgs = true
        override val hiddenFromHelp = true
    }
    ```

=== "In 4.0"
    ```kotlin
    class MyCommand : CliktCommand(
        name = "mycommand",
        help = "command help",
        helpEpilog = "command epilog",
        invokeWithoutSubcommand = true,
        printHelpOnEmptyArgs = true,
        helpTags = mapOf("tag" to "value"),
        autoCompleteEnvvar = "MYCOMMAND_COMPLETE",
        allowMultipleSubcommands = true,
        treatUnknownOptionsAsArgs = true,
        hiddenFromHelp = true,
    ) {
    }
    ```

The full list of moved parameters:

| removed parameter           | new replacement property        |
|-----------------------------|---------------------------------|
| `help`                      | `fun help`                      |
| `epilog`                    | `fun helpEpilog`                |
| `invokeWithoutSubcommand`   | `val invokeWithoutSubcommand`   |
| `printHelpOnEmptyArgs`      | `val printHelpOnEmptyArgs`      |
| `helpTags`                  | `val helpTags`                  |
| `autoCompleteEnvvar`        | `val autoCompleteEnvvar`        |
| `allowMultipleSubcommands`  | `val allowMultipleSubcommands`  |
| `treatUnknownOptionsAsArgs` | `val treatUnknownOptionsAsArgs` |
| `hidden`                    | `val hiddenFromHelp`            |

## Upgrading to 4.0

### Help formatting

The `CliktHelpFormatter` class has been removed and replaced with the `MordantHelpFormatter`. The
`MordantHelpFormatter` constructor takes a `Context` instead of a `Localization`, and the parameters
controlling size and spacing have been removed. See the [documentation][documenting] for details on
how to set the help formatter on the Context.

If you were subclassing `CliktHelpFormatter`, `MordantHelpFormatter`'s open methods are different.
See the [`helpformat`][helpformat] sample for an example of how to use the new formatter.

### Prompting

The `CliktConsole` class has been removed. If you were using it, use your command's Mordant
`terminal` instead.

The `prompt` and `confirm` methods now use Mordant's prompting functionality, and some of their
arguments have changed. In particular, conversion lambdas now return a `ConversionResult`  instead
of throwing an exception.

=== "In 4.0"
    ```kotlin
    val input = prompt("Enter a number") {
        it.toIntOrNull()
            ?.let { ConversionResult.Valid(it) }
            ?: ConversionResult.Invalid("$it is not a valid integer")
    }
    ```

=== "In 3.0"
    ```kotlin
    val input = prompt("Enter a number") {
        it.toIntOrNull() ?: throw BadParameterValue("$it is not a valid integer")
    }
    ```

## Upgrading to 3.0

### Maven Coordinates

Clikt's Maven groupId changed from `com.github.ajalt` to `com.github.ajalt.clikt`. So the full
coordinate is now `com.github.ajalt.clikt:clikt:3.0.0`.

With the new Multiplatform plugin in Kotlin 1.4, there is no longer a separate `clikt-multiplatform`
artifact. You can use `com.github.ajalt.clikt:clikt:3.0.0` for both JVM-only and Multiplatform projects.


### Environment variable splitting

There used to be an `envvarSplit` parameter to `option()` and its `convert()` that would split
values coming from an environment variable. This parameter is removed, and values from environment
variables are no longer split automatically.

If you still want to split option values, you can do so explicitly with [`split()`][split].

### Experimental APIs

The Value Source API and Completion Generation APIs no longer require opt-in. You can use these APIs
without needing the `ExperimentalValueSourceApi` or `ExperimentalCompletionCandidates` annotations.

### Localization

By default, all strings are defined in the [`Localization`][Localization] object set on your
[context][Context.localization].

This means that string parameters like `usageTitle` in the constructor for
`CliktHelpFormatter` have been removed in favor of functions like
[`Localization.usageTitle()`][Localization.usageTitle].

`Context.helpOptionMessage` has also been removed in favor of
[`Localization.helpOptionMessage()`][Localization.helpOptionMessage]. See [Help Option
Customization][help-option-custom] for an example.


[Context.localization]:             api/clikt/com.github.ajalt.clikt.core/-context/-builder/localization.html
[documenting]:                      documenting.md#default-values-in-help
[helpformat]:                       https://github.com/ajalt/clikt/tree/master/samples/helpformat
[help-option-custom]:               documenting.md#help-option-customization
[Localization]:                     api/clikt/com.github.ajalt.clikt.output/-localization/index.html
[Localization.usageTitle]:          api/clikt/com.github.ajalt.clikt.output/-localization/usage-title.html
[Localization.helpOptionMessage]:   api/clikt/com.github.ajalt.clikt.output/-localization/help-option-message.html
[split]:                            api/clikt/com.github.ajalt.clikt.parameters.options/split.html
