# Advanced Patterns

Clikt has reasonable behavior by default, but is also very customizable
for advanced use cases.

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
    $ ./hello --NAME=Foo
    Hello Foo!
    ```

## Replacing stdin and stdout

By default, functions like [`CliktCommand.main`][main] and [`option().prompt()`][prompt]
read from `System.in` and write to `System.out`. If you want to use
clikt in an environment where the standard streams aren't available, you
can set your own implementation of [`CliktConsole`][CliktConsole]
when [customizing the command context][customizing-context].

```kotlin
object MyConsole : CliktConsole {
    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        MyOutputStream.write(prompt)
        return if (hideInput) MyInputStream.readPassword()
        else MyInputStream.readLine()
    }

    override fun print(text: String, error: Boolean) {
        if (error) MyOutputStream.writeError(prompt)
        else MyOutputStream.write(prompt)
    }

    override val lineSeparator: String get() = "\n"
}

class CustomCLI : NoOpCliktCommand() {
    init { context { console = MyConsole } }
}
```

If you are using [`TermUI`][TermUI] directly,
you can also pass your custom console as an argument.

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

## Testing your Clikt CLI

[`CliktCommand.main`][main] calls `exitProcess` when invalid values are provided on the command
line. In unit tests, you should instead call [`CliktCommand.parse`][parse], which throws exceptions
with error details rather than printing the details and exiting the process. See the documentation
on [exceptions](exceptions.md) for more information.

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

All functionality is supported, except the `env` parameter of [editText][editText] and
[editFile][editFile], and the `hideInput` parameter of [prompt][prompt] are ignored.

### NodeJS

All functionality is supported, except the `hideInput` parameter of [prompt][prompt] is ignored.

### Browser JavaScript

The default [CliktConsole][CliktConsole] only outputs to the browser's developer console, which is
probably not what you want. You can [define your own CliktConsole](#replacing-stdin-and-stdout), or
you can call [parse][parse] instead of [main][main] and handle output yourself.

[editText][editText] and [editFile][editFile] are not supported. [prompt][prompt] is only supported
if you define your own CliktConsole.

[aliases]:             api/clikt/com.github.ajalt.clikt.core/-clikt-command/aliases.html
[CliktConsole]:        api/clikt/com.github.ajalt.clikt.output/-clikt-console/index.html
[customizing-context]: commands.md#customizing-contexts
[dash-dash]:           arguments.md#option-like-arguments-using-
[editFile]:            api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-file.html
[editText]:            api/clikt/com.github.ajalt.clikt.output/-term-ui/edit-text.html
[expandArgumentFiles]: api/clikt/com.github.ajalt.clikt.core/-context/expand-argument-files.html
[main]:                api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html
[parse]:               api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.html
[ProgramResult]:       api/clikt/com.github.ajalt.clikt.core/-program-result/index.html
[prompt]:              api/clikt/com.github.ajalt.clikt.parameters.options/prompt.html
[TermUI]:              api/clikt/com.github.ajalt.clikt.output/-term-ui/index.html
[tokenTransformer]:    api/clikt/com.github.ajalt.clikt.core/-context/token-transformer.html

