# Changelog

## [Unreleased]
### Added
- `CompletionCandidates.Fixed` now has a secondary convenience constructor that take a `vararg` of `String`s

### Changed
- `option()` and `argument()` now take optional `completionCandidates` parameters to override how completion is generated. The constructor and `copy` functions of `OptionsWithValues` and `ProcessedArgument` have changed to support default values.

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
