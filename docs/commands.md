# Commands

Clikt supports arbitrarily nested commands. You can add one command as a child of another with the
[`subcommands`][subcommands] function, which can be
called either in an `init` block, or on an existing instance.

## Executing Nested Commands

For commands with no children,
[`run`][run] is called whenever the
command line is parsed (unless parsing is aborted from an error or an option like `--help`).

If a command has children, this isn't the case. Instead, its `run` is
called only if a child command is invoked, just before the subcommand's
`run`. If a parent command is called without specifying a subcommand,
the help page is printed and `run` is not called.

```kotlin tab="Example"
class Tool : CliktCommand() {
    val verbose by option().flag("--no-verbose")
    override fun run() {
        echo("Verbose mode is ${if (verbose) "on" else "off"}")
    }
}

class Execute : CliktCommand() {
    override fun run() {
        echo("executing")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

```text tab="Usage 1"
$ ./tool
Usage: tool [OPTIONS] COMMAND [ARGS]...

Options:
  --verbose / --no-verbose
  -h, --help                Show this message and exit

Commands:
  execute
```

```text tab="Usage 2"
$ ./tool --verbose execute
Verbose mode is on
executing
```

## Customizing Command Name

The default name for subcommands is inferred as a lowercase name from the command class name. You
can also set a name manually in the
[`CliktCommand`][CliktCommand] constructor.

```kotlin tab="Example"
class Tool : CliktCommand() {
    override fun run()= Unit
}

class Execute : CliktCommand(name = "RUN-ME") {
    override fun run() {
        echo("executing")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

```text tab="Usage 1"
$ ./tool RUN-ME
executing
```

```text tab="Usage 2"
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

```kotlin tab="Example"
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

```text tab="Usage"
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
can do so with the command's [`Context`][Context].

Every time the command line is parsed, each command creates a new
context object for itself that is liked to its parent's context.
`Context` objects have a number of properties that can be used to
customize command line parsing. Although each command creates its own
context, the configuration is inherited from the parent context.

`Context` objects also have an `obj` property that can hold any user
defined data. You can use the `obj` to create interfaces like this:

```kotlin tab="Example"
class Tool : CliktCommand() {
    val verbose by option().flag("--no-verbose")
    val config by findOrSetObject { mutableMapOf<String, String>() }
    override fun run() {
        config["VERBOSE"] = if (verbose) "on" else "off"
    }
}

class Execute : CliktCommand() {
    val config by requireObject<Map<String, String>>()
    override fun run() {
        echo("Verbose mode is ${config["VERBOSE"]}")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

```text tab="Usage"
$ ./tool --verbose execute
Verbose mode is on
```

The [`findObject`][findObject], [`findOrSetObject`][findOrSetObject], and
[`requireObject`][requireObject] functions will walk up the context tree until they find an object
with the given type. If no such object exists, they will either return `null`, throw an exception,
or create an instance of the object and store it on the command's context, depending on which
function you call. Note that `findOrSetObject` won't set the Context's object until it's property
value is accessed. If you need to set an object for subcommands without accessing the property, you
should use the [`Context.findOrSetObject`] instead. 

## Running Parent Command Without Children

Normally, if a command has children, [`run`][run] is not called unless a child
command is invoked on the command line. Instead, `--help` is called on the parent. If you want to
change this behavior to always call `run()` on the parent, you can do so by setting
`invokeWithoutSubcommand` to `true`. The `Context` will then have information on the subcommand that
is about to be invoked, if there is one.

```kotlin tab="Example"
class Tool : CliktCommand(invokeWithoutSubcommand = true) {
    override fun run() {
        val subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            echo("invoked without a subcommand")
        } else {
            echo("about to run ${subcommand.commandName}")
        }
    }
}

class Execute : CliktCommand() {
    override fun run() {
        echo("running subcommand")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
```

```text tab="Usage 1"
$ ./tool
invoked without a subcommand
```

```text tab="Usage 2"
$./tool execute
about to run execute
running subcommand
```

## Customizing Contexts

[Contexts][Context] have a number of properties that can be customized,
and which are inherited by child commands.
You can change these properties with the [`context`][context] builder function,
which can be called in an `init` block, or on a command instance.

For example, you can change the default help message for the `--help`
option. These definitions are equivalent:

```kotlin tab="Version 1"
class Cli : NoOpCliktCommand() {
    init {
        context { helpOptionMessage = "print the help" }
    }
}
fun main(args: Array<String>) = Cli()
```

```kotlin tab="Version 2"
class Cli : NoOpCliktCommand()
fun main(args: Array<String>) = Cli()
    .context { helpOptionMessage = "print the help" }
    .main(args)
```

```text tab="Usage"
$ ./cli --help
Usage: cli [OPTIONS]

Options:
  -h, --help  print the help
```

## Printing the Help Message When No Arguments Are Given

Normally, if a command is called with no values on the command line, a usage error is printed if
there are required parameters, or [`run`][run] is called if there aren't any.

You can change this behavior by passing `printHelpOnEmptyArgs = true` to your command's
constructor. This will cause a help message to be printed when no values are provided on the command
line, regardless of the parameters in your command.

```kotlin tab="Example"
class Cli : CliktCommand(printHelpOnEmptyArgs = true) {
    val arg by argument()
    override fun run() { echo("Command ran") }
}
```

```text tab="Usage"
$ ./cli
Usage: cli [OPTIONS]

Options:
  -h, --help  print the help
```

## Warnings and Other Messages

When you want to show information to the user, you'll usually want to use the
[functions for printing to stdout][printing-to-stdout-and-stderr] directly.

However, there's another mechanism that can be useful when writing reusable parameter code:
command messages. These messages are buffered during parsing and printed all at once immediately
before a command's [`run`][run] is called.
They are not printed if there are any errors in parsing. This type of message is used by Clikt for
[`deprecating options`][deprecating-options].

You can issue a command message by calling
[`CliktCommand.issueMessage`][issueMessage] or with the
`message` function available in the context of parameter transformers.

```kotlin tab="Example"
class Cli : CliktCommand() {
    // This will print the warning when the option is given, but not if there are errors
    val opt by option().validate {
        if (it.isEmpty()) message("Empty strings are not recommended")
    }
    override fun run() {
        echo("command run")
    }
}
```

```text tab="Usage 1"
$ ./cli --opt=''
Empty strings are not recommended
command run
```

```text tab="Usage 2"
$ ./cli --opt='' --oops
Error: no such option: "--oops".
```

You can disable automatic message printing on the [command's context][customizing-context]:

```kotlin tab="Example"
class Cli : CliktCommand() {
    init { context { printExtraMessages = false } }
    val opt by option().validate {
        if (it.isEmpty()) message("Empty strings are not recommended")
    }
    override fun run() {
        echo("command run")
    }
}
```

```text tab="Usage"
$ ./cli --opt=''
command run
```

## Chaining and Repeating Subcommands

Some command line interfaces allow you to call more than one subcommand at a time. For example, you
might do something like `gradle clean build publish` to run the `clean` task, then the `build` task,
then the `publish` task, which are all subcommands of `gradle`.

To do this with Clikt, pass `allowMultipleSubcommands = true` to your [CliktCommand][CliktCommand]
constructor.

```kotlin tab="Example"
class Compiler: CliktCommand(allowMultipleSubcommands = true) {
    override fun run() {
        echo("Running compiler")
    }
}

class Clean: CliktCommand() {
    val force by option().flag()
    override fun run() {
        echo("Cleaning (force=$force)")
    }
}

class Build: CliktCommand() {
    val file by argument().file()
    override fun run() {
        echo("Building $file")
    }
}

fun main(args: Array<String>) = Compiler().subcommands(Clean(), Build()).main(args)
```

```text tab="Usage"
$ ./compiler clean --force build main.kt
Running compiler
Cleaning (force=true)
Building main.kt
```

The parent command will [`run`][run] once, and each subcommand will `run` once each time they're called.

### Parsing multiple subcommands

Note that enabling `allowMultipleSubcommands` will disable [`allowInterspersedArgs`][interspersed]
on the command and all its subcommands. If both were allowed to be enabled at the same time, then
not all command lines could be parsed unambiguously.

When parsing in this mode, tokens are consumed greedily by a subcommand until it encounters an
argument token it doesn't support, at which point the parent command resumes parsing where the
subcommand left off. This means that if you have a subcommand with an
[`argument().multiple()`][argument.multiple] parameter, you won't be able to call any other
subcommands after that one, since it will consume the rest of the command line.

Subcommands of a command with `allowMultipleSubcommands=true` can themselves have subcommands, but
cannot have `allowMultipleSubcommands=true`.


[subcommands]:                   api/clikt/com.github.ajalt.clikt.core/subcommands.md
[run]:                           api/clikt/com.github.ajalt.clikt.core/-clikt-command/run.md
[CliktCommand]:                  api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.md
[Context]:                       api/clikt/com.github.ajalt.clikt.core/-context/index.md
[findObject]:                    api/clikt/com.github.ajalt.clikt.core/find-object.md
[findOrSetObject]:               api/clikt/com.github.ajalt.clikt.core/find-or-set-object.md
[Context.findOrSetObject]:       api/clikt/com.github.ajalt.clikt.core/-context/find-or-set-object/
[requireObject]:                 api/clikt/com.github.ajalt.clikt.core/require-object.md
[context]:                       api/clikt/com.github.ajalt.clikt.core/context.md
[printing-to-stdout-and-stderr]: quickstart.md#printing-to-stdout-and-stderr
[deprecating-options]:           options.md#deprecating-options
[issueMessage]:                  api/clikt/com.github.ajalt.clikt.core/-clikt-command/issue-message.md
[customizing-context]:           #customizing-contexts
[interspersed]:                  api/clikt/com.github.ajalt.clikt.core/-context/allow-interspersed-args.md
[argument.multiple]:             api/clikt/com.github.ajalt.clikt.parameters.arguments/multiple.md
