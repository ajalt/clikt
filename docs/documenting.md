# Documenting Scripts

Clikt takes care of creating formatted help messages for commands. There
are a number of ways to customize the default behavior. You can also
implement your own [`HelpFormatter`](api/clikt/com.github.ajalt.clikt.output/-help-formatter/index.html) and set it on the [command's
context](commands.html#customizing-contexts).

## Help Texts

[Commands](api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.html) and parameters accept a `help` argument. Commands also accept an
`epilog` argument, which is printed after the parameters and commands on
the help page. All text is automatically re-wrapped to the terminal
width.

```kotlin
class Hello : CliktCommand(help = """
    This script prints NAME COUNT times.

    COUNT must be a positive number, and defaults to 1.
    """) {

    val count by option("-c", "--count", help = "number of greetings").int().default(1)
    val name by argument()
    override fun run() = repeat(count) { TermUi.echo("Hello $name!") }
}
```

Which creates the following help page:

```
$ ./hello --help
Usage: hello [OPTIONS] NAME

  This script prints NAME COUNT times.

  COUNT must be a positive number, and defaults to 1.

Options:
  -c, --count INT  number of greetings
  -h, --help       Show this message and exit
```

Option names and metavars will appear in help output even if no help
string is specified for them. On the other hand, arguments only appear
in the usage string. It is possible to add a help string to arguments
which will be added to the help page, but the Unix convention is to just
describe arguments in the command help.

## Subcommand Short Help

Subcommands are listed in the help page based on their
[name](commands.html#customizing-command-name). They have a short help
string which is the first line of their help.

```kotiln
class Tool : NoRunCliktCommand()

class Execute : NoRunCliktCommand(help = """
    Execute the command.

    The command will be executed.
    """)

class Abort : NoRunCliktCommand(help="Kill any running commands.")
```

Which generates this help page:

```
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
[command's context](commands.html#customizing-contexts):

```
class Tool : NoRunCliktCommand() {
    init {
        context {
            helpOptionNames = setOf("/help")
            helpOptionMessage = "show the help"
        }
    }
}
```

And on the command line:

```
$ ./tool /help
Usage: tool [OPTIONS]

Options:
  /help  show the help
```

If you don't want a help option to be added, you can set
`helpOptionNames = emptySet()`
