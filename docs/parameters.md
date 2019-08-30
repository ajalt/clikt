# Parameters

Clikt supports two types of parameters: options and positional
arguments. If you're following Unix conventions with your interface, you
should use options for most parameters. Options are usually optional,
and arguments are frequently required.

## Differences

Arguments have the advantage of being able to accept a variable number
of values, while Options are limited to a fixed number of values. Other
than that restriction, options have more capabilities than arguments.

Options can:

* Act as flags (options don't have to take values)
* Prompt for missing input
* Load values from environment variables

In general, arguments are usually used for values like file paths or
URLs, or for required values, and options are used for everything else.

## Parameter Names

Both options and arguments can infer their names (or the metavar in the
case of arguments) from the name of the property. You can also specify
the names manually. Options can have any number of names, where
arguments only have a single metavar.

```kotlin tab="Example"
class Cli : CliktCommand() {
    val inferredOpt by option()
    val inferred by argument()
    val explicitOpt by option("-e", "--explicit")
    val explicitArg by argument("<explicit>")
    override fun run() = Unit
}
```

```text tab="Help Output"
Usage: cli [OPTIONS] INFERRED <explicit>

Options:
  --inferred-opt TEXT
  -e, --explicit TEXT
  -h, --help           Show this message and exit
```

## Parameter Types

Both options and arguments can convert the String that the user inputs
to other types.

Types work by transforming the return value of the property delegate. By
default parameters have a string type:

```kotlin
val opt: String? by option(help="an option")
val arg: String by argument(help="an argument")
```

To convert the input to an integer, for example, use the `int()`
extension function:

```kotlin
val opt: Int? by option(help="an option").int()
val arg: Int by argument(help="an argument").int()
```

## Built-In Types

There are a number of built in types that can be applied to options and
arguments.

* `Int`: [`option().int()` and `argument().int()`][int]
* `Long`: [`option().long()` and `argument().long()`][long]

By default, any value that fits in the integer type is accepted.
You can restrict the values to a range with [`restrictTo()`][restrictTo],
which allows you to either clamp the input to the range,
or fail with an error if the input is outside the range.

* `Float`: [`option().float()` and `argument().float()`][float]
* `Double`: [`option().double()` and `argument().double()`][double]

As with integers, you can restrict the input to a range with [`restrictTo()`][restrictTo].

* [`option().choice()` and `argument().choice()`][choice]

You can restrict the values to a set of values, and optionally map the
input to a new value. For example, to create an option that only
accepts the value "A" or "B":

 ```kotlin
  val opt: String? by option().choice("a", "b")
 ```

You can also convert the restricted set of values to a new type:

 ```kotlin
  val color: Int by argument().choice("red" to 1, "green" to 2)
 ```
* `Enum`: [`option().enum()` and `argument().enum()`][enum]

Like `choice`, but uses the values of an enum type.

```kotlin
enum class Color { RED, GREEN }
val color: Color by option().enum<Color>()
```

* `File`: [`option().file()` and `argument().file()`][file]
* `Path`: [`option().path()` and `argument().path()`][path]

  These conversion functions take extra parameters that allow you to
  require that values are file paths that have certain attributes, such
  as that they are directories, or they are writable files.

## Custom Types

You can convert parameter values to a custom type by using
[`argument().convert()`][convert] and [`option().convert()`][convert].
These functions take a lambda that converts the input `String` to any type.
If the parameter takes multiple values, or an option appears multiple times in `argv`,
the conversion lambda is called once for each value.

Any errors that are thrown from the lambda are automatically caught and
a usage message is printed to the user. If you need to trigger
conversion failure, you can use `fail("error message")` instead of
raising an exception.

For example, you can create an option of type `BigDecimal` like this:

```kotlin tab="Example"
class Cli: CliktCommand() {
    val opt by option().convert { it.toBigDecimal() }
    override fun run() = echo("opt=$opt")
}
```

```text tab="Usage 1"
$ ./cli --opt=1.5
opt=1.5
```

```text tab="Usage 2"
$ ./cli --opt=foo
Usage: cli [OPTIONS]

Error: Invalid value for "--opt": For input string: "foo"
```

You can also pass [`option().convert()`][convert] a metavar
that will be printed in the help page instead of the default of `VALUE`.
We can modify the above example to use a metavar and an explicit error message:

```kotlin tab="Example"
class Cli: CliktCommand() {
    val opt by option(help="a real number").convert("FLOAT") {
        it.toBigDecimalOrNull() ?: fail("A real number is required")
    }
    override fun run() = echo("opt=$opt")
}
```

```text tab="Usage 1"
$ ./cli --opt=foo
Usage: cli [OPTIONS]

Error: Invalid value for "--opt": A real number is required
```

```text tab="Usage 2"
$ ./cli --help
Usage: cli [OPTIONS]

Options:
  --opt FLOAT  a real number
  -h, --help   Show this message and exit
```

## Parameter Validation

After converting a value to a new type, you can perform additional validation on the converted value
with [`option().validate()`][validate] and [`argument().validate()`][validate].
`validate` takes a lambda that returns nothing, but can call `fail("error message")` if the value is
invalid. You can also call `require()`, which will fail if the provided expression is false. The
lambda is only called if the value is non-null.

```kotlin
val opt by option().int().validate {
    require(it % 2 == 0) { "value must be even" }
}
```

The lambdas you pass to `validate` are called after the values for all options and arguments have
been set, so (unlike in transforms) you can reference other parameters:

```kotlin tab="Example"
class Tool : CliktCommand() {
    val number by option().int().default(0)
    val biggerNumber by option().int().validate {
        require(it > number) {
            "--bigger-number must be bigger than --number"
        }
    }

    override fun run() {
        echo("number=$number, biggerNumber=$biggerNumber")
    }
}
```

```text tab="Usage 1"
$ ./tool --number=1
number=1, biggerNumber=null
```

```text tab="Usage 2"
$ ./tool --number=1 --bigger-number=0
Usage: tool [OPTIONS]

Error: --bigger-number must be bigger than --number
```


[int]:        api/clikt/com.github.ajalt.clikt.parameters.types/int.md
[long]:       api/clikt/com.github.ajalt.clikt.parameters.types/long.md
[restrictTo]: api/clikt/com.github.ajalt.clikt.parameters.types/restrict-to.md
[float]:      api/clikt/com.github.ajalt.clikt.parameters.types/float.md
[double]:     api/clikt/com.github.ajalt.clikt.parameters.types/double.md
[choice]:     api/clikt/com.github.ajalt.clikt.parameters.types/choice.md
[file]:       api/clikt/com.github.ajalt.clikt.parameters.types/file.md
[path]:       api/clikt/com.github.ajalt.clikt.parameters.types/path.md
[enum]:       api/clikt/com.github.ajalt.clikt.parameters.types/enum.md
[convert]:    api/clikt/com.github.ajalt.clikt.parameters.options/convert.md
[validate]:   api/clikt/com.github.ajalt.clikt.parameters.options/validate.md
