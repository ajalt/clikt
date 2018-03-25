# Quick Start

You can get the library using any maven-compatible build system.
Installation instructions can be found in the [README](/README.md).

<!-- TODO: talk about app plugin
 https://docs.gradle.org/current/userguide/application_plugin.html  -->

# Basic Concepts

Clikt command line interfaces are created by using property delegates
inside of a [CliktCommand]<!-- TODO -->. The normal way to use Clikt is to forward
`argv` from your `main` funciton to [CliktCommand.main]<!-- TODO -->.

The simplest command with no parameters would look like this:

```kotlin
class Hello: CliktCommand() {
    override fun run() {
        TermUi.echo("Hello World!")
    }
}

fun main(args: Array<String>) = Hello().main(args)
```

And what it looks like to use:

```
$ ./hello
Hello World!
```

A help page is generated automatically:

```
$ ./hello --help
Usage: hello [OPTIONS]

Options:
  -h, --help  Show this message and exit
```

## Echoing

Why does this example use [TermUi.echo]<!-- TODO --> instead of
[println](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/println.html)?
Although `println` works, it can cause problems with multi-platform
support. [TermUi.echo]<!-- TODO --> automatically translates line breaks into the
line separator for the current platform. So you don't have to worry
that some of your users will see mangled output because you didn't test
on Windows.

## Nesting Commands

Instances of any command can be attached to other commands, allowing
arbitrary nesting of commands. For eaxmple, you could write a script to
manage a database:

```kotlin
class Database: CliktCommand() {
    override fun run() = Unit
}

class Init: CliktCommand(help="Initialize the database") {
    override fun run() {
        TermUi.echo("Initialized the database.")
    }
}

class Drop: CliktCommand(help="Drop the database") {
    override fun run() {
        TermUi.echo("Dropped the database.")
    }
}

fun main(args: Array<String>) = Database()
        .subcommands(Init(), Drop())
        .main(args)
```

Which you can use:

```
$ ./db init
Initialized the database.
```

And the generated help will include the subcommands:

```
$ ./db --help
Usage: database [OPTIONS] COMMAND [ARGS]...

Options:
  -h, --help  Show this message and exit

Commands:
  init  Initialize the database
  drop  Drop the database
```


## Adding parameters

To add parameters, use the [option] <!-- TODO --> and [argument] <!--
TODO --> property delegates:

```kotlin
class Hello : CliktCommand() {
    val count by option(help="Number of greetings").int().default(1)
    val username by argument()

    override fun run() {
        for (i in 1..count) {
            TermUi.echo("Hello $username!")
        }
    }
}
```

Which will generate help like:

```
$ ./hello --help
Usage: hello [OPTIONS] USERNAME

Options:
  --count INT  Number of greetings
  -h, --help   Show this message and exit
```
