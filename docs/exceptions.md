# Exception Handling

Clikt uses exceptions internally to signal that processing has ended
early for any reason. This includes incorrect command line usage, or
printing a help page.

## Where are Exceptions Handled?

When you call [`CliktCommand.main`][main], it will [parse][parse] the command line and catch any
[`CliktError`][CliktError] exceptions. If it catches one, it will then print out the error's message
and exit the process with the error's `statusCode`. The message is printed to stderr or stdout
depending on the error's `printError` property.

For exceptions raised by Clikt, [`PrintMessage`][PrintMessage] and
[`PrintHelpMessage`][PrintHelpMessage] have a `statusCode` of 0 and print to stdout. Other
exceptions have a `statusCode` of 1 and print to stderr.

Any other types of exceptions indicate a programming error, and are not caught by [`main`][main].
However, [`convert`][convert] and the other parameter transformations will wrap exceptions thrown
inside them in a [`UsageError`][UsageError], so if you define a custom transformation,
you don't have to worry about an exception escaping to the user.

## Handling Exceptions Manually

[`CliktCommand.main`][main] is just a `try`/`catch` block surrounding
[`CliktCommand.parse`][parse], so if you don't want exceptions to be caught,
you can call [`parse`][parse] wherever you would normally call [`main`][main].

!!! tip

    You can use [echoFormattedHelp][echoFormattedHelp] to print the help or error message to any
    exception, or [getFormattedHelp][getFormattedHelp] to get the help message as a string.

```kotlin
fun main(args: Array<String>) {
    val cli = Cli()
    try {
        cli.parse(args)
    } catch (e: CliktError) {
        cli.echoFormattedHelp(e)
        exitProcess(e.statusCode)
    }
}
```

## Which Exceptions Exist?

All exceptions thrown by Clikt are subclasses of [`CliktError`][CliktError].

The following subclasses exist:

* [`Abort`][Abort] : The command should exit immediately with the given `statusCode` without printing any messages.
* [`PrintMessage`][PrintMessage] : The exception's message should be printed.
* [`PrintHelpMessage`][PrintHelpMessage] : The help page for the exception's command should be printed.
* [`PrintCompletionMessage`][PrintCompletionMessage] : Shell completion code for the command should be printed.
* [`UsageError`][UsageError] : The command line was incorrect in some way. All the following exceptions subclass from this. These exceptions are automatically augmented with extra information about the current parameter, if possible.
* [`MultiUsageError`][MultiUsageError] : Multiple [`UsageError`][UsageError]s occurred. The `errors` property contains the list of the errors.
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


[Abort]:                           api/clikt/com.github.ajalt.clikt.core/-abort/index.html
[BadParameterValue]:               api/clikt/com.github.ajalt.clikt.core/-bad-parameter-value/index.html
[CliktError]:                      api/clikt/com.github.ajalt.clikt.core/-clikt-error/index.html
[convert]:                         api/clikt/com.github.ajalt.clikt.parameters.options/convert.html
[echoFormattedHelp]:               api/clikt/com.github.ajalt.clikt.core/-clikt-command/echo-formatted-help.html
[FileNotFound]:                    api/clikt/com.github.ajalt.clikt.core/-file-not-found/index.html
[getFormattedHelp]:                api/clikt/com.github.ajalt.clikt.core/-clikt-command/get-formatted-help.html
[IncorrectArgumentValueCount]:     api/clikt/com.github.ajalt.clikt.core/-incorrect-argument-value-count/index.html
[IncorrectOptionValueCount]:       api/clikt/com.github.ajalt.clikt.core/-incorrect-option-value-count/index.html
[InvalidFileFormat]:               api/clikt/com.github.ajalt.clikt.core/-invalid-file-format/index.html
[main]:                            api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html
[MissingArgument]:                 api/clikt/com.github.ajalt.clikt.core/-missing-argument/index.html
[MissingOption]:                   api/clikt/com.github.ajalt.clikt.core/-missing-option/index.html
[MultiUsageError]:                 api/clikt/com.github.ajalt.clikt.core/-multi-usage-error/index.html
[MutuallyExclusiveGroupException]: api/clikt/com.github.ajalt.clikt.core/-mutually-exclusive-group-exception/index.html
[NoSuchOption]:                    api/clikt/com.github.ajalt.clikt.core/-no-such-option/index.html
[NoSuchSubcommand]:                api/clikt/com.github.ajalt.clikt.core/-no-such-subcommand/index.html
[parse]:                           api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.html
[PrintCompletionMessage]:          api/clikt/com.github.ajalt.clikt.core/-print-completion-message/index.html
[PrintHelpMessage]:                api/clikt/com.github.ajalt.clikt.core/-print-help-message/index.html
[PrintMessage]:                    api/clikt/com.github.ajalt.clikt.core/-print-message/index.html
[ProgramResult]:                   api/clikt/com.github.ajalt.clikt.core/-program-result/index.html
[UsageError]:                      api/clikt/com.github.ajalt.clikt.core/-usage-error/index.html
