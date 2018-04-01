---
title: Exception Handling
sidebar: home_sidebar
permalink: exceptions.html
---

<!--  TODO: add docs links -->

Clikt uses exceptions internally to signal that processing has ended
early for any reason. This includes incorrect command line usage, or
printing a help page.

## Where are Exceptions Handled?

When you call `CliktCommand.main()`, it will parse the command line and
catch any `CliktError` and `Abort` exceptions. If it catches one, it
will then print out the appropriate information to stdout, and exit the
process. If the caught exception is a `PrintMessage` or
`PrintHelpMessage`, the process exit status will be 0. Otherwise it will
exist with status 1.

Any other types of exceptions indicate a programming error, and are not
caught by `main()`. However, `convert()` and the other parameter
transformations will wrap exceptions thrown inside them in a
`UsageError`, so if you define a custom transformation, you don't have
to worry about an exception escaping to the user.

## Handling Exceptions Manually

`CliktCommand.main` is just a `try`/`catch` block surrounding
`Clikt.parse()`, so if don't want exceptions to be caught, you can call
`parse()` wherever you would normally call `main()`

```kotlin
fun main(args: Array<String>) = Cli().parse(args)
```

## Which Exceptions Exist?

Clikt will throw `Abort` if it needs to halt execution immediately
without a specific message. All other exceptions are subclasses of
`CliktError`.

The following subclasses exist:

* `PrintMessage`: The exception's message should be printed.
* `PrintHelpMessage`: The help page for the exception's command should be printed.
* `UsageError`: The command line was incorrect in some way. All other exceptions subclass from this. These exceptions are automatically augmented with extra information about the current parameter, if possible.
* `BadParameterValue`: A parameter was given the correct number of values, but of invalid format or type.
* `MissingParameter`: A required parameter was not provided.
* `NoSuchOption`: An option was provided that does not exist.
* `IncorrectOptionValueCount`: An option was supplied but the number of values supplied to the option was incorrect.
* `IncorrectArgumentValueCount`: An argument was supplied but the number of values supplied was incorrect.


{% include links.html %}
