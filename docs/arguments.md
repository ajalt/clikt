# Arguments

Arguments are declared and customized similarly to [options][options],
but are provided on the command line positionally instead of by name.
Arguments are declared with [`argument()`][argument],
and the order that they are declared defines the order that they
must be provided on the command line.

## Basic Arguments

By default, [`argument`][argument] takes a single `String` value which is required to be
provided on the command line.

=== "Example"
    ```kotlin

    class Hello : CliktCommand() {
        val name by argument()
        override fun run() {
            echo("Hello $name!")
        }
    }
    ```

=== "Usage"
    ```text
    $ ./hello Foo
    Hello Foo!
    ```

Arguments appear in the usage string, but normally aren't listed in the
help page. It's usually more clear to document arguments in the command
help.

=== "Example"
    ```kotlin
    class Cp : CliktCommand(help = "Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.") {
        private val source by argument().file(mustExist = true).multiple()
        private val dest by argument().file()
        override fun run() {
            // ...
        }
    }
    ```

=== "Help Output"
    ```text
    Usage: cp [OPTIONS] [SOURCE]... DEST

      Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.

    Options:
      -h, --help         Show this message and exit
    ```

## Variadic Arguments

Like [options][options], arguments can take any fixed number of values, which you can change with
functions like [`pair`][pair] and [`triple`][triple]. Unlike options, arguments can also take a
variable (or unlimited) number of values. This is common with file path arguments, since
they are frequently expanded with a glob pattern on the command line.

Variadic arguments are declared with [`multiple`][multiple]. You can declare any number of arguments
with fixed numbers of values, but only one variadic argument in a command.

=== "Example"
    ```kotlin
    class Copy : CliktCommand() {
        val source: List<Path> by argument().path(mustExist = true).multiple()
        val dest: Path by argument().path(canBeFile = false)
        override fun run() {
            echo("Copying files $source to $dest")
        }
    }
    ```

=== "Usage"
    ```text
    $ ./copy file.* out/
    Copying files [file.txt, file.md] to out/
    ```

You can also use [`unique`][unique] to discard duplicates:

```kotlin
val source: Set<Path> by argument().path(mustExist = true).multiple().unique()
```

## Option-Like Arguments (Using `--`)

Clikt normally parses any value that starts with punctuation as an
option, which allows users to intermix options and arguments. However,
sometimes you need to pass a value that starts with punctuation to an
argument. For example, you might have a file named `-file.txt` that you
want to use as an argument.

Clikt supports the POSIX convention of using `--` to force all following
values to be treated as arguments. Any values before the `--` will be
parsed normally.

=== "Example"
    ```kotlin
    class Touch : CliktCommand() {
        val verbose by option().flag()
        val files by argument().multiple()
        override fun run() {
            if (verbose) echo(files.joinToString("\n"))
        }
    }
    ```

=== "Usage 1"
    ```text
    $ ./touch --foo.txt
    Usage: touch [OPTIONS] [FILES]...

    Error: no such option: "--foo.txt".
    ```

=== "Usage 2"
    ```text
    $ ./touch --verbose -- --foo.txt bar.txt
    --foo.txt
    bar.txt
    ```


[argument]: api/clikt/com.github.ajalt.clikt.parameters.arguments/argument.html
[multiple]: api/clikt/com.github.ajalt.clikt.parameters.arguments/multiple.html
[options]:  options.md
[pair]:     api/clikt/com.github.ajalt.clikt.parameters.arguments/pair.html
[triple]:   api/clikt/com.github.ajalt.clikt.parameters.arguments/triple.html
[unique]:   api/clikt/com.github.ajalt.clikt.parameters.arguments/unique.html
