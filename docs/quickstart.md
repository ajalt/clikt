# Quick Start

You can get the library using any maven-compatible build system.
Installation instructions can be found in the [README][README].

## Basic Concepts

Clikt command line interfaces are created by using property delegates
inside a [`CliktCommand`][CliktCommand]. The normal way to use Clikt is to forward
`argv` from your `main` function to [`CliktCommand.main`][main].

The simplest command with no parameters would look like this:

```kotlin
class Hello: CliktCommand() {
    override fun run() {
        echo("Hello World!")
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
Usage: hello [<options>]

Options:
  -h, --help  Show this message and exit
```

## Printing to Stdout and Stderr

Why does this example use [`echo`][echo] instead of [`println`][println]? Although `println` works,
it can cause problems with multi-platform support. `echo` uses Mordant to print, so it supports
colors and detects the current terminal to make sure that colors work on the current system. You can
also pass `err=true` to `echo` to print to stderr instead of stdout.

Additionally, if you use Clikt's [testing utilities][test], output sent 
to `echo` will be captured for testing, but output sent to `println` will not.

## Nesting Commands

Instances of any command can be attached to other commands, allowing
arbitrary nesting of commands. For example, you could write a script to
manage a database:

=== "Example"
    ```kotlin
    class Database(name="db"): CliktCommand() {
        override fun run() = Unit
    }

    class Init: CliktCommand(help="Initialize the database") {
        override fun run() {
            echo("Initialized the database.")
        }
    }

    class Drop: CliktCommand(help="Drop the database") {
        override fun run() {
            echo("Dropped the database.")
        }
    }

    fun main(args: Array<String>) = Database()
            .subcommands(Init(), Drop())
            .main(args)
    ```

=== "Usage"
    ```text
    $ ./db init
    Initialized the database.

    $ ./db drop
    Dropped the database.
    ```

=== "Help Output"
    ```text
    $ ./db --help
    Usage: database [<options>] <command> [<args>]...

    Options:
      -h, --help  Show this message and exit

    Commands:
      init  Initialize the database
      drop  Drop the database
    ```


## Adding Parameters

To add parameters, use the [`option`][option] and [`argument`][argument] property
delegates:

=== "Example"
    ```kotlin
    class Hello : CliktCommand() {
        val count by option(help="Number of greetings").int().default(1)
        val name by argument()

        override fun run() {
            for (i in 1..count) {
                echo("Hello $name!")
            }
        }
    }
    ```

=== "Help Output"
    ```text
    $ ./hello --help
    Usage: hello [<options>] <name>

    Options:
      --count <int>  Number of greetings
      -h, --help     Show this message and exit
    ```

## Developing Command Line Applications With Gradle

When you write a command line application, you probably want to be able to run it without invoking
`java -jar ...` every time. If you're using Gradle, the [application plugin][application_plugin]
provides a gradle task that bundles your program jars and scripts to launch them. It makes it easy
to build a zip or tarball that you can distribute to your users without them needing to perform any
incantations like setting up a classpath. You can see this plugin in use the in [Clikt
samples][clikt-samples].

The application plugin also creates tasks that will build then run your
main function directly from within gradle. You can pass command line arguments through to your app
with the `--args` flag:

```shell
$ ./gradlew run --args="hello --count=3 --name=Clikt"
```

A drawback to using the `run` gradle task is that it redirects stdout, so Clikt will not print
colors or prompt for input. You can configure the Mordant terminal that Clikt uses to always print
with color, but this will cause ANSI codes to be printed even if you redirect the app's output to a
file.

```kotlin
MyCommand().context {
    terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true)
}.main(args)
```

Another approach is to use the `installDist` task provided by the plugin. This builds all the
distribution scripts in your build folder, which you can then execute normally. See Clikt's
[runsample][runsample] script for an example of this approach.


[application_plugin]: https://docs.gradle.org/current/userguide/application_plugin.html
[argument]:           api/clikt/com.github.ajalt.clikt.parameters.arguments/argument.html
[clikt-samples]:      https://github.com/ajalt/clikt/tree/master/samples
[CliktCommand]:       api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.html
[echo]:               api/clikt/com.github.ajalt.clikt.core/-clikt-command/echo.html
[main]:               api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html
[option]:             api/clikt/com.github.ajalt.clikt.parameters.options/option.html
[println]:            https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/println.html
[README]:             https://github.com/ajalt/clikt
[runsample]:          https://github.com/ajalt/clikt/blob/master/runsample
[test]:               testing.md
