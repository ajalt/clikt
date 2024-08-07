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

=== "Example"
    ```kotlin
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

=== "Usage 1"
    ```text
    $ ./tool
    Usage: tool [<options>] <command> [<args>]...

    Options:
      --verbose / --no-verbose
      -h, --help                Show this message and exit

    Commands:
      execute
    ```

=== "Usage 2"
    ```text
    $ ./tool --verbose execute
    Verbose mode is on
    executing
    ```

## Customizing Command Name

The default name for subcommands is inferred as a lowercase name from the command class name. You
can also set a name manually in the
[`CliktCommand`][CliktCommand] constructor.

=== "Example"
    ```kotlin
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

=== "Usage 1"
    ```text
    $ ./tool RUN-ME
    executing
    ```

=== "Usage 2"
    ```text
    $ ./tool -h
    Usage: tool [<options>] <command> [<args>]...

    Options:
      -h, --help  Show this message and exit

    Commands:
      RUN-ME
    ```

## Passing Parameters

When calling subcommands, the position of options and arguments on the
command line affect which command will parse them. A parameter is parsed
by a command if it occurs after the command name, but before any other
command names.

=== "Example"
    ```kotlin
    class Tool : CliktCommand() {
        override fun help(context: Context) = "A tool that runs"
        val verbose by option().flag("--no-verbose")
        override fun run() = Unit
    }

    class Execute : CliktCommand() {
        override fun help(context: Context) = "Execute the command"
        val name by option()
        override fun run() = Unit
    }

    fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
    ```

=== "Usage"
    ```text
    $ ./tool --help
    Usage: tool [<options>] <command> [<args>]...

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
Usage: execute [<options>]

  Execute the command

Options:
  --name <text>
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
context object for itself that is linked to its parent's context.
`Context` objects have a number of properties that can be used to
customize command line parsing. Although each command creates its own
context, the configuration is inherited from the parent context.

`Context` objects also have a [`data`][Context.data] map and [`obj`][Context.obj] property that
hold objects that can be accessed from child commands.

=== "Example"
    ```kotlin
    data class MyConfig(var verbose: Boolean = false)

    class Tool : CliktCommand() {
        val verbose by option().flag("--no-verbose")
        val config by findOrSetObject { MyConfig() }
        override fun run() {
            config.verbose = if (verbose) "on" else "off"
        }
    }

    class Execute : CliktCommand() {
        val config by requireObject<MyConfig>()
        override fun run() {
            echo("Verbose mode is ${config.verbose}")
        }
    }

    fun main(args: Array<String>) = Tool().subcommands(Execute()).main(args)
    ```

=== "Usage"
    ```text
    $ ./tool --verbose execute
    Verbose mode is on
    ```

The [`findObject`][findObject], [`findOrSetObject`][findOrSetObject], and
[`requireObject`][requireObject] functions will walk up the context tree until they find a
[`obj`][Context.obj] with the given type. If no such object exists, they will either return `null`,
throw an exception, or create an instance of the object and store it on the command's context,
depending on which function you use. If you need more than one object, you can pass a `key` to these
functions, and they'll look for an object with that key and type in the context's `data` map.

Keep in mind that the [`findOrSetObject`][findOrSetObject] property is lazy and won't set the
Context's `obj` until its value is accessed. If you need to set an object for subcommands without
accessing the property, you should use [`currentContext.findOrSetObject`][Context.findOrSetObject],
or set [`currentContext.obj`][Context.obj] or [`Context.Builder.obj`][Context.obj] directly,
instead.

=== "Eager initialization with findOrSetObject"
    ```kotlin
    class Tool : CliktCommand() {
        override fun run() {
            // runs eagerly
            currentContext.findOrSetObject { MyConfig() }
        }
    }
    ```

=== "Eager initialization with currentContext.obj"
    ```kotlin
    class Tool : CliktCommand() {
        override fun run() {
            // runs eagerly, won't look for parent contexts
            currentContext.obj = MyConfig()
        }
    }
    ```

=== "Eager initialization with context builder"
    ```kotlin
    Tool().context {
        // runs eagerly, won't look for parent contexts
        obj = MyConfig()
    }
    ```

!!! tip

    If you need to share resources that need to be cleaned up, you can use
    [`currentContext.registerCloseable`](advanced.md#managing-shared-resources)

## Running Parent Command Without Children

Normally, if a command has children, [`run`][run] is not called unless a child command is invoked on
the command line. Instead, `--help` is called on the parent. If you want to change this behavior to
always call `run()` on the parent, you can do so by setting `invokeWithoutSubcommand` to `true`. The
`Context` will then have information on the subcommand that is about to be invoked, if there is one.

=== "Example"
    ```kotlin
    class Tool : CliktCommand() {
        override val invokeWithoutSubcommand = true
        override fun run() {
            val subcommand = currentContext.invokedSubcommand
            if (subcommand == null) {
                echo("invoked without a subcommand")
            } else {
                echo("about to run ${subcommand.name}")
            }
        }
    }
    
    class Execute : CliktCommand() {
        override fun run() {
            echo("running subcommand")
        }
    }
    ```

=== "Usage 1"
    ```text
    $ ./tool
    invoked without a subcommand
    ```

=== "Usage 2"
    ```text
    $./tool execute
    about to run execute
    running subcommand
    ```

## Customizing Contexts

[Contexts][Context] have a number of properties that can be customized,
and which are inherited by child commands.
You can change these properties with the [`context`][context] builder function,
which can be called in an `init` block, or on a command instance.

For example, you can change the name of help option. These definitions are equivalent:

=== "Version 1"
    ```kotlin
    class Cli : NoOpCliktCommand() {
        init {
            context { helpOptionNames = setOf("/help") }
        }
    }
    fun main(args: Array<String>) = Cli()
    ```

=== "Version 2"
    ```kotlin
    class Cli : NoOpCliktCommand()
    fun main(args: Array<String>) = Cli()
        .context { helpOptionNames = setOf("/help") }
        .main(args)
    ```

=== "Usage"
    ```text
    $ ./cli --help
    Usage: cli [<options>]

    Options:
      -h, --help  print the help
    ```

## Printing the Help Message When No Arguments Are Given

Normally, if a command is called with no values on the command line, a usage error is printed if
there are required parameters, or [`run`][run] is called if there aren't any.

You can change this behavior by passing overriding `printHelpOnEmptyArgs = true` in your command.
This will cause a help message to be printed when no values are provided on the command
line, regardless of the parameters in your command.

=== "Example"
    ```kotlin
    class Cli : CliktCommand() {
        override val printHelpOnEmptyArgs = true
        val arg by argument()
        override fun run() { echo("Command ran") }
    }
    ```

=== "Usage"
    ```text
    $ ./cli
    Usage: cli [<options>]

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

=== "Example"
    ```kotlin
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

=== "Usage 1"
    ```text
    $ ./cli --opt=''
    Empty strings are not recommended
    command run
    ```

=== "Usage 2"
    ```text
    $ ./cli --opt='' --oops
    Error: no such option: "--oops".
    ```

You can disable automatic message printing on the [command's context][customizing-context]:

=== "Example"
    ```kotlin
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

=== "Usage"
    ```text
    $ ./cli --opt=''
    command run
    ```

## Chaining and Repeating Subcommands

Some command line interfaces allow you to call more than one subcommand at a time. For example, you
might do something like `gradle clean build publish` to run the `clean` task, then the `build` task,
then the `publish` task, which are all subcommands of `gradle`.

To do this with Clikt, override `allowMultipleSubcommands = true` in your command.

=== "Example"
    ```kotlin
    class Compiler: CliktCommand() {
        override val allowMultipleSubcommands = true
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

=== "Usage"
    ```text
    $ ./compiler clean --force build main.kt
    Running compiler
    Cleaning (force=true)
    Building main.kt
    ```

The parent command will [`run`][run] once, and each subcommand will `run` once each time they're
called.

!!! warning

    Enabling `allowMultipleSubcommands` will disable [`allowInterspersedArgs`][interspersed]
    on the command and all its subcommands. If both were allowed to be enabled at the same time, then
    not all command lines could be parsed unambiguously.

    Subcommands of a command with `allowMultipleSubcommands=true` can themselves have subcommands, but
    cannot have `allowMultipleSubcommands=true`.

## Command pipelines

If you have [multiple subcommands](#chaining-and-repeating-subcommands) you might want to pass the
output of one subcommand to the next. For example, the [ImageMagick](https://imagemagick.org) tool
lets you apply a series of transformations to an image by invoking multiple subcommands.

To do this with Clikt, you could pass your output through the
[`Context.obj`](#nested-handling-and-contexts), but another option is to use a
[ChainedCliktCommand], which allows you to return values from your `run` function that will be
passed to the next subcommand.

In this example, we'll write simple text editing pipeline that takes an initial string, and then
applies a series of transformations to it, printing the final result:

=== "Example"
    ```kotlin
    class EditText : ChainedCliktCommand<String>() {
        override val allowMultipleSubcommands: Boolean = true
        val text by argument()
        override fun run(value: String): String = text
    }

    class RepeatText : ChainedCliktCommand<String>("repeat") {
        val count by option().int().default(1)
        override fun run(value: String): String {
            return value.repeat(count)
        }
    }

    class UppercaseText : ChainedCliktCommand<String>("uppercase") {
        override fun run(value: String): String {
            return value.uppercase()
        }
    }

    class ReplaceText : ChainedCliktCommand<String>("replace") {
        val oldValue by argument()
        val newValue by argument()
        override fun run(value: String): String {
            return value.replace(oldValue, newValue)
        }
    }

    fun main(args: Array<String>) {
        val command = EditText()
            .subcommands(RepeatText(), UppercaseText(), ReplaceText())
        val result = command.main(args, "")
        command.echo(result)
    }
    ```

=== "Usage"
    ```text
    $ ./edit-text 'hello ' uppercase repeat --count=3 replace H Y
    YELLO YELLO YELLO
    ```

[argument.multiple]:             api/clikt/com.github.ajalt.clikt.parameters.arguments/multiple.html
[ChainedCliktCommand]:           api/clikt-mordant/com.github.ajalt.clikt.command/-chained-clikt-command/index.html
[CliktCommand]:                  api/clikt-mordant/com.github.ajalt.clikt.core/-clikt-command/index.html
[Context.findOrSetObject]:       api/clikt/com.github.ajalt.clikt.core/find-or-set-object.html
[Context.obj]:                   api/clikt/com.github.ajalt.clikt.core/obj.html
[Context]:                       api/clikt/com.github.ajalt.clikt.core/-context/index.html
[context]:                       api/clikt/com.github.ajalt.clikt.core/context.html
[customizing-context]:           #customizing-contexts
[deprecating-options]:           options.md#deprecating-options
[findObject]:                    api/clikt/com.github.ajalt.clikt.core/find-object.html
[findOrSetObject]:               api/clikt/com.github.ajalt.clikt.core/find-or-set-object.html
[interspersed]:                  api/clikt/com.github.ajalt.clikt.core/-context/allow-interspersed-args.html
[issueMessage]:                  api/clikt/com.github.ajalt.clikt.core/-base-clikt-command/issue-message.html
[printing-to-stdout-and-stderr]: quickstart.md#printing-to-stdout-and-stderr
[requireObject]:                 api/clikt/com.github.ajalt.clikt.core/require-object.html
[run]:                           api/clikt/com.github.ajalt.clikt.core/-runnable-clikt-command/run.html
[subcommands]:                   api/clikt/com.github.ajalt.clikt.core/subcommands.html
