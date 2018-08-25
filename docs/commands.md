# Commands

Clikt supports arbitrarily nested commands. You can add one command as a
child of another with the [`echo`](api/clikt/com.github.ajalt.clikt.core/subcommands.html) function, which can be called either in
an `init` block, or on an existing instance.

## Executing Nested Commands

For commands with no children,[`run`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/run.html) is called whenever the
command line is parsed (unless parsing is aborted from an error or an
option like `--help`).

If a command has children, this isn't the case. Instead, its `run` is
called only if a child command is invoked, just before the subcommand's
`run`. If a parent command is called without specifying a subcommand,
the help page is printed and `run` is not called.

```kotlin
class Tool : CliktCommand() {
    val verbose by option().flag("--no-verbose")
    override fun run() {
        TermUi.echo("Verbose mode is ${if (verbose) "on" else "off"}")
    }
}

class Execute : CliktCommand() {
    override fun run() {
        TermUi.echo("executing")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

And on the command line:

```
$ ./tool
Usage: tool [OPTIONS] COMMAND [ARGS]...

Options:
  --verbose / --no-verbose
  -h, --help                Show this message and exit

Commands:
  execute
```

Or:

```
$ ./tool --verbose execute
Verbose mode is on
executing
```

## Customizing Command Name

The default name for subcommands is inferred as a lowercase name from
the command class name. You can also set a name manually in the [`CliktCommand`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.html) constructor.

```kotlin
class Tool : CliktCommand() {
    override fun run()= Unit
}

class Execute : CliktCommand(name = "RUN-ME") {
    override fun run() {
        TermUi.echo("executing")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

And on the command line:

```
$ ./tool RUN-ME
executing
```

```
$ ./tool -h
Usage: tool [OPTIONS] COMMAND [ARGS]...

Options:
  -h, --help  Show this message and exit

Commands:
  RUN-ME
```

## Passing Parameters

When calling subcommands, the position of options and arguments on the
command line affect which command will parse them. A parameter is parsed
by a command if is occurs after the command name, but before any other
command names.

For example:

```kotiln
class Tool : CliktCommand(help = "A tool that runs") {
    val verbose by option().flag("--no-verbose")
    override fun run() = Unit
}

class Execute : CliktCommand(help = "Execute the command") {
    val name by option()
    override fun run() = Unit
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

Which has the following behavior:

```
$ ./tool --help
Usage: tool [OPTIONS] COMMAND [ARGS]...

  A tool that runs

Options:
  --verbose / --no-verbose
  -h, --help                Show this message and exit

Commands:
  execute  Execute the command
```

If you instead execute `--help` after the subcommand, the subcommand's
help is printed:

```
$ ./tool execute --help
Usage: execute [OPTIONS]

  Execute the command

Options:
  --name TEXT
  -h, --help   Show this message and exit
```

But executing `./tool --help execute`, with the option _before_ the
subcommand, will cause the parent's help option to be invoked, printing
out `Tool`'s help page as if you just typed `./tool --help`.

## Nested Handling And Contexts

Normally nested command are independent of each other: a child can't
access its parent's parameters. This makes composing commands much
easier, but what if you want to pass information to a child command? You
can do so with the command's [`Context`](api/clikt/com.github.ajalt.clikt.core/-context/index.html).

Every time the command line is parsed, each command creates a new
context object for itself that is liked to its parent's context.
`Context` objects have a number of properties that can be used to
customize command line parsing. Although each command creates its own
context, the configuration is inherited from the parent context.

`Context` objects also have an `obj` property that can hold any user
defined data. You can use the `obj` to create interfaces like this:

```kotlin
class Tool : CliktCommand() {
    val verbose by option().flag("--no-verbose")
    val config by findObject { mutableMapOf<String, String>() }
    override fun run() {
        config["VERBOSE"] = if (verbose) "on" else "off"
    }
}

class Execute : CliktCommand() {
    val config by requireObject<Map<String, String>>()
    override fun run() {
        TermUi.echo("Verbose mode is ${config["VERBOSE"]}")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

And on the command line:

```
$ ./tool --verbose execute
Verbose mode is on
```

The [`findObject`](api/clikt/com.github.ajalt.clikt.core/find-object.html) and [`requireObject`](api/clikt/com.github.ajalt.clikt.core/require-object.html) functions will
walk up the context tree until they find an object with the given type.
If no such object exists, they will either return `null`, throw an
exception, or create an instance of the object and store it on the
command's context, depending on which overload you call.

## Running Parent Command Without Children

Normally, if a command has children, [`run`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/run.html) is not called unless a
child command is invoked on the command line. Instead, `--help` is
called on the parent. If you want to change this behavior to always call
`run()` on the parent, you can do so by setting
`invokeWithoutSubcommand` to `true`. The `Context` will then have
information on the subcommand that is about to be invoked, if there is
one.

```kotlin
class Tool : CliktCommand(invokeWithoutSubcommand = true) {
    override fun run() {
        if (context.invokedSubcommand == null) {
            TermUi.echo("invoked without a subcommand")
        } else {
            TermUi.echo("about to run ${context.invokedSubcommand!!.commandName}")
        }
    }
}

class Execute : CliktCommand() {
    override fun run() {
        TermUi.echo("running subcommand")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

And on the command line:

```
$ ./tool
invoked without a subcommand

$./tool execute
about to run execute
running subcommand
```

## Customizing Contexts

[Contexts](api/clikt/com.github.ajalt.clikt.core/-context/index.html)
have a number of properties that can be customized, and which are
inherited by child commands. You can change these properties with the [`context`](api/clikt/com.github.ajalt.clikt.core/context.html) builder function, which
can be called in an `init` block, or on a command instance.

For example, you can change the default help message for the `--help`
option. These definitions are equivalent:

```kotlin
class Cli : NoRunCliktCommand() {
    init {
        context { helpOptionMessage = "print the help" }
    }
}
fun main(args: Array<String>) = Cli().main(args)
```

and

```kotlin
class Cli : NoRunCliktCommand()
fun main(args: Array<String>) = Cli()
    .context { helpOptionMessage = "print the help" }
    .main(splitArgv(""))
```

Any they work like:

```
$ ./cli --help
Usage: cli [OPTIONS]

Options:
  -h, --help  print the help
```
