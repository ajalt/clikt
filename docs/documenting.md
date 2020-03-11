# Documenting Scripts

Clikt takes care of creating formatted help messages for commands.
There are a number of ways to customize the default behavior.
You can also implement your own [`HelpFormatter`][HelpFormatter]
and set it on the [command's context][customizing-contexts].

## Help Texts

[Commands][Commands] and parameters accept a `help` argument. Commands also accept an `epilog`
argument, which is printed after the parameters and commands on the help page. All text is
automatically trimmed of leading indentation and re-wrapped to the terminal width.

```kotlin tab="Example"
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

```text tab="Usage"
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


```kotlin tab="Example"
class Tool : NoOpCliktCommand(help = """This is my command.

      This paragraph will be wrapped, but the following list will not:

      ```
      - This is a list
      - Its newlines will remain intact
      ```

      This is a new paragraph that will be wrapped if it's wider than the teminal width.
      """)
```

```text tab="Help output"
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

## Subcommand Short Help

Subcommands are listed in the help page based on their [name][customizing-command-name].
They have a short help string which is the first line of their help.

```kotlin tab="Example"
class Tool : NoOpCliktCommand()

class Execute : NoOpCliktCommand(help = """
    Execute the command.

    The command will be executed.
    """)

class Abort : NoOpCliktCommand(help="Kill any running commands.")
```

```text tab="Usage"
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

```kotlin tab="Example"
class Tool : NoOpCliktCommand() {
    init {
        context {
            helpOptionNames = setOf("/help")
            helpOptionMessage = "show the help"
        }
    }
}
```

```text tab="Usage"
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

```kotlin tab="Example"
class Tool : NoOpCliktCommand() {
    init {
        context { helpFormatter = CliktHelpFormatter(showDefaultValues = true) }
    }

    val a by option(help = "this is optional").default("value")
    val b by option(help = "this is also optional").default("value", defaultForHelp="chosen for you")
}
```

```text tab="Usage"
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

```kotlin tab="Example"
class Tool : NoOpCliktCommand() {
    init {
        context { helpFormatter = CliktHelpFormatter(requiredOptionMarker = "*") }
    }

    val option by option(help = "this is optional")
    val required by option(help = "this is required").required()
}
```

```text tab="Usage"
$ ./tool --help
Usage: tool [OPTIONS]

Options:
  --option TEXT    this is optional
* --required TEXT  this is required
  -h, --help       Show this message and exit
```

### Required Option Tag

You can also show a tag for required options by passing `showRequiredTag = true` to the `CliktHelpFormatter`.

```kotlin tab="Example"
class Tool : CliktCommand() {
    init {
        context { helpFormatter = CliktHelpFormatter(showRequiredTag = true) }
    }

    val option by option(help = "this is optional")
    val required by option(help = "this is required").required()
}
```

```text tab="Usage"
$ ./tool --help
Usage: tool [OPTIONS]

Options:
  --option TEXT    this is optional
  --required TEXT  this is required (required)
  -h, --help       Show this message and exit
```

## Grouping Options in Help

You can group options into separate help sections by using [OptionGroup][OptionGroup]. The name of
the group will be shown in the output. You can also add an extra help message to be shown with the
group. Groups can't be nested.

```kotlin tab="Example"
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

```text tab="Usage"
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

```text tab="Mistyped Option"
$ ./cli --sise=5
Error: no such option: "--sise". Did you mean "--size"?
```

```text tab="Mistyped Subcommand"
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

[HelpFormatter]:            api/clikt/com.github.ajalt.clikt.output/-help-formatter/index.md
[Commands]:                 api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.md
[customizing-command-name]: commands.md#customizing-command-name
[customizing-contexts]:     commands.md#customizing-contexts
[default]:                  api/clikt/com.github.ajalt.clikt.parameters.options/default.md
[required]:                 api/clikt/com.github.ajalt.clikt.parameters.options/required.md
[OptionGroup]:              api/clikt/com.github.ajalt.clikt.parameters.groups/-option-group/index.md
[provideDelegate]:          api/clikt/com.github.ajalt.clikt.parameters.groups/provide-delegate.md
