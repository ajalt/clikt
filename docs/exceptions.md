# Exception Handling

Clikt uses exceptions internally to signal that processing has ended
early for any reason. This includes incorrect command line usage, or
printing a help page.

## Where are Exceptions Handled? {#handling}

When you call [`CliktCommand.main`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html), it will parse the command line and catch any [`CliktError`](api/clikt/com.github.ajalt.clikt.core/-clikt-error/index.html) and [`Abort`](api/clikt/com.github.ajalt.clikt.core/-abort/index.html) exceptions. If it catches one, it will then
print out the appropriate information and exit the process. If the
caught exception is a [`PrintMessage`](api/clikt/com.github.ajalt.clikt.core/-print-message/index.html) or [`PrintHelpMessage`](api/clikt/com.github.ajalt.clikt.core/-print-help-message/index.html), the process exit status will be 0 and the
message will be printed to stdout. Otherwise it will exit with status 1
and print the message to stderr.

Any other types of exceptions indicate a programming error, and are not
caught by [`main`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html). However, [`convert`](api/clikt/com.github.ajalt.clikt.parameters.options/convert.html) and the other parameter
transformations will wrap exceptions thrown inside them in a [`UsageError`](api/clikt/com.github.ajalt.clikt.core/-usage-error/index.html), so if you define a custom
transformation, you don't have to worry about an exception escaping to
the user.

## Handling Exceptions Manually

[`CliktCommand.main`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html) is
just a `try`/`catch` block surrounding [`CliktCommand.parse`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.html), so if don't want exceptions to be
caught, you can call [`parse`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.html) wherever you would
normally call [`main`](api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html).

```kotlin
fun main(args: Array<String>) = Cli().parse(args)
```

## Which Exceptions Exist?

Clikt will throw [`Abort`](api/clikt/com.github.ajalt.clikt.core/-abort/index.html) if
it needs to halt execution immediately without a specific message. All
other exceptions are subclasses of [`UsageError`](api/clikt/com.github.ajalt.clikt.core/-usage-error/index.html).

The following subclasses exist:

* [`PrintMessage`](api/clikt/com.github.ajalt.clikt.core/-print-message/index.html) : The exception's message should be printed.
* [`PrintHelpMessage`](api/clikt/com.github.ajalt.clikt.core/-print-help-message/index.html) : The help page for the exception's command should be printed.
* [`UsageError`](api/clikt/com.github.ajalt.clikt.core/-usage-error/index.html) : The command line was incorrect in some way. All other exceptions subclass from this. These exceptions are automatically augmented with extra information about the current parameter, if possible.
* [`BadParameterValue`](api/clikt/com.github.ajalt.clikt.core/-bad-parameter-value/index.html) : A parameter was given the correct number of values, but of invalid format or type.
* [`MissingParameter`](api/clikt/com.github.ajalt.clikt.core/-missing-parameter/index.html) : A required parameter was not provided.
* [`NoSuchOption`](api/clikt/com.github.ajalt.clikt.core/-no-such-option/index.html) : An option was provided that does not exist.
* [`IncorrectOptionValueCount`](api/clikt/com.github.ajalt.clikt.core/-incorrect-option-value-count/index.html) : An option was supplied but the number of values supplied to the option was incorrect.
* [`IncorrectArgumentValueCount`](api/clikt/com.github.ajalt.clikt.core/-incorrect-argument-value-count/index.html) : An argument was supplied but the number of values supplied was incorrect.
