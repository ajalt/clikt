# Advanced Patterns

## Common Options With Subcommands

In some cases, you will have multiple subcommands that all share a common set of options. For
example, you may have an option for a config file, or an output directory, or some API credentials.
There are several ways to structure your commands to avoid repeating the option declarations in each
subcommand.

### Defining Common Options on the Root Command

You can define your options on the root command and pass down the information via the context. With
this design, you'll have to specify the common options before the subcommand name on the command
line.

=== "Example"
    ```kotlin
    class Config(val token: String, val hostname: String)

    class MyApi : CliktCommand() {
        private val token by option(help="api token to use for requests").default("...")
        private val hostname by option(help="base url for requests").default("example.com")

        override fun run() {
            currentContext.obj = Config(token, hostname)
        }
    }

    class Store : CliktCommand() {
        private val file by option(help="file to store").file(canBeDir = false)
        private val config by requireObject<Config>()
        override fun run() {
            myApiStoreFile(config.token, config.hostname, file)
        }
    }

    class Fetch : CliktCommand() {
        private val outdir by option(help="directory to store file in").file(canBeFile = false)
        private val config by requireObject<Config>()
        override fun run() {
            myApiFetchFile(config.token, config.hostname, outdir)
        }
    }

    fun main(args: Array<String>) = MyApi().subcommands(Store(), Fetch()).main(args)
    ```

=== "Usage 1"
    ```text
    $ ./myapi --hostname=https://example.com store file.txt
    ```

=== "Usage 2"
    ```text
    $ ./myapi --hostname=https://example.com fetch --outdir=./out
    ```

### Defining Common Options in a Group

Instead of defining your common options on the root command, you can instead define them in an
[OptionGroup][grouping-options] which you include in each subcommand. This allows you to specify all
options after the subcommand name.

=== "Example"
    ```kotlin
    class CommonOptions: OptionGroup("Standard Options:") {
        val token by option(help="api token to use for requests").default("...")
        val hostname by option(help="base url for requests").default("example.com")
    }

    class MyApi : NoOpCliktCommand()

    class Store : CliktCommand() {
        private val commonOptions by CommonOptions()
        private val file by option(help="file to store").file(canBeDir = false)
        override fun run() {
            myApiStoreFile(commonOptions.token, commonOptions.hostname, file)
        }
    }

    class Fetch : CliktCommand() {
        private val commonOptions by CommonOptions()
        private val outdir by option(help="directory to store file in").file(canBeFile = false)
        override fun run() {
            myApiFetchFile(commonOptions.token, commonOptions.hostname, outdir)
        }
    }

    fun main(args: Array<String>) = MyApi().subcommands(Store(), Fetch()).main(args)
    ```

=== "Usage 1"
    ```text
    $ ./myapi store --hostname=https://example.com file.txt
    ```

=== "Usage 2"
    ```text
    $ ./myapi fetch --hostname=https://example.com --outdir=./out
    ```

### Defining Common Options in a Base Class

A third design to share options is to define the common options in a base class that all the
subcommands inherit from.

=== "Example"
    ```kotlin
    abstract class MyApiSubcommand : CliktCommand() {
        val token by option(help = "api token to use for requests").default("...")
        val hostname by option(help = "base url for requests").default("example.com")
    }

    class MyApi : NoOpCliktCommand()

    class Store : MyApiSubcommand() {
        private val file by option(help = "file to store").file(canBeDir = false)
        override fun run() {
            myApiStoreFile(token, hostname, file)
        }
    }

    class Fetch : MyApiSubcommand() {
        private val outdir by option(help = "directory to store file in").file(canBeFile = false)
        override fun run() {
            myApiFetchFile(token, hostname, outdir)
        }
    }

    fun main(args: Array<String>) = MyApi().subcommands(Store(), Fetch()).main(args)
    ```

=== "Usage 1"
    ```text
    $ ./myapi store --hostname=https://example.com file.txt
    ```

=== "Usage 2"
    ```text
    $ ./myapi fetch --hostname=https://example.com --outdir=./out
    ```

## Command Aliases

Clikt allows commands to alias command names to sequences of tokens.
This allows you to implement common patterns like allowing the user to
invoke a command by typing a prefix of its name, or user-defined aliases
like the way you can configure git to accept `git ci` as an alias for
`git commit`.

To implement command aliases, override [`CliktCommand.aliases`][aliases] in your command.
This function is called once at the start of parsing,
and returns a map of aliases to the tokens that they alias to.

To implement git-style aliases:

=== "Example"
    ```kotlin
    class Repo : NoOpCliktCommand() {
        // You could load the aliases from a config file etc.
        override fun aliases(): Map<String, List<String>> = mapOf(
                "ci" to listOf("commit"),
                "cm" to listOf("commit", "-m")
        )
    }

    class Commit: CliktCommand() {
        val message by option("-m").default("")
        override fun run() {
            echo("Committing with message: $message")
        }
    }

    fun main(args: Array<String>) = Repo().subcommands(Commit()).main(args)
    ```

=== "Usage 1"
    ```text
    $ ./repo ci -m 'my message'
    Committing with message: my message
    ```

=== "Usage 2"
    ```text
    $ ./repo cm 'my message'
    Committing with message: my message
    ```

!!! note

    Aliases are not expanded recursively: none of the tokens that
    an alias expands to will be expanded again, even if they match another
    alias.

You also use this functionality to implement command prefixes:

=== "Example"
    ```kotlin
    class Tool : NoOpCliktCommand() {
        override fun aliases(): Map<String, List<String>> {
            val prefixCounts = mutableMapOf<String, Int>().withDefault { 0 }
            val prefixes = mutableMapOf<String, List<String>>()
            for (name in registeredSubcommandNames()) {
                if (name.length < 3) continue
                for (i in 1..name.lastIndex) {
                    val prefix = name.substring(0..i)
                    prefixCounts[prefix] = prefixCounts.getValue(prefix) + 1
                    prefixes[prefix] = listOf(name)
                }
            }
            return prefixes.filterKeys { prefixCounts.getValue(it) == 1 }
        }
    }

    class Foo: CliktCommand() {
        override fun run() {
            echo("Running Foo")
        }
    }

    class Bar: CliktCommand() {
        override fun run() {
            echo("Running Bar")
        }
    }

    fun main(args: Array<String>) = Tool().subcommands(Foo(), Bar()).main(args)
    ```

=== "Usage"
    ```text
    $ ./tool ba
    Running Bar
    ```

## Token Normalization

To prevent ambiguities in parsing, aliases are only supported for
command names. However, there's another way to modify user input that
works on more types of tokens. You can set [`transformToken`][transformToken] on the
[command's context][customizing-context], which will be
called for each option and command name that's input. This can be used
to implement case-insensitive parsing, for example:

=== "Example"
    ```kotlin
    class Hello : CliktCommand() {
        init {
            context { transformToken = { it.lowercase() } }
        }

        val name by option()
        override fun run() = echo("Hello $name!")
    }
    ```

=== "Usage"
    ```text
    $ ./hello --NAME=Clikt
    Hello Clikt!
    ```

## Replacing stdin and stdout

By default, functions like [`CliktCommand.main`][main] and [`option().prompt()`][prompt] read from
stdin and write to stdout. If you want to use Clikt in an environment where the standard
streams aren't available, you can set your own implementation of a `TerminalInterface` when
[customizing the command context][customizing-context].

```kotlin
object MyInterface : TerminalInterface {
    override val info: TerminalInfo
        get() = TerminalInfo(/* ... */)

    override fun completePrintRequest(request: PrintRequest) {
        if (request.stderr) MyOutputStream.writeError(request.text)
        else MyOutputStream.write(request.text)
    }

    override fun readLineOrNull(hideInput: Boolean): String? {
        return if (hideInput) MyInputStream.readPassword()
        else MyInputStream.readLine()
    }
}

class CustomCLI : NoOpCliktCommand() {
    init { context { terminal = Terminal(terminalInterface = MyInterface ) } }
}
```

!!! tip

    If you want to log the output, you can use Mordant's `TerminalRecorder`. That's how [test][test]
    is implemented!

## Command Line Argument Files ("@argfiles")

Similar to `javac`, Clikt supports loading command line parameters from a file using the "@argfile"
syntax. You can pass any file path to a command prefixed with `@`, and the file will be expanded
into the command line parameters. This can be useful on operating systems like Windows that have
command line length limits.

If you create a file named `cliargs` with content like this:

```
--number 1
--name='jane doe' --age=30
./file.txt
```

You can call your command with the contents of the file like this:

```
$ ./tool @cliargs
```

Which is equivalent to calling it like this:

```
$ ./tool --number 1 --name='jane doe' --age=30 ./file.txt
```

You can use any file path after the `@`, and can specify multiple @argfiles:

```
$ ./tool @../config/args @C:\\Program\ Files\\Tool\\argfile
```

If you have any options with names that start with `@`, you can still use `@argfiles`, but values on
the command line that match an option will be parsed as that option, rather than an `@argfile`, so
you'll have to give your files a different name.

### Preventing @argfile expansion

If you want to use a value starting with `@` as an argument without expanding it, you have three options:

1. Pass it after a `--`, [which disables expansion for everything that occurs after it][dash-dash].
2. Escape it with `@@`. The first `@` will be removed and the rest used as the argument value. For example, `@@file` will parse as the string `@file`
3. Disable @argfile expansion entirely by setting [`Context.readArgumentFile = null`][readArgumentFile]

### File format

- Normal shell quoting and escaping rules apply. 
- Line breaks are treated as word separators, and can be used where you would normally use a space
  to separate parameters.
- Line breaks can occur within quotes, and will be included in the quoted value.
- @argfiles can contain other @argfile arguments, which will be expanded recursively.
- An unescaped `#` character outside of quotes is treated as a line comment: it and the rest of the
  line are skipped. You can pass a literal `#` by escaping it with `\#` or quoting it with `'#'`.
- If a `\` occurs at the end of a line, the next line is trimmed of leading whitespace and the two
  lines are concatenated.

## Managing Shared Resources

You might need to open a resource like a file or a network connection in one command, and use it in
its subcommands.

The typical way to manage a resource is with the `use` function:

```kotiln
class MyCommand : CliktCommand() {
    private val file by option().file().required()

    override fun run() {
        file.bufferedReader().use { reader ->
            // use the reader
        }
    }
}
```

But if you need to share the resource with subcommands, the `use` function will exit and close the
resource before the subcommand is called. Instead, use the context's [registerCloseable] function
(for `kotlin.AutoCloseable`) or [registerJvmCloseable] function (for `java.lang.AutoCloseable`) to:

```kotlin
class MyCommand : CliktCommand() {
    private val file by option().file().required()

    override fun run() {
        currentContext.obj = currentContext.registerJvmCloseable(file.bufferedReader())
    }
}
```

You can register as many closeables as you need, and they will all be closed when the command and
its subcommands have finished running. If you need to manage a resource that isn't `AutoClosable`,
you can use [callOnClose].

## Custom exit status codes

Clikt will normally exit your program with a status code of 0 for a normal execution, or 1 if
there's an error. If you want to use a different value, you can `throw ProgramResult(statusCode)`.
If you use [`CliktCommand.main`][main], that exception will be caught and `exitProcess` will be
called with the value of `statusCode`.

You could also call `exitProcess` yourself, but the [ProgramResult][ProgramResult] has a couple of
advantages:

- `ProgramResult` is easier to test. Exiting the process makes unit tests difficult to run.
- `ProgramResult` works on all platforms. `exitProcess` is only available on the JVM.

## Custom run function signature

Clikt provides a few command base classes that have different run function signatures.
[CliktCommand] has `fun run()`, while [SuspendingCliktCommand] has `suspend fun run()`. If you want
a run function with a different signature, you can define your own base class the inherits from
[BaseCliktCommand] and use the [CommandLineParser] methods to parse and run the command.

For example, if you want a command that uses a [Flow] to emit multiple value for each run, you could
implement it like this:

=== "Example"
    ```kotlin
    abstract class FlowCliktCommand : BaseCliktCommand<FlowCliktCommand>() {
        abstract fun run(): Flow<String>
    }

    class MyFlowCommand : FlowCliktCommand() {
        val opt by option().required()
        val arg by argument().int()

        override fun run(): Flow<String> = flow {
            emit(opt)
            emit(arg.toString())
        }
    }

    class MyFlowSubcommand : FlowCliktCommand() {
        val arg by argument().multiple()

        override fun run(): Flow<String> = flow {
            arg.forEach { emit(it) }
        }
    }

    fun FlowCliktCommand.parse(argv: Array<String>): Flow<String> {
        val flows = mutableListOf<Flow<String>>()
        CommandLineParser.parseAndRun(this, argv.asList()) { flows += it.run() }
        return flow { flows.forEach { emitAll(it) } }
    }

    fun FlowCliktCommand.main(argv: Array<String>): Flow<String> {
        return CommandLineParser.mainReturningValue(this) { parse(argv) }
    }

    suspend fun main(args: Array<String>) {
        val command = MyFlowCommand().subcommands(MyFlowSubcommand())
        val resultFlow: Flow<String> = command.main(args)
        resultFlow.collect {
            command.echo(it)
        }
    }
    ```

=== "Usage"
    ```text
    $ ./command --opt=foo 11 my-flow-subcommand bar baz
    foo
    11
    bar
    baz
    ```

There are a number of steps here, so let's break it down:

1. Define a base class `FlowCliktCommand` that inherits from [BaseCliktCommand] and has an abstract
   `run` function that returns a `Flow`.
2. Define your commands that inherit from `FlowCliktCommand` and implement your `run` function.
3. Define an extension function `parse` that uses [CommandLineParser.parseAndRun] to parse the
   command and run the `run` function.
4. Define an extension function `main` that uses [CommandLineParser.main] to run the `parse`
   function and handle any exceptions it might throw.
5. In your `main` function, call `main` on your command, and collect the results of the `Flow`.

If you want to customize the behavior even further, see the [next section](#custom-run-behavior).

## Custom run behavior

If you want to customize how or when subcommands are run, you can do so by defining a custom base
class like in the [previous section](#custom-run-function-signature), but instead of using
`CommandLineParser.parseAndRun`, you can call your command's `run` functions manually.

For example, if you want commands to return status codes, and you want to stop running commands as
soon as one of them returns a non-zero status code, you could implement it like this:

=== "Example"
    ```kotlin
    abstract class StatusCliktCommand : BaseCliktCommand<StatusCliktCommand>() {
        abstract fun run(): Int
    }

    class ParentCommand : StatusCliktCommand() {
        override val allowMultipleSubcommands: Boolean = true
        override fun run(): Int {
            echo("Parent")
            return 0
        }
    }

    class SuccessCommand : StatusCliktCommand() {
        override fun run(): Int {
            echo("Success")
            return 0
        }
    }

    class FailureCommand : StatusCliktCommand() {
        override fun run(): Int {
            echo("Failure")
            return 1001
        }
    }

    fun StatusCliktCommand.parse(argv: Array<String>): Int {
        val parseResult = CommandLineParser.parse(this, argv.asList())
        parseResult.invocation.flatten().use { invocations ->
            for (invocation in invocations) {
                val status = invocation.command.run()
                if (status != 0) {
                    return status
                }
            }
        }
        return 0
    }

    fun StatusCliktCommand.main(argv: Array<String>) {
        val status = CommandLineParser.mainReturningValue(this) { parse(argv) }
        exitProcess(status)
    }

    fun main(argv: Array<String>) {
        ParentCommand().subcommands(SuccessCommand(), FailureCommand()).main(argv)
    }
    ```

=== "Usage"
    ```text
    $ ./command success failure success
    Parent
    Success
    Failure

    $ echo $?
    1001
    ```

The steps here are similar to the [previous section](#custom-run-function-signature), but instead of
using [CommandLineParser.parseAndRun], we use [CommandLineParser.parse], then call `run` on each
command invocation manually, and stop when one of them returns a non-zero status code.

## Core Module

Clikt normally uses Mordant for rendering output and interacting with the system, but there are some 
cases where you might want to use Clikt without Mordant. For these cases, Clikt has a core module
that doesn't have any dependencies.

Replace your Clikt dependency with the core module:

```kotlin
dependencies {
    implementation("com.github.ajalt.clikt:clikt-core:$cliktVersion")
}
```

The [CliktCommand] class is only available in the full module, so you'll need to use
[CoreCliktCommand] (or [CoreNoOpCliktCommand]) instead. The `CoreCliktCommand` has the same API as `CliktCommand`, but it
doesn't have any of these features built in:

- Text wrapping, formatting, markdown, or color support
- [argument files](#command-line-argument-files-argfiles)
- [environment variables][envvars]
- `main` exiting the process with a status code
- printing to stderr
- [prompt options][prompt]
- The [test][test] function

Most of those features can be added by setting the appropriate properties on the command's context.
Here's an example of setting all of them using Java APIs, but you only need to set the ones you'll
use:

```kotlin
abstract class MyCoreCommand : CoreCliktCommand() {
    init {
        context {
            readArgumentFile = {
                try {
                    Path(it).readText()
                } catch (e: IOException) {
                    throw FileNotFound(it)
                }
            }
            readEnvvar = { System.getenv(it) }
            exitProcess = { System.exit(it) }
            echoMessage = { context, message, newline, err ->
                val writer = if (err) System.err else System.out
                if (newline) {
                    writer.println(message)
                } else {
                    writer.print(message)
                }
            }
        }
    }
}
```

## Multiplatform Support

Clikt supports the following platforms in addition to JVM:

### Desktop native (Linux, Windows, and macOS)

All functionality is supported, except:

* `env` parameter of [editText][editText] and [editFile][editFile] is ignored.
* [file][file] and [path][path] parameter types are not supported.

### NodeJS JavaScript and WasmJS

All functionality is supported, except:

* [file][file] and [path][path] parameter types are not supported.

### Browser JavaScript and WasmJS

All functionality is supported, except:

* The default terminal only outputs to the browser's developer console, which is
probably not what you want. You can [define your own
TerminalInterface](#replacing-stdin-and-stdout), or you can call [parse][parse] instead of
[main][main] and handle output yourself.
* [editText][editText] and [editFile][editFile] are not supported.
* [file][file] and [path][path] parameter types are not supported.

### iOS, watchOS, tvOS and wasmWasi

These platforms are supported for the [core module](#core-module) only.

[BaseCliktCommand]:              api/clikt/com.github.ajalt.clikt.core/-base-clikt-command/index.html
[CliktCommand]:                  api/clikt-mordant/com.github.ajalt.clikt.core/-clikt-command/index.html
[CoreCliktCommand]:              api/clikt/com.github.ajalt.clikt.core/-core-clikt-command/index.html
[CoreNoOpCliktCommand]:          api/clikt/com.github.ajalt.clikt.core/-core-no-op-clikt-command/index.html
[CommandLineParser]:             api/clikt/com.github.ajalt.clikt.parsers/-command-line-parser/index.html
[CommandLineParser.main]:        api/clikt/com.github.ajalt.clikt.parsers/-command-line-parser/main.html
[CommandLineParser.parse]:       api/clikt/com.github.ajalt.clikt.parsers/-command-line-parser/parse.html
[CommandLineParser.parseAndRun]: api/clikt/com.github.ajalt.clikt.parsers/-command-line-parser/parse-and-run.html
[Flow]:                          https://kotlinlang.org/docs/flow.html
[ProgramResult]:                 api/clikt/com.github.ajalt.clikt.core/-program-result/index.html
[SuspendingCliktCommand]:        api/clikt-mordant/com.github.ajalt.clikt.command/-suspending-clikt-command/index.html
[TermUI]:                        api/clikt/com.github.ajalt.clikt.output/-term-ui/index.html
[aliases]:                       api/clikt/com.github.ajalt.clikt.core/-base-clikt-command/aliases.html
[readArgumentFile]:              api/clikt/com.github.ajalt.clikt.core/-context/read-argument-file.html
[callOnClose]:                   api/clikt/com.github.ajalt.clikt.core/-context/call-on-close.html
[context-obj]:                   commands.md#nested-handling-and-contexts
[customizing-context]:           commands.md#customizing-contexts
[dash-dash]:                     arguments.md#option-like-arguments-using-
[editFile]:                      api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-file.html
[editText]:                      api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-text.html
[envvars]:                       options.md#values-from-environment-variables
[file]:                          api/clikt/com.github.ajalt.clikt.parameters.types/file.html
[grouping-options]:              documenting.md#grouping-options-in-help
[main]:                          api/clikt/com.github.ajalt.clikt.core/main.html
[parse]:                         api/clikt/com.github.ajalt.clikt.core/parse.html
[path]:                          api/clikt/com.github.ajalt.clikt.parameters.types/path.html
[prompt]:                        options.md#prompting-for-input
[registerCloseable]:             api/clikt/com.github.ajalt.clikt.core/register-closeable.html
[registerJvmCloseable]:          api/clikt/com.github.ajalt.clikt.core/register-jvm-closeable.html
[test]:                          api/clikt-mordant/com.github.ajalt.clikt.testing/test.html
[transformToken]:                api/clikt/com.github.ajalt.clikt.core/-context/transform-token.html
