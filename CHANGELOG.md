# Changelog

## [Unreleased]
### Added
- Ability to use custom program exit status codes via `ProgramResult`.
- `inputStream` and `outputStream` conversions for options and arguments. ([#157](https://github.com/ajalt/clikt/issues/157) and [#159](https://github.com/ajalt/clikt/issues/159))
- `splitPair`, `toMap`, and `associate` extensions on `option`. ([#166](https://github.com/ajalt/clikt/issues/166))
- `treatUnknownOptionsAsArgs` parameter to `CliktCommand`. ([#152](https://github.com/ajalt/clikt/issues/152))
- `defaultByName` function for `groupChoice` and `groupSwitch` options. ([#171](https://github.com/ajalt/clikt/issues/171))

### Changed
- Update Kotlin to 1.3.71
- Improved command name inference. Now, a class like `MyAppCommand` will infer its `commandName` as `my-app` rather than `myappcommand`. You can still specify the name manually as before. ([#168][https://github.com/ajalt/clikt/pull/168])

### Fixed
- Correctly parse short options with attached values that contain `=`

## [2.6.0] - 2020-03-15
### Added
- `registeredSubcommands`, `registeredOptions`, `registeredArguments`, and `registeredParameterGroups` methods on `CliktCommand`.
- Ability to [read default option values](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.sources/-value-source/index.md) from configuration files and other sources. Support for Java property files is built in on JVM, see the `json` sample for an example of reading from other formats.
- `allowMultipleSubcommands` parameter to `CliktCommand` that allows you to pass multiple subcommands in the same call. ([docs](docs/commands.md#chaining-and-repeating-subcommands))
- Errors from typos in subcommand names will now include suggested corrections. Corrections for options and subcommands are now based on a Jaro-Winkler similarity metric, and can be customized with `Context.correctionSuggestor`

### Changed
- Update Kotlin to 1.3.70
- `convert` can be called more than once on the same option or argument, including after calls to conversion functions like `int` and `file`.
- `wrapValue` is now deprecated, since `convert` can be used in its place instead.
- `CliktCommand.toString` now includes the class name
- Reverted automatic `~` expansion in `file()` and `path()` introduced in 2.5.0. If you need this behavior, you can implement it with code like `convert { /* expand tidle */ }.file()` 

## [2.5.0] - 2020-02-22
### Added
- Clikt is now available as a Kotlin Multiplatform Project, supporting JVM, NodeJS, and native Windows, Linux, and macOS.
- `eagerOption {}` function to more easily register eager options.
- Eager options can now be added to option groups in help out by passing a value for `groupName` when creating them. 
- `canBeSymlink` parameter to `file()` and `path()` conversions that can be used to disallow symlinks
- `CliktCommand.eagerOption` to simplify creating custom eager options

### Changed
- `NoRunCliktCommand` was renamed to `NoOpCliktCommand`. The existing class is deprecated. ([#130](https://github.com/ajalt/clikt/issues/130))
- The `CliktCommand.context` property has been deprecated in favor of the new name, `currentContext`, to avoid confusion with the `CliktCommand.context{}` method.
- The parameter names of `file()` and `path()` conversions have changed. The existing names are deprecated, and can be converted to the new usages with an IntelliJ inspection. Note that if you are calling these functions with unnamed arguments (e.g. `file(true, false)`), you'll need to add argument names in order to remove the deprecation warning.

### Fixed
- `file()` and `path()` conversions will now properly expand leading `~` in paths to the home directory for `mustExist`, `canBeFile`, and `canBeDir` checks. The property value is unchanged, and can still begin with a `~`. ([#131](https://github.com/ajalt/clikt/issues/79))

## [2.4.0] - 2020-01-25
### Added
- `CompletionCandidates.Fixed` now has a secondary convenience constructor that take a `vararg` of `String`s
- `CompletionCadidates.Custom`, which allows you to call other binaries or write a script to generate completions. This class is currently experimental. ([#79](https://github.com/ajalt/clikt/issues/79))
- `Option.wrapValue` and `Argument.wrapValue` to make it easier to reuse existing conversion functions.
- `ignoreCase` parameter to `choice()` and `enum()` conversion functions.

### Changed
- `option()` and `argument()` now take optional `completionCandidates` parameters to override how completion is generated. The constructor and `copy` functions of `OptionsWithValues` and `ProcessedArgument` have changed to support default values.
- The overloads of `findObject` ([1](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.core/-context/find-object/) [2](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.core/find-object/)) that take a default value have been renamed `findOrSetObject`. The existing names are marked with `@Deprecated`, and IntelliJ can convert your callsites automatically. ([#110](https://github.com/ajalt/clikt/issues/110))
- `enum()` parameters now accept case-insensitive values by default. You change this behavior by passing `ignoreCase = false` to `enum()` ([#115](https://github.com/ajalt/clikt/issues/115))

### Fixed
- `groupChoice` help output now includes the choices in the help output metavar
- `TermUi.edit*` functions could freeze on certain editors ([#99](https://github.com/ajalt/clikt/issues/99), thanks @iampravikant and @sebokopter)
- Shell completion can now handle command names with dashes. ([#104](https://github.com/ajalt/clikt/issues/104))
- Arguments with `=` in them could be incorrectly interpreted as options ([#106](https://github.com/ajalt/clikt/issues/106))

## [2.3.0] - 2019-11-07
### Added
- `option().groupSwitch()`, which works like `groupChoice()`, but uses a `switch()` option rather than a `choice()` option.
- `UsageError` now has a `statusCode` parameter (which defaults to 1). If you're using `ClicktCommand.main`, the value of `statusCode` will be passed to `exitProcess`. 

### Changed
- Shell completion code is now printed by throwing a `PrintCompletionMessage` (a subclass of `PrintMessage`) rather than calling `echo` directly.

## [2.2.0] - 2019-09-25
### Added
- Added [`enum()` conversion](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.types/enum/) for options and arguments. ([#84](https://github.com/ajalt/clikt/issues/84))

### Changed
- There are now several ways of [preventing @-file expansion](https://ajalt.github.io/clikt/advanced/#preventing-file-expansion)

### Fixed
- Help output missing items when no help text is specified. ([#85](https://github.com/ajalt/clikt/issues/85))
- Help output not grouping options in groups passed to `groupChoice`. ([#88](https://github.com/ajalt/clikt/issues/88))

## [2.1.0] - 2019-05-23
### Added
- Ability to prevent [rewrapping individual paragraphs](https://ajalt.github.io/clikt/documenting/#preventing-rewrapping) in help output.
- Added parameter `required` to `Option.multiple()` to require at least one instance of the option on the command line.

### Changed
- `CliktCommand.toString()` now includes the names and values of all parameters and subcommands.

### Fixed
- Create subcommand context when `helpOptionNames` is empty. ([#64](https://github.com/ajalt/clikt/issues/64))

## [2.0.0] - 2019-05-12
### Added
- [Bash autocomplete script generation](https://ajalt.github.io/clikt/autocomplete/). A property named `completionCandidates` has been added to `Argument` and `Option` interfaces, and corresponding parameters have been added to the various implementation constructors, as well as the `convert` functions. You can use this to control the values autocomplete that will be suggested.
- [`option().split()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/split/), and the corresponding [`OptionWithValues.valueSplit`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/-option-with-values/value-split/).
- Marking options as deprecated with [`option().deprecated()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/deprecated/)
- You can manually set the pattern to split envvars on by passing a pattern to the `envvarSplit` parameter of [`option()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/option/)
- [Option groups](https://ajalt.github.io/clikt/documenting/#grouping-options-in-help), [mutually exclusive groups](https://ajalt.github.io/clikt/options/#prompting-for-input), [co-occurring groups](https://ajalt.github.io/clikt/options/#co-occurring-option-groups), and [choice options with groups](https://ajalt.github.io/clikt/options/#choice-options-with-groups)
- Support for [Command line argument files](https://ajalt.github.io/clikt/advanced/#command-line-argument-files-files) a.k.a "@-files"

### Changed
- If multiple `--` tokens are present on the command line, all subsequent occurrences after the first are now parsed as positional arguments. Previously, subsequent `--` tokens were skipped.  
- The `PlaintextHelpFormatter` has been replaced with `CliktHelpFormatter`, which is more customizable. See [the docs](https://ajalt.github.io/clikt/documenting/) for more info, or the [new sample](samples/ansicolors/README.md) for an example of customizing help output to use ANSI colors.
- Some of the properties and constructor parameters for [`OptionWithValues`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/-option-with-values/) and [`ProcessedArgument`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.arguments/-processed-argument/) have changed.
- The `OptionDelegate` interface has changed, and `GroupableOption` and `ParameterHolder` interfaces have been added to work with option groups.
- [Parameter validation](https://ajalt.github.io/clikt/parameters/#parameter-validation) now occurs after all parameter delegates have set their values, so the lambdas passed to `validate` may reference other parameters. 

## [1.7.0] - 2019-03-23
### Added
- `printHelpOnEmptyArgs` parameter to `CliktCommand` constructor. ([#41](https://github.com/ajalt/clikt/issues/41))

### Fixed
- Usage errors now correctly print subcommand names. ([#47](https://github.com/ajalt/clikt/issues/47))
- Arguments with `multiple(required=true)` now report an error if no argument is given on the command line. ([#36](https://github.com/ajalt/clikt/issues/36))

## [1.6.0] - 2018-12-02
### Added
- `.multiple().unique()` modifier for options and arguments.

### Fixed
- Support multi-line input when redirecting stdin

## [1.5.0] - 2018-08-26
### Added
- Ability to use alternate output streams rather than stdin and stdout by setting `Context.console` or by passing a console to `TermUI` functions.

## [1.4.0] - 2018-07-31
### Added
- `path()` type for parameter values

### Changed
- Clikt now targets JVM 8 bytecode
- Responses to `TermUi.confirm()` are now case-insensitive

## [1.3.0] - 2018-06-23
### Added
- `defaultLazy` extension for options and arguments

### Changed
- `main` now prints messages to stderr instead of stdout

### Fixed
- Parameter help messages are now wrapped more consistently

## [1.2.0] - 2018-05-07
### Added
- Default parameter to `option().default()`

### Changed
- Treat tokens with unknown prefixes as arguments (this makes it easier
  to pass in file paths without using `--`).

## [1.1.0] - 2018-04-15
### Added
- `List<String>` overloads to `CliktCommand.parse` and `main`
- `err` parameter to `TermUi.echo`
- `error` property to `Abort`
