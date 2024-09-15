# Documenting Scripts

Clikt takes care of creating formatted help messages for commands.
There are a number of ways to customize the default behavior.
You can also implement your own [`HelpFormatter`][HelpFormatter]
and set it on the [command's context][customizing-contexts].

## Help Texts

You can add help text to commands and parameters. For parameters, you can pass a `help` string or 
use the `help()` extension. For commands, you can override the `help` and `helpEpilog` methods.

=== "Example"
    ```kotlin
    class Hello : CliktCommand() {
        override fun help(context: Context) = """
        This script prints <name> <count> times.
    
        <count> must be a positive number, and defaults to 1.
        """.trimIndent()
        val count by option("-c", "--count", metavar="count", help="number of greetings")
            .int().default(1)
        val name by argument(help="The name to greet")
        override fun run() = repeat(count) { echo("Hello $commandName!") }
    }
    ```

=== "Alternate style"
    ```kotlin
    class Hello : CliktCommand() {
        override fun help(context: Context): String {
            val style = context.theme.info
            return """
            This script prints ${style("<name>")} ${style("<count>")} times.
    
            ${style("<count>")} must be a positive number, and defaults to 1.
            """.trimIndent()
        }
    
        val count by option("-c", "--count", metavar="count").int().default(1)
            .help { theme.success("number of greetings") }
        val name by argument()
            .help("The name to greet")
        override fun run() = repeat(count) { echo("Hello $name!") }
    }
    ```

=== "Help output"
    ```text
    $ ./hello --help
    Usage: hello [<options>] <name>

      This script prints <name> <count> times.

      <count> must be a positive number, and defaults to 1.

    Options:
      -c, --count <count> number of greetings
      -h, --help          Show this message and exit
    ```

Option names and metavars will appear in help output even if no help
string is specified for them. On the other hand, arguments only appear
in the usage string. It is possible to add a help string to arguments
which will be added to the help page, but the Unix convention is to just
describe arguments in the command help.

## Markdown in help texts

You can configure Clikt to use Mordant to render Markdown in help texts. You can use all the normal
markdown features, such as lists, tables, and even hyperlinks if your terminal supports them.

First, add the `:clitk-markdown` dependency to your project:

```kotlin
dependencies {
   implementation("com.github.ajalt.clikt:clikt-markdown:$cliktVersion")
}
```

And install the markdown help formatter on your command:

```kotlin
val command = MyCommand().installMordantMarkdown()
```

Then you can use markdown in your help strings:

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        init {
            installMordantMarkdown()
        }
        val option by option().help {
            """
            | This | is | a | table |
            | ---- | -- | - | ----- |
            | 1    | 2  | 3 | 4     |
            
            - This is
            - a list
            
            ```
            You can
                use code blocks
            ```
            """.trimIndent()
        }
    }
    ```

=== "Help output"
    ```text
    Usage: tool [<options>]
    
    Options:
      --option=<text>  ┌──────┬────┬───┬───────┐
                       │ This │ is │ a │ table │
                       ╞══════╪════╪═══╪═══════╡
                       │ 1    │ 2  │ 3 │ 4     │
                       └──────┴────┴───┴───────┘
    
                        • This is
                        • a list
    
                       ╭───────────────────╮
                       │You can            │
                       │    use code blocks│
                       ╰───────────────────╯
      -h, --help       Show this message and exit
    ```

## Manual Line Breaks

If you want to insert a line break manually without preformatting the entire paragraph, you can use
the [Unicode Next Line (NEL) character][nel]. You can type a NEL with the unicode literal `\u0085`.

Clikt will treat NEL similarly to how `<br>` behaves in HTML: The NEL will be replaced with a line
break in the output, and the paragraph will still be wrapped to the terminal width.

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        val option by option()
            .help("This help will be at least two lines.\u0085(this will start a new line)")
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


!!! tip

    In raw multiline strings (which do not parse escape sequences),
    you'll need to insert the NEL with a string template such as `${"\u0085"}`.

## Subcommand Short Help

Subcommands are listed in the help page based on their [name][customizing-command-name].
They have a short help string which is the first line of their help.

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand()

    class Execute : NoOpCliktCommand() {
        override fun help(context: Context) = """
            Execute the command.

            The command will be executed.
            """.trimIndent()
    }

    class Abort : NoOpCliktCommand() {
        override fun help(context: Context) = "Kill any running commands."
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [<options>] <command> [<args>]...

    Options:
      -h, --help  Show this message and exit

    Commands:
      execute  Execute the command.
      abort    Kill any running commands.
    ```


## Help Option Customization

Clikt adds a help option to every command automatically. It uses the names `-h` and `--help` and
prints the command's help message when invoked. 

### Changing the help option names

Any help option name that conflicts with another option is not used for the help option. If the help
option has no unique names, it is not added.

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
    Usage: tool [<options>]

    Options:
      /help  show the help
    ```

If you don't want a help option to be added, you can set
`helpOptionNames = emptySet()`


### Changing the help option behavior

If you want to run some code when the help option is invoked, or change its behavior, you can define
the option yourself. The default help option is an [eager option][eager-options] that throws a
[PrintHelpMessage], so if you wanted to log some information when the help option is invoked, you
could do something like this:

=== "Example"
    ```kotlin
    class CustomHelpCommand : TestCommand() {
        init {
            eagerOption("-h", "--help", help="Show this message and exit") {
                echo("about to print help")
                throw PrintHelpMessage(context)
            }
        }
    }
    ```

=== "Example 2"
    ```kotlin
    // If you want to use the help message from the localization, you can register an option
    // with eager=true and use the lazy `help` method.
    class CustomHelpCommand : TestCommand() {
        init {
            registerOption(
                option("-h", "--help", eager=true).flag()
                    .help { context.localization.helpOptionMessage() }
                    .validate {
                        if(it) {
                            echo("about to print help")
                            throw PrintHelpMessage(context)
                        }
                    }
            )
        }
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    about to print help
    Usage: custom-help [<options>]

    Options:
      -h, --help  Show this message and exit
    ```

!!! warning
    Eager options can't reference other options or arguments, since they're evaluated before parsing
    the rest of the command line.

## Default Values in Help

You can configure the help formatter to show default values in the help output by passing
`showDefaultValues = true` to the `MordantHelpFormatter`. By default, the string value of the
default value will be shown. You can show a different value by passing the value you want to show to
the `defaultForHelp` parameter of [`default`][default].

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        init {
            context {
                helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true) }
            }
        }

        val a by option(help = "this is optional").default("value")
        val b by option(help = "this is also optional").default("value", defaultForHelp="chosen for you")
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [<options>]

    Options:
      --a <text>    this is optional (default: value)
      --b <text>    this is also optional (default: chosen for you)
    ```


## Required Options in Help

By default, [`required`][required] options are displayed the same way as other options. The help
formatter includes two different ways to show that an option is required.

### Required Option Marker

You can pass a character to the `requiredOptionMarker` argument of the `MordantHelpFormatter`.

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        init {
            context {
                helpFormatter = { MordantHelpFormatter(it, requiredOptionMarker = "*") }
            }
        }

        val option by option(help = "this is optional")
        val required by option(help = "this is required").required()
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [<options>]

    Options:
      --option <text>    this is optional
    * --required <text>  this is required
      -h, --help         Show this message and exit
    ```

### Required Option Tag

You can also show a tag for required options by passing `showRequiredTag = true` to the
`MordantHelpFormatter`.

=== "Example"
    ```kotlin
    class Tool : CliktCommand() {
        init {
            context {
                helpFormatter = { MordantHelpFormatter(it, showRequiredTag = true) }
            }
        }

        val option by option(help = "this is optional")
        val required by option(help = "this is required").required()
    }
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [<options>]

    Options:
      --option <text>    this is optional
      --required <text>  this is required (required)
      -h, --help         Show this message and exit
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
    Usage: cli [<options>]

    User Options:

      Options controlling the user

      --name <text>  user name
      --age <int>    user age

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
    Usage: cli [<options>] <command> [<args>]...

    Error: no such subcommand: "building". Did you mean "build"?
    ```

By default, Clikt will suggest corrections of any similar option or subcommand name based on a
similarity metric. You can customize the suggestions by setting `suggestTypoCorrection` on your
command's context.

```kotlin
class Cli : NoOpCliktCommand() {
    init {
        context {
            // Only suggest corrections that start with the entered value
            suggestTypoCorrection = { enteredValue, possibleValues ->
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
        override fun optionsTitle() = "𝒪𝓅𝓉𝒾𝑜𝓃𝓈"
        override fun optionsMetavar() = "𝑜𝓅𝓉𝒾𝑜𝓃𝓈"
        override fun helpOptionMessage() = "𝒮𝒽𝑜𝓌 𝓉𝒽𝒾𝓈 𝓂𝑒𝓈𝓈𝒶𝑔𝑒 𝒶𝓃𝒹 𝑒𝓍𝒾𝓉"

        // ... override the rest of the strings here
    }

    class I18NTool : NoOpCliktCommand() {
        override fun help(context: Context) = "𝒯𝒽𝒾𝓈 𝓉𝑜𝑜𝓁 𝒾𝓈 𝒾𝓃 𝒸𝓊𝓇𝓈𝒾𝓋𝑒"
        init { context { localization = CursiveLocalization() } }
    }
    ```

=== "Usage"
    ```text
    $ ./i18ntool --help
    𝒰𝓈𝒶𝑔𝑒: i18ntool [<𝑜𝓅𝓉𝒾𝑜𝓃𝓈>]

      𝒯𝒽𝒾𝓈 𝓉𝑜𝑜𝓁 𝒾𝓈 𝒾𝓃 𝒸𝓊𝓇𝓈𝒾𝓋𝑒

    𝒪𝓅𝓉𝒾𝑜𝓃𝓈:
      -h, --help  𝒮𝒽𝑜𝓌 𝓉𝒽𝒾𝓈 𝓂𝑒𝓈𝓈𝒶𝑔𝑒 𝒶𝓃𝒹 𝑒𝓍𝒾𝓉
    ```

[Commands]:                 api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.html
[Context.localization]:     api/clikt/com.github.ajalt.clikt.core/-context/-builder/localization.html
[HelpFormatter]:            api/clikt/com.github.ajalt.clikt.output/-help-formatter/index.html
[Localization]:             api/clikt/com.github.ajalt.clikt.output/-localization/index.html
[OptionGroup]:              api/clikt/com.github.ajalt.clikt.parameters.groups/-option-group/index.html
[PrintHelpMessage]:         api/clikt/com.github.ajalt.clikt.core/-print-help-message/index.html
[customizing-command-name]: commands.md#customizing-command-name
[customizing-contexts]:     commands.md#customizing-contexts
[default]:                  api/clikt/com.github.ajalt.clikt.parameters.options/default.html
[eager-options]:            options.md#eager-options
[groups.provideDelegate]:   api/clikt/com.github.ajalt.clikt.parameters.groups/provide-delegate.html
[nel]:                      https://www.fileformat.info/info/unicode/char/0085/index.htm
[provideDelegate]:          api/clikt/com.github.ajalt.clikt.parameters.groups/provide-delegate.html
[required]:                 api/clikt/com.github.ajalt.clikt.parameters.options/required.html
