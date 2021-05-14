# Changelog

## Unreleased

## 3.2.0
_2021-05-14_

### Added
- `InputStream.isCliktParameterDefaultStdin` and `OutputStream.isCliktParameterDefaultStdout` to check if the streams returned from `inputStream`/`outputStream` options are proxying stdin/stdout ([#272](https://github.com/ajalt/clikt/issues/272))

### Changed
- Make parameters of `mutuallyExclusiveOptions` covariant to allow validation without explicit type annotations. ([#265](https://github.com/ajalt/clikt/issues/265))
- Updated kotlin to 1.5.0

### Fixed
- Reading from an option or argument property on a command that hasn't been invoked will now always throw an `IllegalStateException`

## 3.1.0
_2020-12-12_

### Added
- Added `required()` and `defaultLazy()` for nullable flag options like `switch()`. ([#240](https://github.com/ajalt/clikt/issues/240))
- Added support for generating autocomplete scripts for Fish shells ([#189](https://github.com/ajalt/clikt/issues/189))
- Added `CompletionCommand` and `CliktCommand.completionOption()` that will print an autocomplete script when invoked, as an alternative to using environment variables.

### Changed
- Updated Kotlin to 1.4.21
- `@argfiles` now allow line breaks in quoted values, which are included in the value verbatim. You can now end lines with `\` to concatenate them with the following line. ([#248](https://github.com/ajalt/clikt/issues/248))

## 3.0.1
_2020-09-03_

### Deprecated
- Deprecated calling `echo` with `err` or `lineSeparator` but no `message`. 


## 3.0.0
_2020-09-02_

### Added
- Clikt's JS target now supports both NodeJS and Browsers. ([#198](https://github.com/ajalt/clikt/issues/198))
- Default values for switch options are now shown in the help. Help text can be customized using the `defaultForHelp` argument, similar to normal options. ([#205](https://github.com/ajalt/clikt/issues/205))
- Added `FlagOption.convert` ([#208](https://github.com/ajalt/clikt/issues/208))
- Added ability to use unicode NEL character (`\u0085`) to manually break lines in help output ([#214](https://github.com/ajalt/clikt/issues/214))
- Added `help("")` extension to options and arguments as an alternative to passing the help as an argument ([#207](https://github.com/ajalt/clikt/issues/207))
- Added `valueSourceKey` parameter to `option`
- Added `check()` extensions to options and arguments as an alternative to `validate()`
- Added `prompt` and `confirm` functions to `CliktCommand` that call the `TermUi` equivalents with the current console.
- Added `echo()` overload with no parameters to CliktCommand that prints a newline by itself.
- Added localization support. You can set an implementation of the `Localization` interface on your context with your translations. ([#227](https://github.com/ajalt/clikt/issues/227))

### Fixed
- Hidden options will no longer be suggested as possible typo corrections. ([#202](https://github.com/ajalt/clikt/issues/202))
- Options and Arguments with `multiple(required=true)` will now show as required in help output. ([#212](https://github.com/ajalt/clikt/issues/212))
- Multiple short lines in a help text paragraph no longer appear dedented ([#215](https://github.com/ajalt/clikt/issues/215))

### Changed
- Updated Kotlin to 1.4.0
- `Argument.help` and `Option.help` properties have been renamed to `argumentHelp` and `optionHelp`, respectively. The `help` parameter names to `option()` and `argument()` are unchanged.
- `commandHelp` and `commandHelpEpilog` properties on `CliktCommand` are now `open`, so you can choose to override them instead of passing `help` and `epilog` to the constructor.
- Replaced `MapValueSource.defaultKey` with `ValueSource.getKey()`, which is more customizable.
- `Option.metavar`, `Option.parameterHelp`, `OptionGroup.parameterHelp` and `Argument.parameterHelp` properties are now functions.
- Changed constructor parameters of `CliktHelpFormatter`. Added `localization` and removed `usageTitle`, `optionsTitle`, `argumentsTitle`, `commandsTitle`, `optionsMetavar`, and `commandMetavar`. Those strings are now defined on equivalently named functions on `Localization`.

### Removed
- Removed `envvarSplit` parameter from `option()` and `convert()`. Option values from environment variables are no longer split automatically. ([#177](https://github.com/ajalt/clikt/issues/177))
- Removed public constructors from the following classes: `ProcessedArgument`, `OptionWithValues`, `FlagOption`, `CoOccurringOptionGroup`, `ChoiceGroup`, `MutuallyExclusiveOptions`.
- `MissingParameter` exception replaced with `MissingOption` and `MissingArgument`
- Removed `Context.helpOptionMessage`. Override `Localization.helpOptionMessage` and set it on your context instead.

### Deprecated
- `@ExperimentalCompletionCandidates` and `@ExperimentalValueSourceApi` annotations. These APIs no longer require an opt-in.

## 2.8.0
_2020-06-19_

### Added
- Added `error` parameter to `PrintMessage` and `PrintHelpMessage`. When `true`, `CliktCommand.main` will exit with status code 1. ([#187](https://github.com/ajalt/clikt/issues/187))

### Changed
- When `printHelpOnEmptyArgs` is `true` and no arguments are present, or when `invokeWithoutSubcommand` is `false` and no subcommand is present, `CliktCommand.main` will now exit with status code 1 rather than 0. 
- `restrictTo` now works with any `Comparable` value, not just `Number`.
- `CliktCommand.main` now accepts `Array<out String>`, not just `Array<String>`. ([#196](https://github.com/ajalt/clikt/issues/196))

### Fixed
- Fixed option values being reset when calling multiple subcommands with `allowMultipleSubcommands=true` ([#190](https://github.com/ajalt/clikt/issues/190))

## 2.7.1
_2020-05-19_

### Fixed
- Fixed NPE thrown in some cases when using `defaultByName` ([#179](https://github.com/ajalt/clikt/issues/179))

## 2.7.0
_2020-05-13_

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

## 2.6.0
_2020-03-15_

### Added
- `registeredSubcommands`, `registeredOptions`, `registeredArguments`, and `registeredParameterGroups` methods on `CliktCommand`.
- Ability to [read default option values](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.sources/-value-source/index.md) from configuration files and other sources. Support for Java property files is built in on JVM, see the `json` sample for an example of reading from other formats.
- `allowMultipleSubcommands` parameter to `CliktCommand` that allows you to pass multiple subcommands in the same call. ([docs](https://ajalt.github.io/clikt/commands/#chaining-and-repeating-subcommands))
- Errors from typos in subcommand names will now include suggested corrections. Corrections for options and subcommands are now based on a Jaro-Winkler similarity metric, and can be customized with `Context.correctionSuggestor`

### Changed
- Update Kotlin to 1.3.70
- `convert` can be called more than once on the same option or argument, including after calls to conversion functions like `int` and `file`.
- `CliktCommand.toString` now includes the class name
- Reverted automatic `~` expansion in `file()` and `path()` introduced in 2.5.0. If you need this behavior, you can implement it with code like `convert { /* expand tidle */ }.file()` 

### Deprecated
- `wrapValue` is now deprecated, since `convert` can be used in its place instead.

## 2.5.0
_2020-02-22_

### Added
- Clikt is now available as a Kotlin Multiplatform Project, supporting JVM, NodeJS, and native Windows, Linux, and macOS.
- `eagerOption {}` function to more easily register eager options.
- Eager options can now be added to option groups in help out by passing a value for `groupName` when creating them. 
- `canBeSymlink` parameter to `file()` and `path()` conversions that can be used to disallow symlinks
- `CliktCommand.eagerOption` to simplify creating custom eager options

### Changed
- The parameter names of `file()` and `path()` conversions have changed. The existing names are deprecated, and can be converted to the new usages with an IntelliJ inspection. Note that if you are calling these functions with unnamed arguments (e.g. `file(true, false)`), you'll need to add argument names in order to remove the deprecation warning.

### Deprecated
- The `CliktCommand.context` property has been deprecated in favor of the new name, `currentContext`, to avoid confusion with the `CliktCommand.context{}` method.
- `NoRunCliktCommand` was renamed to `NoOpCliktCommand`. The existing class is deprecated. ([#130](https://github.com/ajalt/clikt/issues/130))

### Fixed
- `file()` and `path()` conversions will now properly expand leading `~` in paths to the home directory for `mustExist`, `canBeFile`, and `canBeDir` checks. The property value is unchanged, and can still begin with a `~`. ([#131](https://github.com/ajalt/clikt/issues/79))

## 2.4.0
_2020-01-25_

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

## 2.3.0
_2019-11-07_

### Added
- `option().groupSwitch()`, which works like `groupChoice()`, but uses a `switch()` option rather than a `choice()` option.
- `UsageError` now has a `statusCode` parameter (which defaults to 1). If you're using `ClicktCommand.main`, the value of `statusCode` will be passed to `exitProcess`. 

### Changed
- Shell completion code is now printed by throwing a `PrintCompletionMessage` (a subclass of `PrintMessage`) rather than calling `echo` directly.

## 2.2.0
_2019-09-25_

### Added
- Added [`enum()` conversion](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.types/enum/) for options and arguments. ([#84](https://github.com/ajalt/clikt/issues/84))

### Changed
- There are now several ways of [preventing @-file expansion](https://ajalt.github.io/clikt/advanced/#preventing-file-expansion)

### Fixed
- Help output missing items when no help text is specified. ([#85](https://github.com/ajalt/clikt/issues/85))
- Help output not grouping options in groups passed to `groupChoice`. ([#88](https://github.com/ajalt/clikt/issues/88))

## 2.1.0
_2019-05-23_

### Added
- Ability to prevent [rewrapping individual paragraphs](https://ajalt.github.io/clikt/documenting/#preventing-rewrapping) in help output.
- Added parameter `required` to `Option.multiple()` to require at least one instance of the option on the command line.

### Changed
- `CliktCommand.toString()` now includes the names and values of all parameters and subcommands.

### Fixed
- Create subcommand context when `helpOptionNames` is empty. ([#64](https://github.com/ajalt/clikt/issues/64))

## 2.0.0
_2019-05-12_

### Added
- [Bash autocomplete script generation](https://ajalt.github.io/clikt/autocomplete/). A property named `completionCandidates` has been added to `Argument` and `Option` interfaces, and corresponding parameters have been added to the various implementation constructors, as well as the `convert` functions. You can use this to control the values autocomplete that will be suggested.
- [`option().split()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/split/), and the corresponding [`OptionWithValues.valueSplit`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/-option-with-values/value-split/).
- Marking options as deprecated with [`option().deprecated()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/deprecated/)
- You can manually set the pattern to split envvars on by passing a pattern to the `envvarSplit` parameter of [`option()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/option/)
- [Option groups](https://ajalt.github.io/clikt/documenting/#grouping-options-in-help), [mutually exclusive groups](https://ajalt.github.io/clikt/options/#prompting-for-input), [co-occurring groups](https://ajalt.github.io/clikt/options/#co-occurring-option-groups), and [choice options with groups](https://ajalt.github.io/clikt/options/#choice-options-with-groups)
- Support for [Command line argument files](https://ajalt.github.io/clikt/advanced/#command-line-argument-files-files) a.k.a "@-files"

### Changed
- If multiple `--` tokens are present on the command line, all subsequent occurrences after the first are now parsed as positional arguments. Previously, subsequent `--` tokens were skipped.  
- The `PlaintextHelpFormatter` has been replaced with `CliktHelpFormatter`, which is more customizable. See [the docs](https://ajalt.github.io/clikt/documenting/) for more info, or the [new sample](https://github.com/ajalt/clikt/tree/master/samples/ansicolors) for an example of customizing help output to use ANSI colors.
- Some of the properties and constructor parameters for [`OptionWithValues`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/-option-with-values/) and [`ProcessedArgument`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.arguments/-processed-argument/) have changed.
- The `OptionDelegate` interface has changed, and `GroupableOption` and `ParameterHolder` interfaces have been added to work with option groups.
- [Parameter validation](https://ajalt.github.io/clikt/parameters/#parameter-validation) now occurs after all parameter delegates have set their values, so the lambdas passed to `validate` may reference other parameters. 

## 1.7.0
_2019-03-23_

### Added
- `printHelpOnEmptyArgs` parameter to `CliktCommand` constructor. ([#41](https://github.com/ajalt/clikt/issues/41))

### Fixed
- Usage errors now correctly print subcommand names. ([#47](https://github.com/ajalt/clikt/issues/47))
- Arguments with `multiple(required=true)` now report an error if no argument is given on the command line. ([#36](https://github.com/ajalt/clikt/issues/36))

## 1.6.0
_2018-12-02_

### Added
- `.multiple().unique()` modifier for options and arguments.

### Fixed
- Support multi-line input when redirecting stdin

## 1.5.0
_2018-08-26_

### Added
- Ability to use alternate output streams rather than stdin and stdout by setting `Context.console` or by passing a console to `TermUI` functions.

## 1.4.0
_2018-07-31_

### Added
- `path()` type for parameter values

### Changed
- Clikt now targets JVM 8 bytecode
- Responses to `TermUi.confirm()` are now case-insensitive

## 1.3.0
_2018-06-23_

### Added
- `defaultLazy` extension for options and arguments

### Changed
- `main` now prints messages to stderr instead of stdout

### Fixed
- Parameter help messages are now wrapped more consistently

## 1.2.0
_2018-05-07_

### Added
- Default parameter to `option().default()`

### Changed
- Treat tokens with unknown prefixes as arguments (this makes it easier
  to pass in file paths without using `--`).

## 1.1.0
_2018-04-15_

### Added
- `List<String>` overloads to `CliktCommand.parse` and `main`
- `err` parameter to `TermUi.echo`
- `error` property to `Abort`
