# Options

Options are added to commands by defining a property delegate with the
[`option`][option] function.

## Basic Options

The default option takes one value of type `String`. The property is
nullable. If the option is not given on the command line, the property
value will be null. If the option is given at least once, the property
will return the value of the last occurrence of the option.

```kotlin tab="Example"
class Hello: CliktCommand() {
    val name by option(help="your name")
    override fun run() {
        echo("Hello, $name!")
    }
}
```

```text tab="Usage"
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

```text tab="Usage 1"
$ ./hello -nfoo
Hello, foo!
```

```text tab="Usage 2"
$ ./hello -n foo
Hello, foo!
```

All other option names are considered long options, and can be called
like this:

```text tab="Usage 1"
$ ./hello --name=foo
Hello, foo!
```

```text tab="Usage 2"
$ ./hello --name foo
Hello, foo!
```

## Customizing Options

The option behavior and delegate type can be customized by calling extension functions on the
[`option`][option] call. For example, here are some different option declarations:

```kotlin
val a: String? by option()
val b: Int? by option().int()
val c: Pair<Int, Int>? by option().int().pair()
val d: Pair<Int, Int> by option().int().pair().default(0 to 0)
val e: Pair<Float, Float> by option().float().pair().default(0f to 0f)
```

There are three main types of behavior that can be customized
independently:

1. **The type of each value in the option.**
   The value type is `String` by default, but can be customized with built-in functions like
   [`int`][int] or [`choice`][choice], or manually with [`convert`][convert].
   This is detailed in the [parameters][parameter-types] page.
2. **The number of values that the option requires.**
   Options take one value by default, but this can be changed with
   built-in functions like [`pair`][pair] and [`triple`][triple], or manually with
   [`transformValues`][transformValues].
3. **How to handle all calls to the option (i.e. if the option is not present, or is present more than once).**
   By default, the option delegate value is the null if the option is not given on the command line,
   and will use the value of the last occurrence if the option is given more than once. You can
   change this behavior with functions like [`default`][default] and [`multiple`][multiple].

Since the three types of customizations are orthogonal, you can choose
which ones you want to use, and if you implement a new customization, it
can be used with all of the existing functions without any repeated
code.

## Default Values

By default, option delegates return `null` if the option wasn't provided
on the command line. You can instead return a default value with [`default`][default].

```kotlin tab="Example"
class Pow : CliktCommand() {
    val exp by option("-e", "--exp").double().default(1.0)
    override fun run() {
        echo("2 ^ $exp = ${(2.0).pow(exp)}")
    }
}
```

```text tab="Usage 1"
$ ./pow -e 8
2 ^ 8.0 = 256.0
```

```text tab="Usage 2"
$ ./pow
2 ^ 1.0 = 2.0
```

If the default value is expensive to compute, you can use
[`defaultLazy`][defaultLazy] instead of [`default`][default].
It has the same effect, but you give it a lambda returning the default value,
and the lambda will only be called if the default value is used.

## Multi Value Options

Options can take any fixed number of values separated by whitespace,
or a variable number of values separated by a non-whitespace delimiter you specify.
If you want a variable number of values separated by whitespace, you need to use an argument instead.

### Options With Fixed Number of Values

There are built in functions for options that take two values ([`pair`][pair], which uses a `Pair`),
or three values ([`triple`][triple], which uses a `Triple`). You can change the type of each value
as normal with functions like [`int`][int].

If you need more values, you can provide your own container with
[`transformValues`][transformValues]. You give that function the number of values you want, and a
lambda that will transform a list of values into the output container. The list will always have a
size equal to the number you specify. If the user provides a different number of values, Clikt will
inform the user and your lambda won't be called.

```kotlin tab="Example"
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

```text tab="Usage"
$ ./geometry --square 1 2 --cube 3 4 5 --tesseract 6 7 8 9
Square has dimensions 1x2
Cube has dimensions 3x4x5
Tesseract has dimensions 6x7x8x9
```

### Options With a Variable Number of Values

You can use [`split`][split] to allow a
variable number of values to a single option invocation by separating the values with non-whitespace
delimiters.

```kotlin tab="Example"
class C : CliktCommand() {
    val profiles by option("-P").split(",")
    override fun run() {
        for (profile in profiles) {
            echo(profile)
        }
    }
}
```

```text tab="Usage"
$ ./split -P profile-1,profile-2
profile-1
profile-2
```


## Multiple Options

Normally, when an option is provided on the command line more than once,
only the values from the last occurrence are used. But sometimes you
want to keep all values provided. For example, `git commit -m foo -m
bar` would create a commit message with two lines: `foo` and `bar`. To
get this behavior with Clikt, you can use [`multiple`][multiple]. This
will cause the property delegate value to be a list, where each item in
the list is the value of from one occurrence of the option. If the option
is never given, the list will be empty (or you can specify a default to use).

```kotlin tab="Example"
class Commit : CliktCommand() {
    val message: List<String> by option("-m").multiple()
    override fun run() {
        echo(message.joinToString("\n"))
    }
}
```

```text tab="Usage"
$ ./commit -m foo -m bar
foo
bar
```

You can combine [`multiple`][multiple] with item type conversions and multiple values.

```kotlin
val opt: List<Pair<Int, Int>> by option().int().pair().multiple()
```

### Default values for option().multiple()

You can also supply a default value to [`multiple`][multiple] or require at least one value be present
on the command line. These are specified as arguments rather than with separate extension functions
since they don't change the type of the delegate.

```kotlin tab="Required"
val opt: List<String> by option().multiple(required=true)
```

```kotlin tab="Default"
val opt: List<String> by option().multiple(default=listOf("default message"))
```

### Deduplicating option().multiple() into a unique set

You can discard duplicate values from a `multiple` option with [`unique`][unique].

```kotlin tab="Example"
class Build : CliktCommand() {
    val platforms: Set<String> by option("-p").multiple().unique()
    override fun run() {
        echo("Building for platforms: $platforms")
    }
}
```

```text tab="Usage"
$ ./build -p android -p ios -p android
Building for platforms: [android, ios]
```

## Key-Value and Map Options

You can split an option's value into a key-value pair with [`splitPair`][splitPair]. By default, the
delimiter `=` will be used to split. You can also use [`associate`][associate] to allow the option
to be specified multiple times, and have its values collected in a map.

```kotlin tab="Example"
class Build : CliktCommand() {
    val systemProp: Map<String, String> by option("-D", "--system-prop").associate()

    override fun run() {
        echo(systemProp)
    }
}
```

```text tab="Usage"
$ ./build -Dplace=here --system-prop size=small
{place=here, size=small}
```

## Boolean Flag Options

Flags are options that don't take a value. Boolean flags can be enabled
or disabled, depending on the name used to invoke the option. You can
turn an option into a boolean flag with [`flag`][flag]. That function takes an optional
list of secondary names that will be added to any existing or inferred
names for the option. If the option is invoked with one of the secondary
names, the delegate will return false. It's a good idea to always set
secondary names so that a user can disable the flag if it was enabled
previously.


```kotlin tab="Example"
class Cli : CliktCommand() {
    val flag by option("--on", "-o").flag("--off", "-O", default = false)
    override fun run() {
        echo(flag)
    }
}
```

```text tab="Usage 1"
$ ./cli -o
true
```

```text tab="Usage 2"
$ ./cli --on --off
false
```


Multiple short flag options can be combined when called on the command
line:

```kotlin tab="Example"
class Cli : CliktCommand() {
    val flagA by option("-a").flag()
    val flagB by option("-b").flag()
    val foo by option("-f")
    override fun run() {
        echo("$flagA $flagB $foo")
    }
}
```

```text tab="Usage"
$ ./cli -abfFoo
true true Foo
```

## Counted Flag Options

You might want a flag option that counts the number of times it occurs on the command line. You can
use [`counted`][counted] for this.

```kotlin tab="Example"
class Log : CliktCommand() {
    val verbosity by option("-v").counted()
    override fun run() {
        echo("Verbosity level: $verbosity")
    }
}
```

```text tab="Usage"
$ ./log -vvv
Verbosity level: 3
```

## Feature Switch Flags

Another way to use flags is to assign a value to each option name. You can do this with
[`switch`][switch], which takes a map of
option names to values. Note that the names in the map replace any previously specified or inferred
names.

```kotlin tab="Example"
class Size : CliktCommand() {
    val size by option().switch(
        "--large" to "large", 
        "--small" to "small"
    ).default("unknown")
    override fun run() {
        echo("You picked size $size")
    }
}
```

```text tab="Usage"
$ ./size --small
You picked size small
```

## Choice Options

You can restrict the values that a regular option can take to a set of values using
[`choice`][choice]. You can also map the
input values to new types.

```kotlin tab="Example"
class Digest : CliktCommand() {
    val hash by option().choice("md5", "sha1")
    override fun run() {
        echo(hash)
    }
}
```

```text tab="Usage 1"
$ ./digest --hash=md5
md5
```

```text tab="Usage 2"
$ ./digest --hash=sha256
Usage: digest [OPTIONS]

Error: Invalid value for "--hash": invalid choice: sha256. (choose from md5, sha1)
```

```text tab="Usage 3"
$ ./digest --help
Usage: digest [OPTIONS]

Options:
  --hash [md5|sha1]
  -h, --help         Show this message and exit
```

## Mutually Exclusive Option Groups

If [`choice`][choice] or [`switch`][switch] options aren't flexible enough,
you can use [`mutuallyExclusiveOptions`][mutuallyExclusiveOptions]
to group any nullable options into a mutually exclusive group. If more than one of the options in
the group is given on the command line, the last value is used.

If you want different types for each option, you can wrap them in a sealed class. You can also use
[`wrapValue`][wrapValue] if you have an existing conversion function like [int][int] or [file][file]
you'd like to use.

```kotlin tab="Example"
sealed class Fruit {
    data class Oranges(val size: String): Fruit()
    data class Apples(val count: Int): Fruit()
}
class Order : CliktCommand() {
    val fruit: Fruit? by mutuallyExclusiveOptions<Fruit>(
        option("--oranges").convert { Oranges(it) },
        option("--apples").int().wrapValue { Apples(it) }
    )

    override fun run() = echo(fruit)
}
```

```text tab="Usage 1"
$ ./order --apples=10
Apples(count=10)
```

```text tab="Usage 2"
$ ./order --oranges=small
Oranges(size=small)
```

```text tab="Usage 3"
$ ./order --apples=10 --oranges=large
Oranges(size=large)
```

You can enforce that only one of the options is given with [`single`][single]:

```kotlin tab="Example"
val fruit: Fruit? by mutuallyExclusiveOptions<Fruit>(
        option("--apples").convert { Apples(it.toInt()) },
        option("--oranges").convert { Oranges(it) }
).single()
```

```text tab="Usage"
$ ./order --apples=10 --oranges=small
Usage: order [OPTIONS]

Error: option --apples cannot be used with --oranges
```

Like regular options, you can make the entire group
[`required`][required], or give it a [`default`][default] value.

Like [other option groups][grouping-options-in-help], you can specify a `name` and
`help` text for the group if you want to set the group apart in the help output.

## Co-Occurring Option Groups

Sometimes you have a set of options that only make sense when specified together. To enforce this,
you can make an option group [`cooccurring`][cooccurring].

Co-occurring groups must have at least one
[`required`][required] option, and may also
have non-required options. The `required` constraint is enforced if any of the options in the group
are given on the command line. If none if the options are given, the value of the group is null.

```kotlin tab="Example"
class UserOptions : OptionGroup() {
    val name by option().required()
    val age by option().int()
}
class Tool : CliktCommand() {
    val userOptions by UserOptions().cooccurring()
    override fun run() {
        userOptions?.let {
            echo(it.name)
            echo(it.age)
        } ?: echo("No user options")
    }
}
```

```text tab="Usage 1"
$ ./tool
No user options
```

```text tab="Usage 2"
$ ./tool --name=jane --age=30
jane
30
```

```text tab="Usage 3"
$ ./tool --age=30
Usage: tool [OPTIONS]

Error: Missing option "--name".
```

Like [other option groups][grouping-options-in-help], you can specify a `name` and
`help` text for the group if you want to set the group apart in the help output.

## Choice and Switch Options With Groups

If you have different groups of options that only make sense when another option has a certain value,
you can use [`groupChoice`][groupChoice] and [`groupSwitch`][groupSwitch].

`groupChoice` options are similar to [`choice` options][choice-options], but instead of mapping a value to
a single new type, they map a value to a [co-occurring `OptionGroup`][co-occurring-option-groups].
Options for groups other than the selected one are ignored, and only the selected group's `required`
constraints are enforced. In the same way, `groupSwitch` options are similar to [`switch`
options][choice-options].

```kotlin tab="Example"
sealed class LoadConfig(name: String): OptionGroup(name)
class FromDisk : LoadConfig("Options for loading from disk") {
    val path by option().file().required()
    val followSymlinks by option().flag()
}

class FromNetwork: LoadConfig("Options for loading from network") {
    val url by option().required()
    val username by option().prompt()
    val password by option().prompt(hideInput = true)
}

class Tool : CliktCommand(help = "An example of a custom help formatter that uses ansi colors") {
    val load by option().groupChoice(
            "disk" to FromDisk(),
            "network" to FromNetwork()
    )

    override fun run() {
        when(val it = load) {
            is FromDisk -> echo("Loading from disk: ${it.path}")
            is FromNetwork -> echo("Loading from network: ${it.url}")
            null -> echo("Not loading")
        }
    }
}
```

```text tab="Usage 1"
$ ./tool --load=disk --path=./config --follow-symlinks
Loading from disk: .\config
```

```text tab="Usage 2"
$ ./tool --load=network --url=www.example.com --username=admin
Password: *******
Loading from network: www.example.com
```

```text tab="Usage 3"
$ ./tool --load=disk
Usage: cli [OPTIONS]

Error: Missing option "--path".
```

```text tab="Usage 4"
$ ./tool --load=whoops
Usage: cli [OPTIONS]

Error: Invalid value for "--load": invalid choice: whoops. (choose from disk, network)
```

## Prompting For Input

In some cases, you might want to create an option that uses the value
given on the command line if there is one, but prompt the user for input
if one is not provided. Clikt can take care of this for you with the [`prompt`][prompt] function.

```kotlin tab="Example"
class Hello : CliktCommand() {
    val name by option().prompt()
    override fun run() {
        echo("Hello $name")
    }
}
```

```text tab="Usage 1"
./hello --name=foo
Hello foo
```

```text tab="Usage 2"
./hello
Name: foo
Hello foo
```

The default prompt string is based on the option name, but
[`prompt`][prompt] takes a number of parameters to customize the output.

## Password Prompts

You can also create a option that uses a hidden prompt and asks for
confirmation. This combination of behavior is commonly used for
passwords.

```kotlin tab="Example"
class Login : CliktCommand() {
    val password by option().prompt(requireConfirmation = true, hideInput = true)
    override fun run() {
        echo("Your hidden password: $password")
    }
}
```

```text tab="Usage"
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
[`versionOption`][versionOption]. Since
the option doesn't have a value, you can't define it using a property delegate. Instead, call the
function on a command directly, either in an `init` block, or on a command instance.

These definitions are equivalent:

```kotlin tab="Version 1"
class Cli : NoOpCliktCommand() {
    init {
        versionOption("1.0")
    }
}
fun main(args: Array<String>) = Cli().main(args)
```

```kotlin tab="Version 2"
class Cli : NoOpCliktCommand()
fun main(args: Array<String>) = Cli().versionOption("1.0").main(args)
```

```text tab="Usage"
$ ./cli --version
cli version 1.0
```

If you want to define your own option with a similar behavior, you can do so by calling
[`eagerOption`][eagerOption]. This function takes an `action` that is called when the option is
encountered on the command line. To print a message and halt execution normally from the callback,
you can throw a [`PrintMessage`][PrintMessage] exception, and
[`CliktCommand.main`][CliktCommand.main] will take care of printing the message. If you want to exit
normally without printing a message, you can throw [`Abort(error=false)`][Abort] instead.

You can define your own version option like this:

```kotlin
class Cli : NoOpCliktCommand() {
    init {
        eagerOption("--version") {
            throw PrintMessage("$commandName version 1.0")
        }
    }
}
```

## Deprecating Options

You can communicate to users that an option is deprecated with
[`option().deprecated()`][deprecated]. By default, this function will add a tag to the option's help
message, and print a warning to stderr if the option is used.

You can customize or omit the warning message and help tags, or change the warning into an error.

```kotlin tab="Example"
class Cli : CliktCommand() {
   val opt by option(help = "option 1").deprecated()
   val opt2 by option(help = "option 2").deprecated("WARNING: --opt2 is deprecated, use --new-opt instead", tagName = null)
   val opt3 by option(help = "option 3").deprecated(tagName = "pending deprecation", tagValue = "use --new-opt instead")
   val opt4 by option(help = "option 4").deprecated(error = true)

   override fun run() = echo("command run")
}
```

```text tab="Usage 1"
$ ./cli --opt=x
WARNING: option --opt is deprecated
command run
```

```text tab="Usage 2"
$ ./cli --opt2=x
WARNING: --op2 is deprecated, use --new-opt instead
command run
```

```text tab="Usage 3"
$ ./cli --opt3=x
WARNING: option --opt3 is deprecated
command run
```

```text tab="Usage 4"
$ ./cli --opt4=x
ERROR: option --opt4 is deprecated
```

```text tab="Help Output"
$ ./cli --help
Usage: cli [OPTIONS]

Options:
  --opt TEXT   option 1 (deprecated)
  --opt2 TEXT  option 2
  --opt3 TEXT  option 3 (pending deprecation: use --new-opt instead)
  --opt4 TEXT  option 4 (deprecated)
```

## Unknown Options

You may want to collect unknown options for manual processing. You can do this by passing
`treatUnknownOptionsAsArgs = true` to your [`CliktCommand` constructor][CliktCommand.init]. This
will cause Clikt to treat unknown options as positional arguments rather than reporting an error
when one is encountered. You'll need to define an [`argument().multiple()`][argument.multiple]
property to collect the options, otherwise an error will still be reported.

```kotlin tab="Example"
class Wrapper : CliktCommand(treatUnknownOptionsAsArgs = true) {
    init { context { allowInterspersedArgs = false } }

    val command by option(help = "?").required()
    val arguments by argument().multiple()

    override fun run() {
        val cmd = (listOf(command) + arguments).joinToString(" ")
        val proc = Runtime.getRuntime().exec(cmd)
        println(proc.inputStream.bufferedReader().readText())
        proc.waitFor()
    }
}
```

```text tab="Usage"
$ ./wrapper --command=git tag --help | head -n4
GIT-TAG(1)                        Git Manual                        GIT-TAG(1)

NAME
       git-tag - Create, list, delete or verify a tag object signed with GPG
```

Note that flag options in a single token (e.g. using `-abc` to specify `-a`, `-b`, and `-c` in a
single token) will still report an error if they are unknown. Each option should be specified
separately in this mode.

You'll often want to set [`allowInterspersedArgs = false`][allowInterspersedArgs] on your Context when
using `treatUnknownOptionsAsArgs`. You may also find that subcommands are a better fit than
`treatUnknownOptionsAsArgs` for your use case.

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

To set the envvar name manually, pass the name to
[`option`][option]:

```kotlin tab="Example"
class Hello : CliktCommand() {
    val name by option(envvar = "MY_NAME")
    override fun run() {
        echo("Hello $name")
    }
}
```

```text tab="Usage 1"
$ export MY_NAME=Foo
$ ./hello
Hello Foo
```

```text tab="Usage 2"
$ export MY_NAME=Foo
$ ./hello --name=Bar
Hello Bar
```

You can enable automatic envvar name inference by setting the `autoEnvvarPrefix` on a command's
[`context`][context]. This will cause all options without
an explicit envvar name to be given an uppercase underscore-separated envvar name. Since the prefix
is set on the [`context`][context], it is propagated to
subcommands. If you have a a subcommand called `foo` with an option `--bar`, and your prefix is
`MY_TOOL`, the option's envvar name will be `MY_TOOL_FOO_BAR`.

```kotlin tab="Example"
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

```text tab="Usage"
$ export HELLO_NAME=Foo
$ ./hello
Hello Foo
```

### Multiple Values from Environment Variables

You might need to allow users to specify multiple values for an option in a single environment
variable. You can do this by creating an option with
[`multiple`][multiple]. The environment
variable's value will be split according a regex, which defaults to split on whitespace for most
types. [`file`][file] will change the pattern
to split according to the operating system's path splitting rules. On Windows, it will split on
semicolons (`;`). On other systems, it will split on colons (`:`). You can also specify a split
pattern by passing it to the `envvarSplit` parameter of `option`.

```kotlin tab="Example"
class Hello : CliktCommand() {
    val names by option(envvar = "NAMES").multiple()
    override fun run() {
        for (name in names) echo("Hello $name")
    }
}
```

```text tab="Usage"
$ export NAMES=Foo Bar
$ ./hello
Hello Foo
Hello Bar
```

## Values from Configuration Files

Clikt also supports reading option values from one or more configuration files (or other sources)
when they aren't present on the command line. For example, when using `git commit`, you can set the
author email with a command line parameter: `git commit --author='Clikt <clikt@example.com>`. But
you can also set it in your git configuration file: `user.email=clikt@example.com`.

Clikt allows you to specify one or more sources of option values that will be read from with the
[`Context.valueSource`][Context.valueSource] builder.

```kotlin tab="Example"
class Hello : CliktCommand() {
    init {
        context { 
            valueSource = PropertiesValueSource.from("myconfig.properties")
        }
    }
    val name by option()
    override fun run() {
        echo("Hello $name")
    }
}
```

```text tab="Usage"
$ echo "name=Foo" > myconfig.properties
$ ./hello
Hello Foo
```

You can also pass multiple sources to [`Context.valueSources`][Context.valueSources], and each
source will be searched for the value in order.

Clikt includes support for reading values [from a map][MapValueSource], and (on JVM) [from Java
Properties files][PropertiesValueSource]. You can add any other file type by implementing
[ValueSource][ValueSource]. See the [JSON sample][json sample] for an implementation that uses
[kotlinx.serialization][serialization] to load values from JSON files.

### Configuration Files and Environment Variables

Every option can read values from both environment variables and configuration files. By default,
Clikt will use the value from an environment variable before the value from a configuration file,
but you can change this by setting [`Context.readEnvvarBeforeValueSource`][readEnvvarFirst] to
`false`.

## Windows and Java-Style Option Prefixes

When specifying option names manually, you can use any prefix (as long
as it's entirely punctuation).

For example, you can make a Windows-style interface with slashes:

```kotlin tab="Example"
class Hello: CliktCommand() {
    val name by option("/name", help="your name")
    override fun run() {
        echo("Hello, $name!")
    }
}
```

```text tab="Usage"
$ ./hello /name Foo
Hello, Foo!
```

Or you can make a Java-style interface that uses single-dashes for long
options:

```kotlin tab="Example"
class Hello: CliktCommand() {
    val name by option("-name", help="your name")
    override fun run() {
        echo("Hello, $name!")
    }
}
```

```text tab="Usage"
$ ./hello -name Foo
Hello, Foo!
```

Note that inferred names will always have a POSIX-style prefix like
`--name`. If you want to use a different prefix, you should specify all
option names manually.

## Option Transformation Order

Clikt has a large number of extension functions that can modify options.
When applying multiple functions to the same option,
there's only one valid order for the functions to be applied.
For example, `option().default(3).int()` will not compile,
because [`default`][default] must be applied after the value type conversion.
Similarly, you can only apply one transform of each type.
So `option().int().float()` is invalid since [`int`][int] and [`float`][float]
both change the value type, as is `option().default("").multiple()`
since [`default`][default] and [`multiple`][multiple] both transform the
call list (if you need a custom default value for `multiple`, you can pass it one as an argument).

Here's an integer option with one of each available transform in a valid order:

```kotlin
val opt: Pair<Int, Int> by option("-o", "--opt")
        .int()
        .restrictTo(1..100)
        .pair()
        .default(1 to 2)
        .validate { require(it.second % 2 == 0) }
```


[option]:                      api/clikt/com.github.ajalt.clikt.parameters.options/option.md
[int]:                         api/clikt/com.github.ajalt.clikt.parameters.types/int.md
[choice]:                      api/clikt/com.github.ajalt.clikt.parameters.types/choice.md
[convert]:                     api/clikt/com.github.ajalt.clikt.parameters.options/convert.md
[wrapValue]:                   api/clikt/com.github.ajalt.clikt.parameters.options/wrap-value.md
[parameter-types]:             parameters.md#parameter-types
[associate]:                   api/clikt/com.github.ajalt.clikt.parameters.options/associate.md
[pair]:                        api/clikt/com.github.ajalt.clikt.parameters.options/pair.md
[triple]:                      api/clikt/com.github.ajalt.clikt.parameters.options/triple.md
[transformValues]:             api/clikt/com.github.ajalt.clikt.parameters.options/transform-values.md
[default]:                     api/clikt/com.github.ajalt.clikt.parameters.options/default.md
[multiple]:                    api/clikt/com.github.ajalt.clikt.parameters.options/multiple.md
[unique]:                      api/clikt/com.github.ajalt.clikt.parameters.options/unique.md
[defaultLazy]:                 api/clikt/com.github.ajalt.clikt.parameters.options/default-lazy.md
[split]:                       api/clikt/com.github.ajalt.clikt.parameters.options/split.md
[flag]:                        api/clikt/com.github.ajalt.clikt.parameters.options/flag.md
[counted]:                     api/clikt/com.github.ajalt.clikt.parameters.options/counted.md
[switch]:                      api/clikt/com.github.ajalt.clikt.parameters.options/switch.md
[splitPair]:                   api/clikt/com.github.ajalt.clikt.parameters.options/split-pair.md
[choice-options]:              #choice-options
[feature-switch-flags]:        #feature-switch-flags
[mutuallyExclusiveOptions]:    api/clikt/com.github.ajalt.clikt.parameters.groups/mutually-exclusive-options.md
[single]:                      api/clikt/com.github.ajalt.clikt.parameters.groups/single.md
[required]:                    api/clikt/com.github.ajalt.clikt.parameters.groups/required.md
[default]:                     api/clikt/com.github.ajalt.clikt.parameters.groups/required.md
[grouping-options-in-help]:    documenting.md#grouping-options-in-help
[cooccurring]:                 api/clikt/com.github.ajalt.clikt.parameters.groups/cooccurring.md
[required]:                    api/clikt/com.github.ajalt.clikt.parameters.options/required.md
[groupChoice]:                 api/clikt/com.github.ajalt.clikt.parameters.groups/group-choice.md
[groupSwitch]:                 api/clikt/com.github.ajalt.clikt.parameters.groups/group-switch.md
[switch-options]:              #feature-switch-flags
[co-occurring-option-groups]:  #co-occurring-option-groups
[prompt]:                      api/clikt/com.github.ajalt.clikt.parameters.options/prompt.md
[versionOption]:               api/clikt/com.github.ajalt.clikt.parameters.options/version-option.md
[eagerOption]:                 api/clikt/com.github.ajalt.clikt.parameters.options/eager-option.md
[CliktCommand.registerOption]: api/clikt/com.github.ajalt.clikt.core/-clikt-command/register-option.md
[PrintMessage]:                api/clikt/com.github.ajalt.clikt.core/-print-message/index.md
[Abort]:                       api/clikt/com.github.ajalt.clikt.core/-abort/index.md
[CliktCommand.main]:           api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.md
[deprecated]:                  api/clikt/com.github.ajalt.clikt.parameters.options/deprecated.md
[context]:                     api/clikt/com.github.ajalt.clikt.core/context.md
[file]:                        api/clikt/com.github.ajalt.clikt.parameters.types/file.md
[float]:                       api/clikt/com.github.ajalt.clikt.parameters.types/float.md
[Context.valueSource]:         api/clikt/com.github.ajalt.clikt.core/-context/-builder/value-source.md
[Context.valueSources]:        api/clikt/com.github.ajalt.clikt.core/-context/-builder/value-sources.md
[ValueSource]:                 api/clikt/com.github.ajalt.clikt.sources/-value-source/index.md
[json sample]:                 https://github.com/ajalt/clikt/tree/master/samples/json
[serialization]:               https://github.com/Kotlin/kotlinx.serialization
[readEnvvarFirst]:             api/clikt/com.github.ajalt.clikt.core/-context/-builder/read-envvar-before-value-source.md
[PropertiesValueSource]:       api/clikt/com.github.ajalt.clikt.sources/-properties-value-source/index.md
[MapValueSource]:              api/clikt/com.github.ajalt.clikt.sources/-map-value-source/index.md
[CliktCommand.init]:           api/clikt/com.github.ajalt.clikt.core/-clikt-command/-init-/
[argument.multiple]:           api/clikt/com.github.ajalt.clikt.parameters.arguments/multiple/
[allowInterspersedArgs]: api/clikt/com.github.ajalt.clikt.core/-context/allow-interspersed-args.md
