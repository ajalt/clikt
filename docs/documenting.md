# Documenting Scripts

Clikt takes care of creating formatted help messages for commands.
There are a number of ways to customize the default behavior.
You can also implement your own [`HelpFormatter`][HelpFormatter]
and set it on the [command's context][customizing-contexts].

## Help Texts

[Commands][Commands] and parameters accept a `help` argument. Commands also accept an `epilog`
argument, which is printed after the parameters and commands on the help page. All text is
automatically trimmed of leading indentation and re-wrapped to the terminal width.

As an alternative to passing your help strings as function arguments, you can also use the `help()`
extensions for your options, and override `commandHelp` and `commandHelpEpilog` on your commands.

=== "Example"
    ```kotlin
    class Hello : CliktCommand(help = """
        This script prints NAME COUNT times.

        COUNT must be a positive number, and defaults to 1.
        """
    ) {
        val count by option("-c", "--count", metavar="COUNT", help = "number of greetings").int().default(1)
        val name by argument()
        override fun run() = repeat(count) { echo("Hello $name!") }
    }
    ```

=== "Alternate style"
    ```kotlin
    class Hello : CliktCommand() {
        override val commandHelp = """
            This script prints NAME COUNT times.

            COUNT must be a positive number, and defaults to 1.
        """
        val count by option("-c", "--count", metavar="COUNT").int().default(1)
            .help("number of greetings")
        val name by argument()
        override fun run() = repeat(count) { echo("Hello $name!") }
    }
    ```

=== "Help output"
    ```text
    $ ./hello --help
    Usage: hello [OPTIONS] NAME

      This script prints NAME COUNT times.

      COUNT must be a positive number, and defaults to 1.

    Options:
      -c, --count COUNT number of greetings
      -h, --help        Show this message and exit
    ```

Option names and metavars will appear in help output even if no help
string is specified for them. On the other hand, arguments only appear
in the usage string. It is possible to add a help string to arguments
which will be added to the help page, but the Unix convention is to just
describe arguments in the command help.

## Preformatting Paragraphs

By default, Clikt will rewrap all paragraphs in your text to the terminal width. This can be
undesirable if you have some preformatted text, such as source code or a bulleted list.

You can preformat a paragraph by surrounding it with markdown-style triple backticks. The backticks
will be removed from the output, and if the backticks are on a line by themselves, the line will be
removed. All whitespace and newlines in the paragraph will be preserved, and will be be rewrapped.


=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand(help = """This is my command.

          This paragraph will be wrapped, but the following list will not:

          ```
          - This is a list
          - Its newlines will remain intact
          ```

          This is a new paragraph that will be wrapped if it's wider than the teminal width.
          """)
    ```

=== "Help output"
    ```text
    Usage: tool

      This is my command.

      This paragraph will be wrapped, but the following list
      will not:

      - This is a list
      - It's newlines will remain intact

      This is a new paragraph that will be wrapped if it's wider
      than the terminal width.

    Options:
      -h, --help  Show this message and exit
    ```

## Manual Line Breaks

If you want to insert a line break manually without preformmating the entire paragraph, you can use
the [Unicode Next Line (NEL) character][nel]. You can type a NEL with the unicode literal `\u0085`.

Clikt will treat NEL similarly to how `<br>` behaves in HTML: The NEL will be replaced with a line
break in the output, and the paragraph will still be wrapped to the terminal width.

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        val option by option(
            help="This help will be at least two lines.\u0085(this will start a new line)"
        )
    }
    ```

=== "Help output"
    ```text
    Usage: tool

    Options:
      --option    This help will be at least
                  two lines.
                  (this will start a new
                  line)
      -h, --help  Show this message and exit
    ```

## Subcommand Short Help

Subcommands are listed in the help page based on their [name][customizing-command-name].
They have a short help string which is the first line of their help.

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand()

    class Execute : NoOpCliktCommand(help = """
        Execute the command.

        The command will be executed.
        """)

    class Abort : NoOpCliktCommand(help="Kill any running commands.")
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [OPTIONS] COMMAND [ARGS]...

    Options:
      -h, --help  Show this message and exit

    Commands:
      execute  Execute the command.
      abort    Kill any running commands.
    ```


## Help Option Customization

Clikt handles the help option is specially. It is added automatically to
every command. Any help option name that conflicts with another option is
not used for the help option. If the help option has no unique names, it
is not added.

You can change the help option's name and help message on the
[command's context][customizing-contexts]:

=== "Example"
    ```kotlin
    class HelpLocalization: Localization {
        override fun helpOptionMessage(): String = "show the help"
    }

    class Tool : NoOpCliktCommand() {
        init {
            context {
                helpOptionNames = setOf("/help")
                localization = HelpLocalization()
            }
        }
    }
    ```

=== "Usage"
    ```text
    $ ./tool /help
    Usage: tool [OPTIONS]

    Options:
      /help  show the help
    ```

If you don't want a help option to be added, you can set
`helpOptionNames = emptySet()`

## Default Values in Help

You can configure the help formatter to show default values in the help output by passing
`showRequiredTag = true` to the `CliktHelpFormatter`. By default, the string value of the
default value will be shown. You can show a different value by passing the value you want to show to
the `defaultForHelp` parameter of [`default`][default].

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        init {
            context { helpFormatter = CliktHelpFormatter(showDefaultValues = true) }
        }

        val a by option(help = "this is optional").default("value")
        val b by option(help = "this is also optional").default("value", defaultForHelp="chosen for you")
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [OPTIONS]

    Options:
      --a TEXT    this is optional (default: value)
      --b TEXT    this is also optional (default: chosen for you)
    ```


## Required Options in Help

By default, [`required`][required] options are displayed the same way as other options. The help
formatter includes two different ways to show that an option is required.

### Required Option Marker

You can pass a character to the `requiredOptionMarker` argument of the `CliktHelpFormatter`.

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        init {
            context { helpFormatter = CliktHelpFormatter(requiredOptionMarker = "*") }
        }

        val option by option(help = "this is optional")
        val required by option(help = "this is required").required()
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [OPTIONS]

    Options:
      --option TEXT    this is optional
    * --required TEXT  this is required
      -h, --help       Show this message and exit
    ```

### Required Option Tag

You can also show a tag for required options by passing `showRequiredTag = true` to the `CliktHelpFormatter`.

=== "Example"
    ```kotlin
    class Tool : CliktCommand() {
        init {
            context { helpFormatter = CliktHelpFormatter(showRequiredTag = true) }
        }

        val option by option(help = "this is optional")
        val required by option(help = "this is required").required()
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [OPTIONS]

    Options:
      --option TEXT    this is optional
      --required TEXT  this is required (required)
      -h, --help       Show this message and exit
    ```

## Grouping Options in Help

You can group options into separate help sections by using [OptionGroup][OptionGroup]
and importing [groups.provideDelegate][groups.provideDelegate]. The name of
the group will be shown in the output. You can also add an extra help message to be shown with the
group. Groups can't be nested.

=== "Example"
    ```kotlin
    import com.github.ajalt.clikt.parameters.groups.provideDelegate

    class UserOptions : OptionGroup(
            name = "User Options",
            help = "Options controlling the user"
    ) {
        val name by option(help = "user name")
        val age by option(help = "user age").int()
    }

    class Tool : NoOpCliktCommand() {
        val userOptions by UserOptions()
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: cli [OPTIONS]

    User Options:

      Options controlling the user

      --name TEXT  user name
      --age INT    user age

    Options:
      -h, --help  Show this message and exit
    ```

## Suggesting Corrections for Mistyped Parameters

When an option or subcommand is mistyped, Clikt will suggest corrections that are similar to the typed value.

=== "Mistyped Option"
    ```text
    $ ./cli --sise=5
    Error: no such option: "--sise". Did you mean "--size"?
    ```

=== "Mistyped Subcommand"
    ```text
    $ ./cli building
    Usage: cli [OPTIONS] COMMAND [ARGS]...

    Error: no such subcommand: "building". Did you mean "build"?
    ```

By default, Clikt will suggest corrections of any similar option or subcommand name based on a
similarity metric. You can customize the suggestions by setting a `correctionSuggestor` on your
command's context.

```kotlin
class Cli : NoOpCliktCommand() {
    init {
        context {
            // Only suggest corrections that start with the entered value
            correctionSuggestor = { enteredValue, possibleValues ->
                possibleValues.filter { it.startsWith(enteredValue) }
            }
        }
    }
}
```

## Localization

You can localize error messages by implementing [`Localization`][Localization] and setting the
[`localization`][Context.localization] property on your context.

=== "Example"
    ```kotlin
    class CursiveLocalization : Localization {
        override fun usageTitle() = "𝒰𝓈𝒶𝑔𝑒:"
        override fun optionsTitle() = "𝒪𝓅𝓉𝒾𝑜𝓃𝓈:"
        override fun optionsMetavar() = "[𝒪𝒫𝒯𝐼𝒪𝒩𝒮]:"
        override fun helpOptionMessage() = "𝒮𝒽𝑜𝓌 𝓉𝒽𝒾𝓈 𝓂𝑒𝓈𝓈𝒶𝑔𝑒 𝒶𝓃𝒹 𝑒𝓍𝒾𝓉"

        // ... override the rest of the strings here
    }

    class I18NTool : NoOpCliktCommand(help = "𝒯𝒽𝒾𝓈 𝓉𝑜𝑜𝓁 𝒾𝓈 𝒾𝓃 𝒸𝓊𝓇𝓈𝒾𝓋𝑒") {
        init {
            context { localization = CursiveLocalization() }
        }
    }
    ```

=== "Usage"
    ```text
    $ ./i18ntool --help
    𝒰𝓈𝒶𝑔𝑒: i18ntool [𝒪𝒫𝒯𝐼𝒪𝒩𝒮]

      𝒯𝒽𝒾𝓈 𝓉𝑜𝑜𝓁 𝒾𝓈 𝒾𝓃 𝒸𝓊𝓇𝓈𝒾𝓋𝑒

    𝒪𝓅𝓉𝒾𝑜𝓃𝓈:
      -h, --help  𝒮𝒽𝑜𝓌 𝓉𝒽𝒾𝓈 𝓂𝑒𝓈𝓈𝒶𝑔𝑒 𝒶𝓃𝒹 𝑒𝓍𝒾𝓉
    ```

[CliktHelpFormatter]:       api/clikt/com.github.ajalt.clikt.output/-clikt-help-formatter/index.html
[Commands]:                 api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.html
[Context.localization]:     api/clikt/com.github.ajalt.clikt.core/-context/-builder/localization.html
[customizing-command-name]: commands.md#customizing-command-name
[customizing-contexts]:     commands.md#customizing-contexts
[default]:                  api/clikt/com.github.ajalt.clikt.parameters.options/default.html
[groups.provideDelegate]:   api/clikt/com.github.ajalt.clikt.parameters.groups/provide-delegate.html
[HelpFormatter]:            api/clikt/com.github.ajalt.clikt.output/-help-formatter/index.html
[Localization]:             api/clikt/com.github.ajalt.clikt.output/-localization/index.html
[nel]:                      https://www.fileformat.info/info/unicode/char/0085/index.htm
[OptionGroup]:              api/clikt/com.github.ajalt.clikt.parameters.groups/-option-group/index.html
[provideDelegate]:          api/clikt/com.github.ajalt.clikt.parameters.groups/provide-delegate.html
[required]:                 api/clikt/com.github.ajalt.clikt.parameters.options/required.html
