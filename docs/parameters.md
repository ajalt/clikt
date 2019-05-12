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

```kotlin
class Cli : CliktCommand() {
    val inferredOpt by option()
    val inferred by argument()
    val explicitOpt by option("-e", "--explicit")
    val explicitArg by argument("<explicit>")
    override fun run() = Unit
}
```

The above class will generate the following help page:

```
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

```kotiln
val opt: Int? by option(help="an option").int()
val arg: Int by argument(help="an argument").int()
```

## Built-In Types

There are a number of built in types that can be applied to options and
arguments.

* `Int`: [`option().int()` and `argument().int()`](api/clikt/com.github.ajalt.clikt.parameters.types/int.html)
* `Long`: [`option().long()` and `argument().long()`](api/clikt/com.github.ajalt.clikt.parameters.types/long.html)

  By default, any value that fits in the integer type is accepted. You
  can restrict the values to a range with [`restrictTo()`](api/clikt/com.github.ajalt.clikt.parameters.types/restrict-to.html), which
  allows you to either clamp the input to the range, or fail with an
  error if the input is outside the range.

* `Float`: [`option().float()` and `argument().float()`](api/clikt/com.github.ajalt.clikt.parameters.types/float.html)
* `Double`: [`option().double()` and `argument().double()`](api/clikt/com.github.ajalt.clikt.parameters.types/double.html)

  As with integers, you can restrict the input to a range with [`restrictTo()`](api/clikt/com.github.ajalt.clikt.parameters.types/restrict-to.html).

* [`option().choice()` and `argument().choice()`](api/clikt/com.github.ajalt.clikt.parameters.types/choice.html)

  You can restrict the values to a set of values, and optionally map the
  input to a new value. For example, to create an option that only
  accepts the value "A" or "B":

 ```kotlin
  val opt: String? by option().choice("A", "B")
 ```

  To create an argument that requires the user to choose from the values
  of an enum:

 ```kotlin
  enum class Color { RED, GREEN }
  val color: Color by argument().choice("RED" to Color.RED, "GREEN" to Color.GREEN)
 ```

* `File`: [`option().file()` and `argument().file()`](api/clikt/com.github.ajalt.clikt.parameters.types/file.html)
* `Path`: [`option().path()` and `argument().path()`](api/clikt/com.github.ajalt.clikt.parameters.types/path.html)

  These conversion functions take extra parameters that allow you to
  require that values are file paths that have certain attributes, such
  as that they are directories, or they are writable files.

## Custom Types

You can convert parameter values to a custom type by using
[`argument().convert()`](api/clikt/com.github.ajalt.clikt.parameters.arguments/convert.html) and
[`option().convert()`](api/clikt/com.github.ajalt.clikt.parameters.options/convert.html). These
functions take a lambda that converts the input `String` to any type. If the parameter takes
multiple values, or an option appears multiple times in `argv`, the conversion lambda is called once
for each value.

Any errors that are thrown from the lambda are automatically caught and
a usage message is printed to the user. If you need to trigger
conversion failure, you can use `fail("error message")` instead of
raising an exception.

For example, you can create an option of type `BigDecimal` like this:

```kotlin
class Cli: CliktCommand() {
    val opt by option().convert { it.toBigDecimal() }
    override fun run() = echo("opt=$opt")
}
```

Calling it looks like:

```
$ ./cli --opt=1.5
opt=1.5

$ ./cli --opt=foo
Usage: cli [OPTIONS]

Error: Invalid value for "--opt": For input string: "foo"
```

You can also pass
[`option().convert()`](api/clikt/com.github.ajalt.clikt.parameters.options/convert.html) a metavar
that will be printed in the help page instead of the default of `VALUE`. We can modify the above
example to use a metavar and an explicit error message:

```kotlin
class Cli: CliktCommand() {
    val opt by option(help="a real number").convert("FLOAT") {
        it.toBigDecimalOrNull() ?: fail("A real number is required")
    }
    override fun run() = echo("opt=$opt")
}
```

Which works like this:

```
$ ./cli --opt=foo
Usage: cli [OPTIONS]

Error: Invalid value for "--opt": A real number is required

$ ./cli --help
Usage: cli [OPTIONS]

Options:
  --opt FLOAT  a real number
  -h, --help   Show this message and exit
```

## Parameter Validation

After converting a value to a new type, you can perform additional validation on the converted value
with [`option().validate()`](api/clikt/com.github.ajalt.clikt.parameters.options/validate.html) and
[`argument().validate()`](api/clikt/com.github.ajalt.clikt.parameters.arguments/validate.html).
`validate` takes a lambda that returns nothing, but can call `fail("error message")` if the value is
invalid. You can also call `require()`, which will fail if the provided expression is false. The
lambda is only called if the value is non-null.

```kotlin
val opt by option().int().validate {
    require(it % 2 == 0) { "value must be even" }
}
```
