# Quick Start

You can get the library using any maven-compatible build system.
Installation instructions can be found in the [README](https://github.com/ajalt/clikt).

## Basic Concepts

Clikt command line interfaces are created by using property delegates
inside of a [CliktCommmand](api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.html). The normal way to use Clikt is to forward
`argv` from your `main` function to [ClktCommand.main](api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html).

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

## Printing to Stdout and Stderr

Why does this example use [TermUi.echo](api/clikt/com.github.ajalt.clikt.output/-term-ui/echo.html) instead of
[println](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/println.html)?
Although `println` works, it can cause problems with multi-platform
support. [echo](api/clikt/com.github.ajalt.clikt.output/-term-ui/echo.html) automatically translates line breaks into the line
separator for the current platform. So you don't have to worry that some
of your users will see mangled output because you didn't test on
Windows. You can also pass `err=true` to `echo` to print to stderr
instead of stdout.

## Nesting Commands

Instances of any command can be attached to other commands, allowing
arbitrary nesting of commands. For example, you could write a script to
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


## Adding Parameters

To add parameters, use the [option](api/clikt/com.github.ajalt.clikt.parameters.options/option.html) and [argument](api/clikt/com.github.ajalt.clikt.parameters.arguments/argument.html) property delegates:

```kotlin
class Hello : CliktCommand() {
    val count by option(help="Number of greetings").int().default(1)
    val name by argument()

    override fun run() {
        for (i in 1..count) {
            TermUi.echo("Hello $name!")
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

## Developing Command Line Applications With Gradle

When you write a command line application, you probably want to be able
to run it without invoking `java -jar ...` every time. If you're using
Gradle, the [application
plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
provides a gradle task that bundles your program jars and scripts to
launch them. It makes it easy to build a zip or tarball that you can distribute to your users without them needing to perform any incatations like setting up a classpath. You can see this plugin in use the in [Clikt samples](https://github.com/ajalt/clikt/tree/master/samples).

The application plugin also creates tasks that will build then run your
main function directly from within gradle. Although it seems like these
tasks would make development easier, they are not recommended for use
with command line programs. Unfortunately, due to the way gradle is
designed, command line arguments are not visible to the task. Although
you can hack the task to split up a gradle property and pass it in to
your argv, this approach is limited. Additionally, stdin, stdout, and
environment variables are all captured by gradle. All these limitations
make the run task mostly useless for command line applications.

An easier way to do development is to used the `installDist` task
provided by the plugin. This builds all the distribution scripts in your
build folder, which you can then execute normally. See Clikt's
[runsample](https://github.com/ajalt/clikt/blob/master/runsample) script
for an example of this approach.
