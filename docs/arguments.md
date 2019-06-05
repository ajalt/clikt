# Arguments

Arguments are declared and customized similarly to [options][options],
but are provided on the command line positionally instead of by name.
Arguments are declared with [`argument()`][argument],
and the order that they are declared defines the order that they
must be provided on the command line.

## Basic Arguments

By default, [`argument`][argument] takes a single `String` value which is required to be
provided on the command line.

```kotlin tab="Example"

class Hello : CliktCommand() {
    val name by argument()
    override fun run() {
        echo("Hello $name!")
    }
}
```

```text tab="Usage"
$ ./hello Foo
Hello Foo!
```

Arguments appear in the usage string, but normally aren't listed in the
help page. It's usually more clear to document arguments in the command
help.

```kotlin tab="Example"
class Cp : CliktCommand(help = "Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.") {
    private val source by argument().file(exists = true).multiple()
    private val dest by argument().file()
    override fun run() {
        // ...
    }
}
```

```text tab="Help Output"
Usage: cp [OPTIONS] [SOURCE]... DEST

  Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.

Options:
  -h, --help         Show this message and exit
```

## Variadic Arguments

Like [options][options], arguments can take any fixed number of values, which you can change with
functions like [`pair`][pair] and [`triple`][triple]. Unlike options,
arguments can take a variable (or unlimited) number of values. This is especially common when taking
file paths, since they are frequently expanded with a glob pattern on the command line.

You can declare any number of arguments with fixed numbers of values,
but only one variadic argument.

```kotlin tab="Example"
class Copy : CliktCommand() {
    val source by argument().file(exists = true).multiple()
    val dest by argument().file(fileOkay = false)
    override fun run() {
        echo("Copying files $source to $dest")
    }
}
```

```text tab="Usage"
$ ./copy file.* out/
Copying files [file.txt, file.md] to out/
```

## Option-Like Arguments

Clikt normally parses any value that starts with punctuation as an
option, which allows users to intermix options and arguments. However,
sometimes you need to pass a value that starts with punctuation to an
argument. For example, you might have a file named `-file.txt` that you
want to use as an argument.

Clikt supports the POSIX convention of using `--` to force all following
values to be treated as arguments. Any values before the `--` will be
parsed normally.

```kotlin tab="Example"
class Touch : CliktCommand() {
    val verbose by option().flag()
    val files by argument().multiple()
    override fun run() {
        if (verbose) echo(files.joinToString("\n"))
    }
}
```

```text tab="Usage 1"
$ ./touch --foo.txt
Usage: touch [OPTIONS] [FILES]...

Error: no such option: "--foo.txt".
```

```text tab="Usage 2"
$ ./touch --verbose -- --foo.txt bar.txt
--foo.txt
bar.txt
```


[options]:  options.md
[argument]: api/clikt/com.github.ajalt.clikt.parameters.arguments/argument.md
[pair]:     api/clikt/com.github.ajalt.clikt.parameters.arguments/pair.md
[triple]:   api/clikt/com.github.ajalt.clikt.parameters.arguments/triple.md
