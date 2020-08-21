# Exception Handling

Clikt uses exceptions internally to signal that processing has ended
early for any reason. This includes incorrect command line usage, or
printing a help page.

## Where are Exceptions Handled?

When you call [`CliktCommand.main`][main], it will parse the command line and catch any
[`CliktError`][CliktError] and [`Abort`][Abort] exceptions. If it catches one, it will then print
out the appropriate information and exit the process. If the caught exception is a
[`PrintMessage`][PrintMessage] or [`PrintHelpMessage`][PrintHelpMessage], the process exit status
will be 0 and the message will be printed to stdout. Otherwise it will exit with status 1 and print
the message to stderr.

Any other types of exceptions indicate a programming error, and are not caught by [`main`][main].
However, [`convert`][convert] and the other parameter transformations will wrap exceptions thrown
inside them in a [`UsageError`][UsageError], so if you define a custom transformation,
you don't have to worry about an exception escaping to the user.

## Handling Exceptions Manually

[`CliktCommand.main`][main] is just a `try`/`catch` block surrounding
[`CliktCommand.parse`][parse], so if don't want exceptions to be caught,
you can call [`parse`][parse] wherever you would normally call [`main`][main].

```kotlin
fun main(args: Array<String>) = Cli().parse(args)
```

## Which Exceptions Exist?

Clikt will throw [`Abort`][Abort] if it needs to halt execution immediately without a specific
message. All other exceptions are subclasses of [`UsageError`][UsageError].

The following subclasses exist:

* [`PrintMessage`][PrintMessage] : The exception's message should be printed.
* [`PrintHelpMessage`][PrintHelpMessage] : The help page for the exception's command should be printed.
* [`PrintCompletionMessage`][PrintCompletionMessage] : Shell completion code for the command should be printed.
* [`UsageError`][UsageError] : The command line was incorrect in some way. All other exceptions subclass from this. These exceptions are automatically augmented with extra information about the current parameter, if possible.
* [`ProgramResult`][ProgramResult] : The program should exit with the `statusCode` from this exception.
* [`BadParameterValue`][BadParameterValue] : A parameter was given the correct number of values, but of invalid format or type.
* [`MissingOption`][MissingOption] and [`MissingArgument`][MissingArgument]: A required parameter was not provided.
* [`NoSuchOption`][NoSuchOption] : An option was provided that does not exist.
* [`NoSuchSubcommand`][NoSuchSubcommand] : A subcommand was called that does not exist.
* [`IncorrectOptionValueCount`][IncorrectOptionValueCount] : An option was supplied but the number of values supplied to the option was incorrect.
* [`IncorrectArgumentValueCount`][IncorrectArgumentValueCount] : An argument was supplied but the number of values supplied was incorrect.
* [`MutuallyExclusiveGroupException`][MutuallyExclusiveGroupException] : Multiple options in a mutually exclusive group were supplied when the group is restricted to a single value.
* [`FileNotFound`][FileNotFound] : A required configuration file or @-file was not found.
* [`InvalidFileFormat`][InvalidFileFormat] : A configuration file or @-file failed to parse correctly.


[Abort]:                           api/clikt/com.github.ajalt.clikt.core/-abort/index.md
[BadParameterValue]:               api/clikt/com.github.ajalt.clikt.core/-bad-parameter-value/index.md
[CliktError]:                      api/clikt/com.github.ajalt.clikt.core/-clikt-error/index.md
[convert]:                         api/clikt/com.github.ajalt.clikt.parameters.options/convert.md
[FileFormatError]:                 api/clikt/com.github.ajalt.clikt.core/-file-not-found/index.md
[FileNotFound]:                    api/clikt/com.github.ajalt.clikt.core/-file-not-found/index.md
[IncorrectArgumentValueCount]:     api/clikt/com.github.ajalt.clikt.core/-incorrect-argument-value-count/index.md
[IncorrectOptionValueCount]:       api/clikt/com.github.ajalt.clikt.core/-incorrect-option-value-count/index.md
[InvalidFileFormat]:               api/clikt/com.github.ajalt.clikt.core/-invalid-file-format/index.md
[main]:                            api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.md
[MissingArgument]:                 api/clikt/com.github.ajalt.clikt.core/-missing-argument/index.md
[MissingOption]:                   api/clikt/com.github.ajalt.clikt.core/-missing-option/index.md
[MutuallyExclusiveGroupException]: api/clikt/com.github.ajalt.clikt.core/-mutually-exclusive-group-exception/index.md
[NoSuchOption]:                    api/clikt/com.github.ajalt.clikt.core/-no-such-option/index.md
[NoSuchSubcommand]:                api/clikt/com.github.ajalt.clikt.core/-no-such-subcommand/index.md
[parse]:                           api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.md
[PrintCompletionMessage]:          api/clikt/com.github.ajalt.clikt.core/-print-completion-message/index.md
[PrintHelpMessage]:                api/clikt/com.github.ajalt.clikt.core/-print-help-message/index.md
[PrintMessage]:                    api/clikt/com.github.ajalt.clikt.core/-print-message/index.md
[ProgramResult]:                   api/clikt/com.github.ajalt.clikt.core/-program-result/index.md
[UsageError]:                      api/clikt/com.github.ajalt.clikt.core/-usage-error/index.md
