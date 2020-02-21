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

```kotlin tab="Example"
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

```text tab="Usage 1"
$ ./repo ci -m 'my message'
Committing with message: my message
```

```text tab="Usage 2"
$ ./repo cm 'my message'
Committing with message: my message
```

Note that aliases are not expanded recursively: none of the tokens that
an alias expands to will be expanded again, even if they match another
alias.

You also use this functionality to implement command prefixes:

```kotlin
class Tool : CliktCommand() {
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

    override fun run() = Unit
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

Which allows you to call the subcommands like this:

```
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

```kotlin tab="Example"
class Hello : CliktCommand() {
    init {
        context { tokenTransformer = { it.toLowerCase() } }
    }

    val name by option()
    override fun run() = echo("Hello $name!")
}
```

```text tab="Usage"
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

class CustomCLI : CliktCommand() {
    init { context { this.console = MyConsole } }
    override fun run() {}
}
```

If you are using [`TermUI`][TermUI] directly,
you can also pass your custom console as an argument.

## Command Line Argument Files ("@-files")

Similar to `javac`, Clikt supports loading command line parameters from a file using the "@-file"
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

You can use any file path after the `@`, and can specify multiple @-files:

```
$ ./tool @../config/args @C:\\Program\ Files\\Tool\\argfile
```

If you have any options with names that start with `@`, you can still use `@-files`, but values on
the command line that match an option will be parsed as that option, rather than an `@-file`, so
you'll have to give your files a different name.

### Preventing @-file expansion

If you want to use a value starting with `@` as an argument without expanding it, you have three options:

1. Pass it after a `--`, [which disables expansion for everything that occurs after it][dash-dash].
2. Escape it with `@@`. The first `@` will be removed and the rest used as the argument value. For example, `@@file` will parse as the string `@file`
3. Disable @-file expansion entirely by setting [`Context.expandArgumentFiles = false`][expandArgumentFiles]

### File format

In argument files, normal shell quoting and escaping rules apply. Line breaks are treated as word
separators, and can be used where you would normally use a space to separate parameters. Line breaks
cannot occur within quotes. @-files can contain other @-file arguments, which will be expanded
recursively.

An unescaped `#` character outside of quotes is treated as a line comment: it and the rest of the
line are skipped. You can pass a literal `#` by escaping it with `\#` or quoting it with `'#'`.

## Testing your Clikt CLI

[`CliktCommand.main`][main] calls `exitProcess` when invalid values are provided on the command
line. In unit tests, you should instead call [`CliktCommand.parse`][parse], which throws exceptions
with error details rather than printing the details and exiting the process. See the documentation
on [exceptions][exceptions.md] for more information.

[aliases]:             api/clikt/com.github.ajalt.clikt.core/-clikt-command/aliases.md
[tokenTransformer]:    api/clikt/com.github.ajalt.clikt.core/-context/token-transformer.md
[customizing-context]: commands.md#customizing-contexts
[main]:                api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.md
[parse]:               api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.md
[prompt]:              api/clikt/com.github.ajalt.clikt.parameters.options/prompt.md
[CliktConsole]:        api/clikt/com.github.ajalt.clikt.output/-clikt-console/index.md
[TermUI]:              api/clikt/com.github.ajalt.clikt.output/-term-ui/index.md
[dash-dash]:           arguments.md#option-like-arguments-using-
[expandArgumentFiles]: api/clikt/com.github.ajalt.clikt.core/-context/expand-argument-files.md
