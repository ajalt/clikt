---
title: Options
sidebar: home_sidebar
permalink: options.html
---

<!--  TODO: add docs links -->

Options are added to commands by defining a property delegate with the
`option()` function. The option behavior and delegate type can be
customized by calling extension functions on the `option()` call. For
example, here are some different option declarations:

```kotlin
val a: String? by option()
val b: Int? by option().int()
val c: Pair<Int, Int>? by option().int().paired()
val d: Pair<Int, Int> by option().int().paired().default(0 to 0)
val e: Pair<Float, Float> by option().float().paired().default(0f to 0f)
```

There are three type of behavior that can be customized independentaly:

1. The type of each value in the option

   The value type is `String` by default, but can be customized with
   built-in functions like `int()` or `choice()`, or manually with
   `convert()`. This is detailed in the
   [parameters](parameters.html#types) page.

2. The number of values that the option requires

   Options take one value by default, but this can be changed with
   built-in functions like `paired()` and `triple()`, or manually with
   `transformValues()`.

3. How to handle all calls to the option (i.e. if the option is not given, or is given more than once).

   By defualt, the option delegate value is the null if the option is
   not given on the command line, but you can change this behavior with
   functions like `default()` and `multiple()`.

Since the three types of customizations are orthogonal, you can choose
which ones you want to use, and if you implement a new customization, it
can be used with all of the existing functions without any repeated
code.

## Basic Options

The default option takes one value of type `String`. The property is
nullable. If the option is not given on the command line, the property
value will be null. If the option is given at least once, the property
wil return the value of the last occurance of the option.

```kotlin
class Hello: CliktCommand() {
    val name by option()
    override fun run() {
        TermUi.echo("Hello, $name!")
    }
}
```

And on the command line:

```
$ ./hello --name=Foo
Hello, Foo!
```

## Default Values

By default, option delegates return `null` if the option wasn't provided
on the command line. You can instead return a default value with
`default()`.

```kotlin
class Pow : CliktCommand() {
    val exp by option().double().default(1.0)
    override fun run() {
        TermUi.echo("2 ^ $exp = ${Math.pow(2.0, exp)}")
    }
}
```

And on the command line:

```
$ ./pow --exp 8
2 ^ 8.0 = 256.0

$ ./pow
2 ^ 1.0 = 2.0
```


## Multi Value Options

Options can take any fixed number of values. If you want a variable
number of values, you need to use and argument instead. There are built
in functions for options that take two values (`paired()`, which uses a
`Pair`), or three values (`triple()`, which uses a `Triple`).  You can
change the type of each value as normal with functions like `int()`.

If you need more values, you can provide your own container with
`transformValues()`. You give that function the number of values you
want, and a lambda that will transform a list of values into the output
container. The list will always have a size equal to the number you
specify. If the user provides a different number of values, the parser
will inform the user and your lambda won't be called.

```kotlin
data class Quad<out T>(val a: T, val b: T, val c: T, val d: T)
fun <T> Quad<T>.toList(): List<T> = listOf(a, b, c, d)
class Geometry : CliktCommand() {
    val square by option().int().paired()
    val cube by option().int().triple()
    val tesseract by option().int().transformValues(4) { Quad(it[0], it[1], it[2], it[3]) }
    override fun run() {
        TermUi.echo("Square has dimensions ${square?.toList()?.joinToString("x")}")
        TermUi.echo("Cube has dimensions ${cube?.toList()?.joinToString("x")}")
        TermUi.echo("Tesseract has dimensions ${tesseract?.toList()?.joinToString("x")}")
    }
}
```

And on the command line:

```
$ ./geometry --square 1 2 --cube 3 4 5 --tesseract 6 7 8 9
Square has dimensions 1x2
Cube has dimensions 3x4x5
Tesseract has dimensions 6x7x8x9
```


## Multiple Options

Normally, when an option is provided on the command line more than once,
only the values from the last occurance are used. But sometimes you want
to keep all values provided. For example, `git commit -m foo -m bar`
would create a commit message with two lines: `foo` and `bar. To get
this behavior with Clikt, you can use `multiple()`. This will cause the
property delegate value to be a list, where each item in the list is the
values from one occurance of the option. If the option is never given,
the list will be empty.

```kotlin
class Commit : CliktCommand() {
    val message by option("-m").multiple()
    override fun run() {
        TermUi.echo(message.joinToString("\n"))
    }
}
```

And on the command line:

```
$ ./commit -m foo -m bar
foo
bar
```

You can combine `multiple()` with item type conversions and multiple
values. For example:

```kotlin
val opt: List<Pair<Int, Int>> option().int().paired().multiple()
```

## Boolean Flag Options

Flags are options that don't take a value. Boolean flags can be enabled
or disabled, depending on the name used to invoke the option. You can
turn an option into a boolean flag with `flag()`. That function takes an
optional list of secondary names that will be added to any existing or
inferred names for the option. If the option is invoked with one of the
secondary names, the delegate will return false. It's a good idea to
always set seconadry names so that a user can disable the flag if it was
enabled previously.


```kotlin
class Cli : CliktCommand() {
    val flag by option("--on", "-o").flag("--off", "-O", default = false)
    override fun run() {
        TermUi.echo(flag)
    }
}
```

And on the command line:

```
$ ./cli -o
true

$ ./cli --on --off
false
```

## Counted Flag Options

You might want a flag option that counts the number of times it occurs
on the command line. You can use `counted()` for this.

```kotlin
class Log : CliktCommand() {
    val verbosity by option("-v").counted()
    override fun run() {
        TermUi.echo("Verbosity level: $verbosity")
    }
}
```

And on the command line:

```
$ ./log -vvv
Verbosity level: 3
```

## Feature Switch Flags

Another way to use flags to to assign a value to each option name. You
can do this with `switch()`, which takes a map of option names to
values. Note that the names in the map replace any previously specified
or inferred names.

```kotlin
class Size : CliktCommand() {
    val size by option().switch("--large" to "large", "--small" to "small").default("unknown")
    override fun run() {
        TermUi.echo("You picked size $size")
    }
}
```

And on the command line:

```
$ ./size --small
You picked size small
```

## Choice Options

You can restrict the values that a regular option can take to a set of
values using `choice()`. You can also map the input values to new types.

```kotlin
class Digest : CliktCommand() {
    val hash by option().choice("md5", "sha1")
    override fun run() {
        TermUi.echo(hash)
    }
}
```

And on the command line:

```
$ ./digest --hash=md5
md5

$ ./digest --hash=sha256
Usage: digest [OPTIONS]

Error: Invalid value for "--hash": invalid choice: sha256. (choose from md5, sha1)

$ ./digest --help
Usage: digest [OPTIONS]

Options:
  --hash [md5|sha1]
  -h, --help         Show this message and exit
```


## Prompting for input

In some cases, you might want to create an option that uses the value
given on the command line if there is one, but prompt the user for input
if one is not provided. Clikt can take care of this for you with the
`prompt()` function.

```kotlin
class Hello : CliktCommand() {
    val name by option().prompt()
    override fun run() {
        TermUi.echo("Hello $name")
    }
}
```

Which behaves like:

```
./hello --name=foo
Hello foo

./hello
Name: foo
Hello foo
```

The default prompt string is based on the option name, but `prompt()`
takes a number of parameters to customize the output.

## Password prompts

You can also create a option that uses a hidden prompt and asks for
confirmation. This combination of behavior is commonly used for
passwords.

```kotlin
class Login : CliktCommand() {
    val password by option().prompt(requireConfirmation = true, hideInput = true)
    override fun run() {
        TermUi.echo("Securely printing password to the screen: $password")
    }
}
```

And on the command line:

```
$ ./login
Password:
Repeat for confirmation:
Securely printing password to the screen: hunter2
```


## Eager Options

Sometimes you want an option to halt execution immediately and print a
message. For example, the built-on `--help` option, or the `--version`
option that many programs have. Neither of these options have any value
associated with them, and they stop command line parsing as soon as
they're encountered.

The `--help` option is added automatically to commands, and `--version`
can be added using `versionOption()`. Since the option doesn't have a
value, you can't define it using a property delegate. Instead, call the
function on a command directly, either in an `init` block, or on a
command instance.

These definitions are equivalent:

```kotlin
class Cli : NoRunCliktCommand() {
    init {
        versionOption("1.0")
    }
}
fun main(args: Array<String>) = Cli().main(args)
```

and

```kotlin
class Cli : NoRunCliktCommand()
fun main(args: Array<String>) = Cli().versionOption("1.0").main(splitArgv(""))
```

Any they work like:

```
$ ./cli --version
cli version 1.0
```

If you want to define your own option with a similar behavior, you can
do so by creating an instance of `EagerOption` and passing it to
`CliktCommand.registerOption`. `EagerOption`s have a `callback` that is
called when the option is encountered on the command line. To print a
message and halt execution normally from the callback, you can throw a
`PrintMessage` exception, and `CliktCommand.main()` will take care of
printing the message.

You can define your own version option like this:

```kotlin
class Cli : CliktCommand() {
    init {
        registerOption(EagerOption("--version") {
            throw PrintMessage("$commandName version 1.0")
        })
    }

    override fun run() {
        // ...
    }
}
```

## Values from Environment Variables



{% include links.html %}
