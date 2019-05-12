# Options

Options are added to commands by defining a property delegate with the
[`option`](api/clikt/com.github.ajalt.clikt.parameters.options/option.html) function.

## Basic Options

The default option takes one value of type `String`. The property is
nullable. If the option is not given on the command line, the property
value will be null. If the option is given at least once, the property
will return the value of the last occurrence of the option.

```kotlin
class Hello: CliktCommand() {
    val name by option(help="your name")
    override fun run() {
        echo("Hello, $name!")
    }
}
```

And on the command line:

```
$ ./hello --name=Foo
Hello, Foo!
```

## Option Names

If you don't specify names for an option, a lowercase hyphen-separated
name is automatically inferred from the property. For example, `val
myOpt by option()` will create an option that can be called with
`--my-opt`.

You can also specify any number of names for an option manually:

```kotlin
class Hello: CliktCommand() {
    val name by option("-n", "--name", help="your name")
    override fun run() {
        echo("Hello, $name!")
    }
}
```

Option names that are two characters long (like `-n`) are treated as
POSIX-style short options. You call them with a value like this:

```
$ ./hello -nfoo
Hello, foo!
```

or:

```
$ ./hello -n foo
Hello, foo!
```

All other option names are considered long options, and can be called
like this:

```
$ ./hello --name=foo
Hello, foo!
```

or:

```
$ ./hello --name foo
Hello, foo!
```

## Customizing Options

The option behavior and delegate type can be customized by calling extension functions on the
[`option`](api/clikt/com.github.ajalt.clikt.parameters.options/option.html) call. For example, here
are some different option declarations:

```kotlin
val a: String? by option()
val b: Int? by option().int()
val c: Pair<Int, Int>? by option().int().pair()
val d: Pair<Int, Int> by option().int().pair().default(0 to 0)
val e: Pair<Float, Float> by option().float().pair().default(0f to 0f)
```

There are three main types of behavior that can be customized
independently:

1. The type of each value in the option

   The value type is `String` by default, but can be customized with built-in functions like
   [`int`](api/clikt/com.github.ajalt.clikt.parameters.types/int.html) or
   [`choice`](api/clikt/com.github.ajalt.clikt.parameters.types/choice.html), or manually with
   [`convert`](api/clikt/com.github.ajalt.clikt.parameters.options/convert.html). This is detailed
   in the [parameters](parameters.md#parameter-types) page.

2. The number of values that the option requires

   Options take one value by default, but this can be changed with
   built-in functions like [`pair`](api/clikt/com.github.ajalt.clikt.parameters.options/pair.html)
   and [`triple`](api/clikt/com.github.ajalt.clikt.parameters.options/triple.html), or manually with
   [`transformValues`](api/clikt/com.github.ajalt.clikt.parameters.options/transform-values.html).

3. How to handle all calls to the option (i.e. if the option is not given, or is given more than once).

   By default, the option delegate value is the null if the option is
   not given on the command line, but you can change this behavior with
   functions like [`default`](api/clikt/com.github.ajalt.clikt.parameters.options/default.html) and
   [`multiple`](api/clikt/com.github.ajalt.clikt.parameters.options/multiple.html).

Since the three types of customizations are orthogonal, you can choose
which ones you want to use, and if you implement a new customization, it
can be used with all of the existing functions without any repeated
code.

## Default Values

By default, option delegates return `null` if the option wasn't provided
on the command line. You can instead return a default value with 
[`default`](api/clikt/com.github.ajalt.clikt.parameters.options/default.html).

```kotlin
class Pow : CliktCommand() {
    val exp by option("-e", "--exp").double().default(1.0)
    override fun run() {
        echo("2 ^ $exp = ${Math.pow(2.0, exp)}")
    }
}
```

And on the command line:

```
$ ./pow -e 8
2 ^ 8.0 = 256.0
```

```
$ ./pow
2 ^ 1.0 = 2.0
```

If the default value is expensive to compute, you can use
[`defaultLazy`](api/clikt/com.github.ajalt.clikt.parameters.options/default-lazy.html) instead of
[`default`](api/clikt/com.github.ajalt.clikt.parameters.options/default.html). It has the same
effect, but you give it a lambda returning the default value, and the lambda will only be called if
the default value is used.

## Multi Value Options

Options can take any fixed number of values separated by whitespace, or a variable number of values
separated by a non-whitespace delimiter you specify. If you want a variable number of values
separated by whitespace, you need to use an argument instead.

### Options With Fixed Number of Values

There are built in functions for options that take two values
([`pair`](api/clikt/com.github.ajalt.clikt.parameters.options/pair.html), which uses a `Pair`), or
three values ([`triple`](api/clikt/com.github.ajalt.clikt.parameters.options/triple.html), which
uses a `Triple`).  You can change the type of each value as normal with functions like
[`int`](api/clikt/com.github.ajalt.clikt.parameters.types/int.html).

If you need more values, you can provide your own container with
[`transformValues`](api/clikt/com.github.ajalt.clikt.parameters.options/transform-values.html). You
give that function the number of values you want, and a lambda that will transform a list of values
into the output container. The list will always have a size equal to the number you specify. If the
user provides a different number of values, Clikt will inform the user and your lambda won't be
called.

```kotlin
data class Quad<out T>(val a: T, val b: T, val c: T, val d: T)
fun <T> Quad<T>.toList(): List<T> = listOf(a, b, c, d)

class Geometry : CliktCommand() {
    val square by option().int().pair()
    val cube by option().int().triple()
    val tesseract by option().int().transformValues(4) { Quad(it[0], it[1], it[2], it[3]) }
    override fun run() {
        echo("Square has dimensions ${square?.toList()?.joinToString("x")}")
        echo("Cube has dimensions ${cube?.toList()?.joinToString("x")}")
        echo("Tesseract has dimensions ${tesseract?.toList()?.joinToString("x")}")
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

### Options With a Variable Number of Values

You can use [`split`](api/clikt/com.github.ajalt.clikt.parameters.options/split.html) to allow a
variable number of values to a single option invocation by separating the values with non-whitespace
delimiters.

```kotlin
class C : CliktCommand() {
    val profiles by option("-P").split(",")
    override fun run() {
        for (profile in profiles) {
            echo(profile)
        }
    }
}
```

And on the command line:

```
$ ./split -P profile-1,profile-2
profile-1
profile-2
```


## Multiple Options

Normally, when an option is provided on the command line more than once,
only the values from the last occurrence are used. But sometimes you
want to keep all values provided. For example, `git commit -m foo -m
bar` would create a commit message with two lines: `foo` and `bar`. To
get this behavior with Clikt, you can use [`multiple`](api/clikt/com.github.ajalt.clikt.parameters.options/multiple.html). This will cause the property
delegate value to be a list, where each item in the list is the value of
from one occurrence of the option. If the option is never given, the
list will be empty (or you can specify a list to use).

```kotlin
class Commit : CliktCommand() {
    val message by option("-m").multiple()
    override fun run() {
        echo(message.joinToString("\n"))
    }
}
```

And on the command line:

```
$ ./commit -m foo -m bar
foo
bar
```

You can combine [`multiple`](api/clikt/com.github.ajalt.clikt.parameters.options/multiple.html) with item type conversions and multiple values. For
example:

```kotlin
val opt: List<Pair<Int, Int>> by option().int().pair().multiple()
```

## Boolean Flag Options

Flags are options that don't take a value. Boolean flags can be enabled
or disabled, depending on the name used to invoke the option. You can
turn an option into a boolean flag with [`flag`](api/clikt/com.github.ajalt.clikt.parameters.options/flag.html). That function takes an optional
list of secondary names that will be added to any existing or inferred
names for the option. If the option is invoked with one of the secondary
names, the delegate will return false. It's a good idea to always set
secondary names so that a user can disable the flag if it was enabled
previously.


```kotlin
class Cli : CliktCommand() {
    val flag by option("--on", "-o").flag("--off", "-O", default = false)
    override fun run() {
        echo(flag)
    }
}
```

And on the command line:

```
$ ./cli -o
true
```

```
$ ./cli --on --off
false
```


Multiple short flag options can be combined when called on the command
line:

```kotlin
class Cli : CliktCommand() {
    val flagA by option("-a").flag()
    val flagB by option("-b").flag()
    val foo by option("-f")
    override fun run() {
        echo("$flagA $flagB $foo")
    }
}
```

And on the command line:

```
$ ./cli -abfFoo
true true Foo
```


## Counted Flag Options

You might want a flag option that counts the number of times it occurs
on the command line. You can use [`counted`](api/clikt/com.github.ajalt.clikt.parameters.options/counted.html) for this.

```kotlin
class Log : CliktCommand() {
    val verbosity by option("-v").counted()
    override fun run() {
        echo("Verbosity level: $verbosity")
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
can do this with [`switch`](api/clikt/com.github.ajalt.clikt.parameters.options/switch.html), which takes a map of option names to values. Note that
the names in the map replace any previously specified or inferred names.

```kotlin
class Size : CliktCommand() {
    val size by option().switch("--large" to "large", "--small" to "small").default("unknown")
    override fun run() {
        echo("You picked size $size")
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
values using [`choice`](api/clikt/com.github.ajalt.clikt.parameters.types/choice.html). You can also map the input values to new types.

```kotlin
class Digest : CliktCommand() {
    val hash by option().choice("md5", "sha1")
    override fun run() {
        echo(hash)
    }
}
```

And on the command line:

```
$ ./digest --hash=md5
md5
```

```
$ ./digest --hash=sha256
Usage: digest [OPTIONS]

Error: Invalid value for "--hash": invalid choice: sha256. (choose from md5, sha1)
```

```
$ ./digest --help
Usage: digest [OPTIONS]

Options:
  --hash [md5|sha1]
  -h, --help         Show this message and exit
```

## Prompting For Input

In some cases, you might want to create an option that uses the value
given on the command line if there is one, but prompt the user for input
if one is not provided. Clikt can take care of this for you with the [`prompt`](api/clikt/com.github.ajalt.clikt.parameters.options/prompt.html) function.

```kotlin
class Hello : CliktCommand() {
    val name by option().prompt()
    override fun run() {
        echo("Hello $name")
    }
}
```

Which behaves like:

```
./hello --name=foo
Hello foo
```

```
./hello
Name: foo
Hello foo
```

The default prompt string is based on the option name, but [`prompt`](api/clikt/com.github.ajalt.clikt.parameters.options/prompt.html) takes a number of
parameters to customize the output.

## Password Prompts

You can also create a option that uses a hidden prompt and asks for
confirmation. This combination of behavior is commonly used for
passwords.

```kotlin
class Login : CliktCommand() {
    val password by option().prompt(requireConfirmation = true, hideInput = true)
    override fun run() {
        echo("Your hidden password: $password")
    }
}
```

And on the command line:

```
$ ./login
Password:
Repeat for confirmation:
Your hidden password: hunter2
```


## Eager Options

Sometimes you want an option to halt execution immediately and print a
message. For example, the built-on `--help` option, or the `--version`
option that many programs have. Neither of these options have any value
associated with them, and they stop command line parsing as soon as
they're encountered.

The `--help` option is added automatically to commands, and `--version` can be added using
[`versionOption`](api/clikt/com.github.ajalt.clikt.parameters.options/version-option.html). Since
the option doesn't have a value, you can't define it using a property delegate. Instead, call the
function on a command directly, either in an `init` block, or on a command instance.

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
fun main(args: Array<String>) = Cli().versionOption("1.0").main(args)
```

And they work like:

```
$ ./cli --version
cli version 1.0
```

If you want to define your own option with a similar behavior, you can do so by creating an instance
of [`EagerOption`](api/clikt/com.github.ajalt.clikt.parameters.options/-eager-option/index.html) and
passing it to
[`CliktCommand.registerOption`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/register-option.html).
`EagerOption`s have a `callback` that is called when the option is encountered on the command line.
To print a message and halt execution normally from the callback, you can throw a
[`PrintMessage`](api/clikt/com.github.ajalt.clikt.core/-print-message/index.html) exception, and
[`CliktCommand.main`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html) will take care
of printing the message.

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

## Deprecating Options

You can communicate to users that an option is deprecated with
[`option().deprecated()`](api/clikt/com.github.ajalt.clikt.parameters.options/deprecated.html). By
default, this function will add a tag to the option's help message, and print a warning to stderr if
the option is used.

You can customize or omit the warning message and help tags, or change the warning into an error.

```kotlin
class Cli : CliktCommand() {
   val opt by option(help = "option 1").deprecated()
   val opt2 by option(help = "option 2").deprecated("WARNING: --opt2 is deprecated, use --new-opt instead", tagName = null)
   val opt3 by option(help = "option 3").deprecated(tagName = "pending deprecation", tagValue = "use --new-opt instead")
   val opt4 by option(help = "option 4").deprecated(error = true)

   override fun run() = echo("command run")
}
```

And on the command line:
 
```
$ ./cli --opt=x
WARNING: option --opt is deprecated
command run

$ ./cli --opt2=x
WARNING: --op2 is deprecated, use --new-opt instead
command run

$ ./cli --opt3=x
WARNING: option --opt3 is deprecated
command run

$ ./cli --opt4=x
ERROR: option --opt4 is deprecated

$ ./cli --help
Usage: cli [OPTIONS]

Options:
  --opt TEXT   option 1 (deprecated)
  --opt2 TEXT  option 2
  --opt3 TEXT  option 3 (pending deprecation: use --new-opt instead)
  --opt4 TEXT  option 4 (deprecated)
```

## Values From Environment Variables

Clikt supports reading option values from environment variables if they
aren't given on the command line. This feature is helpful when
automating tools. For example, when using `git commit`, you can set the
author date with a command line parameter:
`git commit --date=10/21/2015`. But you can also set it with an
environment variable: `GIT_AUTHOR_DATE=10/21/2015 git commit`.

Clikt will read option values from environment variables as long as it
has an envvar name for the option. There are two ways to set that name:
you can set the name manually for an option, or you can enable automatic
envvar name inference.

To set the envvar name manually, pass the name to [`option`](api/clikt/com.github.ajalt.clikt.parameters.options/option.html):

```kotlin
class Hello : CliktCommand() {
    val name by option(envvar = "MY_NAME")
    override fun run() {
        echo("Hello $name")
    }
}
```

And on the command line:

```
$ export MY_NAME=Foo
$ ./hello
Hello Foo
```

```
$ export MY_NAME=Foo
$ ./hello --name=Bar
Hello Bar
```

You can enable automatic envvar name inference by setting the `autoEnvvarPrefix` on a command's
[`context`](api/clikt/com.github.ajalt.clikt.core/context.html). This will cause all options without
an explicit envvar name to be given an uppercase underscore-separated envvar name. Since the prefix
is set on the [`context`](api/clikt/com.github.ajalt.clikt.core/context.html), it is propagated to
subcommands. If you have a a subcommand called `foo` with an option `--bar`, and your prefix is
`MY_TOOL`, the option's envvar name will be `MY_TOOL_FOO_BAR`.

```kotlin
class Hello : CliktCommand() {
    init {
        context { autoEnvvarPrefix = "HELLO" }
    }
    val name by option()
    override fun run() {
        echo("Hello $name")
    }
}
```

And on the command line:

```
$ export HELLO_NAME=Foo
$ ./hello
Hello Foo
```

## Multiple Values from Environment Variables

You might need to allow users to specify multiple values for an option in a single environment
variable. You can do this by creating an option with
[`multiple`](api/clikt/com.github.ajalt.clikt.parameters.options/multiple.html). The environment
variable's value will be split according a regex, which defaults to split on whitespace for most
types. [`file`](api/clikt/com.github.ajalt.clikt.parameters.types/file.html) will change the pattern
to split according to the operating system's path splitting rules. On Windows, it will split on
semicolons (`;`). On other systems, it will split on colons (`:`). You can also specify a split
pattern by passing it to the `envvarSplit` parameter of `option`.

```kotlin
class Hello : CliktCommand() {
    val names by option(envvar = "NAMES").multiple()
    override fun run() {
        for (name in names) echo("Hello $name")
    }
}
```

And on the command line:

```
$ export NAMES=Foo Bar
$ ./hello
Hello Foo
Hello Bar
```

## Windows and Java-Style Option Prefixes

When specifying option names manually, you can use any prefix (as long
as it's entirely punctuation).

For example, you can make a Windows-style interface with slashes:

```kotlin
class Hello: CliktCommand() {
    val name by option("/name", help="your name")
    override fun run() {
        echo("Hello, $name!")
    }
}
```

An on the command line:

```
$ ./hello /name Foo
Hello, Foo!
```

Or you can make a Java-style interface that uses single-dashes for long
options:

```kotlin
class Hello: CliktCommand() {
    val name by option("-name", help="your name")
    override fun run() {
        echo("Hello, $name!")
    }
}
```

An on the command line:

```
$ ./hello -name Foo
Hello, Foo!
```

Note that inferred names will always have a POSIX-style prefix like
`--name`. If you want to use a different prefix, you should specify all
option names manually.

## Option Transformation Order

Clikt has a large number of extension functions that can modify options. When applying multiple
functions to the same option, there's only one valid order for the functions to be applied. For
example, `option().default(3).int()` will not compile, because
[`default`](api/clikt/com.github.ajalt.clikt.parameters.options/default.html) must be applied after
the value type conversion. Similarly, you can only apply one transform of each type. So
`option().int().float()` is invalid since
[`int`](api/clikt/com.github.ajalt.clikt.parameters.types/int.html) and
[`float`](api/clikt/com.github.ajalt.clikt.parameters.types/float.html) both change the value type,
as is `option().default("").multiple()` since
[`default`](api/clikt/com.github.ajalt.clikt.parameters.options/default.html) and
[`multiple`](api/clikt/com.github.ajalt.clikt.parameters.options/multiple.html) both transform the
call list (if you need a custom default value for `multiple`, you can pass it one as an argument).

Here's an integer option with one of each available transform in a valid
order:

```kotlin
val opt: Pair<Int, Int> by option("-o", "--opt")
        .int()
        .restrictTo(1..100)
        .pair()
        .default(1 to 2)
        .validate { require(it.second % 2 == 0) }
```
