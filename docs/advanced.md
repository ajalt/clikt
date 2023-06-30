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

Note that aliases are not expanded recursively: none of the tokens that
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
works on more types of tokens. You can set a [`tokenTransformer`][tokenTransformer] on the
[command's context][customizing-context] that will be
called for each option and command name that is input. This can be used
to implement case-insensitive parsing, for example:

=== "Example"
    ```kotlin
    class Hello : CliktCommand() {
        init {
            context { tokenTransformer = { it.lowercase() } }
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
3. Disable @argfile expansion entirely by setting [`Context.expandArgumentFiles = false`][expandArgumentFiles]

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

## Custom exit status codes

Clikt will normally exit your program with a status code of 0 for a normal execution, or 1 if
there's an error. If you want to use a different value, you can `throw ProgramResult(statusCode)`.
If you use [`CliktCommand.main`][main], that exception will be caught and `exitProcess` will be
called with the value of `statusCode`.

You could also call `exitProcess` yourself, but the [ProgramResult][ProgramResult] has a couple of
advantages:

- `ProgramResult` is easier to test. Exiting the process makes unit tests difficult to run.
- `ProgramResult` works on all platforms. `exitProcess` is only available on the JVM.

## Multiplatform Support

Clikt supports the following platforms in addition to JVM:

### Desktop native (Linux, Windows, and MacOS)

All functionality is supported, except:

* `env` parameter of [editText][editText] and [editFile][editFile] is ignored.
* [file][file] and [path][path] parameter types are not supported.

### NodeJS

All functionality is supported, except:

* [file][file] and [path][path] parameter types are not supported.

### Browser JavaScript

All functionality is supported, except:

* The default terminal only outputs to the browser's developer console, which is
probably not what you want. You can [define your own CliktConsole](#replacing-stdin-and-stdout), or
you can call [parse][parse] instead of [main][main] and handle output yourself.
* [editText][editText] and [editFile][editFile] are not supported.
* [file][file] and [path][path] parameter types are not supported.

[aliases]:             api/clikt/com.github.ajalt.clikt.core/-clikt-command/aliases.html
[CliktConsole]:        api/clikt/com.github.ajalt.clikt.output/-clikt-console/index.html
[context-obj]:         commands.md#nested-handling-and-contexts
[customizing-context]: commands.md#customizing-contexts
[dash-dash]:           arguments.md#option-like-arguments-using-
[editFile]:            api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-file.html
[editText]:            api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-text.html
[expandArgumentFiles]: api/clikt/com.github.ajalt.clikt.core/-context/expand-argument-files.html
[file]:                api/clikt/com.github.ajalt.clikt.parameters.types/file.html
[grouping-options]:    documenting.md#grouping-options-in-help
[main]:                api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html
[parse]:               api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.html
[path]:                api/clikt/com.github.ajalt.clikt.parameters.types/path.html
[ProgramResult]:       api/clikt/com.github.ajalt.clikt.core/-program-result/index.html
[prompt]:              api/clikt/com.github.ajalt.clikt.parameters.options/prompt.html
[test]:                api/clikt/com.github.ajalt.clikt.testing/test.html
[TermUI]:              api/clikt/com.github.ajalt.clikt.output/-term-ui/index.html
[tokenTransformer]:    api/clikt/com.github.ajalt.clikt.core/-context/token-transformer.html
