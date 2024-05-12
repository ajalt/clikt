# Changelog

## Unreleased
### Added
- Added `NoSuchArgument` exception that is thrown when too many arguments were given on the command line. Previously, a less specific `UsageError` was thrown instead.
- Added `CommandLineParser.tokenize` that splits a string into argv tokens.
- Added `CommandLineParser` that provides functions for parsing and finalizing commands manually for more control.
- Added `Context.invokedSubcommands` that contains all subcommands of the current command that are going to be invoked when `allowMultipleSubcommands` is `true`.
- Added `SuspendingCliktCommand` that has a `suspend fun run` method, allowing you to use coroutines in your commands.
- Added `ChainedCliktCommand` that allows you to return a value from your `run` method and pass it to the next command in the chain.
- Added `Context.data` as an alternative to `obj` that allows you to store more than one object in the context.
- Added `Context.echoer` to customize how `echo` messages are printed.
- Added `CompletionGenerator` to manually generate completions for a command.
- Added `Context.exitProcess` which you can use to prevent the process from exiting during tests.
- Added core module that supports iOS, watchOS, tvOS, and wasmWasi targets.
### Changed
- In a subcommand with `argument().multiple()`, the behavior is now the same regardless of the value of `allowMultipleSubcommands`: if a token matches a subcommand name, it's now treated as a subcommand rather than a positional argument.
- Due to changes to the internal parsing algorithm, the exact details of error messages when multiple usage errors occur have changed in some cases.
- **Breaking Change:** Moved the following parameters from `CliktCommand`'s constructor; override the corresponding properties instead:

  | removed parameter           | replacement property            |
  |-----------------------------|---------------------------------|
  | `help`                      | `fun help`                      |
  | `epilog`                    | `fun helpEpilog`                |
  | `invokeWithoutSubcommand`   | `val invokeWithoutSubcommand`   |
  | `printHelpOnEmptyArgs`      | `val printHelpOnEmptyArgs`      |
  | `helpTags`                  | `val helpTags`                  |
  | `autoCompleteEnvvar`        | `val autoCompleteEnvvar`        |
  | `allowMultipleSubcommands`  | `val allowMultipleSubcommands`  |
  | `treatUnknownOptionsAsArgs` | `val treatUnknownOptionsAsArgs` |
  | `hidden`                    | `val hiddenFromHelp`            |
- The following methods on `CliktCommand` have been renamed: `commandHelp` -> `help`, `commandHelpEpilog` -> `epilog`. The old names are deprecated.
- **Breaking Change:** `CliktCommand.main` and `CliktCommand.parse` are now extension functions rather than methods.
- **Breaking Change:** `Context.obj` and `Context.terminal`, and `OptionTransformContext.terminal` are now extension functions rather than properties.
- **Breaking Change:** The `RenderedSection` and `DefinitionRow` classes have moved to `AbstractHelpFormatter`.

### Fixed
- Fixed excess arguments not being reported when `allowMultipleSubcommands=true` and a subcommand has excess arguments followed by another subcommand.

### Deprecated
- Deprecated `Context.originalArgv`. It will now always return an empty list. If your commands need an argv, you can pass it to them before you run them, or set in on the new `Context.data` map.
- Deprecated `Context.expandArgumentFiles`. Use `Context.argumentFileReader` instead.

### Removed
- Removed previously deprecated experimental annotations.
- Removed `MordantHelpFormatter.graphemeLength`
- Removed `TermUi`

## 4.4.0
### Added
- Publish `linuxArm64` and `wasmJs` targets.

## 4.3.0
### Added
- Added `limit` parameter to `option().counted()` to limit the number of times the option can be used. You can either clamp the value to the limit, or throw an error if the limit is exceeded. ([#483](https://github.com/ajalt/clikt/issues/483))
- Added `Context.registerClosable` and `Context.callOnClose` to allow you to register cleanup actions that will be called when the command exits. ([#395](https://github.com/ajalt/clikt/issues/395))

### Fixed
- Fixed `unrecognized modifier 'i'` that happened on tab-completion when using sub command aliases. Thanks to @hick209 for the contribution. ([#500](https://github.com/ajalt/clikt/pull/500))
- Make sure auto complete script works on zsh, fixing the error `complete:13: command not found: compdef`. Thanks to @hick209 for the contribution. ([#499](https://github.com/ajalt/clikt/pull/499))

## 4.2.2
### Changed
- Options and arguments can now reference option groups in their `defaultLazy` and other finalization blocks. They can also freely reference each other, including though chains of references. ([#473](https://github.com/ajalt/clikt/issues/473))
- Updated Kotlin to 1.9.21 ([#472](https://github.com/ajalt/clikt/pull/472))

## 4.2.1
### Added
- Added `toString` implementations to options and arguments. ([#434](https://github.com/ajalt/clikt/issues/434))
- Added `CliktCommand.test` overload that takes a vararg of `String`s as the command line arguments. Thanks to @sschuberth for the contribution ([#451](https://github.com/ajalt/clikt/issues/451))

### Fixed
- Update Mordant dependency to fix crashes on native targets and GraalVM ([#447](https://github.com/ajalt/clikt/issues/447))

## 4.2.0
### Added
- Added `requireConfirmation` parameter to `option().prompt()` ([#426](https://github.com/ajalt/clikt/issues/426))
- Added `CliktCommand.terminal` extension for accessing the terminal from a command.
- Added `includeSystemEnvvars`, `ansiLevel`, `width`, and `height` parameters to all `CliktCommand.test` overloads.

### Deprecated
- Deprecated `CliktCommand.prompt`, use `CliktCommand.terminal.prompt` or `Prompt` instead.
- Deprecated `CliktCommand.confirm`, use `YesNoPrompt` instead.

### Fixed
- Fixed incorrect error message when a `defaultLazy` option referenced a `required` option. ([#430](https://github.com/ajalt/clikt/issues/430))

## 4.1.0
### Added
- Added `MordantHelpFormatter.renderAttachedOptionValue` that you can override to change how option values are shown, e.g. if you want option to show as `--option <value>` instead of `--option=<value>`. ([#416](https://github.com/ajalt/clikt/issues/416))
- Added `option().optionalValueLazy{}`, which work like `optionalValue()` but the default value is computed lazily. ([#381](https://github.com/ajalt/clikt/issues/381))

### Changed
- Updated Kotlin to 1.9.0
- `PrintMessage`, `PrintHelpMessage` and `PrintCompletionMessage` now default to exiting with a status code 0, which is the behavior they had in 3.x. ([#419](https://github.com/ajalt/clikt/issues/419))

## 4.0.0
### Added
- Added `Context.errorEncountered` which is true if parsing has continued after an error was encountered.
- `option().help{""}` and `argument().help{""}` extensions that set the parameter's help text lazily, with access to the current context so that you can add colors.

### Changed
- `Option.optionHelp` and `Argument.argumentHelp`, `CliktCommand.commandHelp`, and `CliktCommand.commandHelpEpilog` are now methods that take the context as an argument, and the `help` parameter to `copy` is now a `helpGetter` lambda. `CliktCommand.shortHelp` now takes the context as an argument.
- The `message` method on `TransformContext` interfaces is now an extension.

### Deprecated
- Deprecated `CliktCommand.commandHelp` and `commandHelpEpilog` properties in favor of the methods with the same name.

## 4.0.0-RC
### Added
- You can now use markdown in your help strings, including tables and lists. Clikt uses the Mordant library for rendering.
- Help output and error messages now include colors by default. You can disable this or customize the styling by configuring the `context.terminal`
- Added `Option.varargValues()` to create an option that accepts a variable number of values
- Added `Option.optionalValue()` to create an option whose value is optional.
- Added `obj` setter to context builder as an alternative to `currentContext.obj`
- Added `boolean()` parameter type conversions.
- Added `uint()` and `ulong()` parameter type conversions.
- Added `nullableFlag()` parameter transformation.
- Added `CliktCommand.test` extension for testing your commands and their output
- Clikt will now report multiple errors if they occur via the new `MultiUsageError` exception, rather than just reporting the first error. ([#367](https://github.com/ajalt/clikt/issues/367))
- Added `CliktCommand.allHelpParams()`, which can be overridden to change which parameters are displayed in help output
- Added `Context.argumentFileReader` which allows custom loading of argument files
- Added `Context.allowGroupedShortOptions` which can disable parsing `-abc` as `-a -b -c`
- Options named `-?` or `/?` are now supported
- Added `option(eager=true)` to create an eager option that takes values
- Added `option(acceptsUnattachedValue=false)` to force the option to only accept values like `--option=1` and not `--option 1`
- Added `CliktCommand.test()` that captures the output of a command and does not exit the process.

### Removed
- Removed `CliktConsole`. Mordant is now used for all input and output. If you were defining a custom console, instead define a mordant `TerminalInterface` and set it on your context's `Terminal`.
- Removed `TermUi.echo`, `TermUi.prompt`, and `TermUi.confirm`. Use the equivalent methods on your `CliktCommand`, or use mordant's prompts directly.
- Removed legacy JS publications. Now only the JS/IR artifacts are published.
- Removed `CliktHelpFormatter`. Use `MordantHelpFormatter` instead.
- Removed `FlagOption` and `EagerOption` classes. All options are now implemented as transformations on `OptionWithValues`. `FlagOption` is now `OptionWithValues<Boolean, Boolean, Boolean>`.

### Changed
- `prompt` and `confirm` are now implemented with mordant's prompt functionality, and the method parameters have changed to match mordant's
- When using `treatUnknownOptionsAsArgs`, grouped short options like `-abc` will be treated as an argument rather than reporting an error as long as they don't match any short options in the command. ([#340](https://github.com/ajalt/clikt/pull/340))
- Clikt no longer automatically calls `trimIndent` on strings passed to `help`. Call `trimIndent` or `trimMargin` yourself if necessary.
- `Context.Builder.helpOptionNames` now accepts any iterable rather than just a set.
- `CliktCommand.echo` and `prompt` are now public. ([#407](https://github.com/ajalt/clikt/issues/407))
- Internally, all options are implemented transformations on `OptionWithValues`, rather than using separate classes for each option type. 
- Some Localization strings have changed, removed `Localization.aborted()`, added `Localization.argumentsMetavar()`
- `Context.Builder.helpFormatter` is now a lambda that takes the current context as an argument
- Exceptions have been reworked so that all exceptions thrown by Clikt are subclasses of `CliktError`.
- `CliktError` now includes `statusCode` and `printError` properties.
- The constructor of `UsageError` and its subclasses no longer takes a `context` parameter. The context is now inferred automatically.
- `UsageError.formatUsage` now takes the localization and formatter as arguments

### Fixed
- When parsing a command line with more than one error, Clikt will now always report the error that occurs earliest if it can't report them all ([#361](https://github.com/ajalt/clikt/issues/361))
- When `treatUnknownOptionsAsArgs` is true, grouped unknown short options will now be treated as arguments rather than reporting an error.

## 3.5.4
### Fixed
- Revert jvm jars to target Java 8

## 3.5.3
### Changed
- Updated Kotlin to 1.8.22

### Fixed
- Context is now set properly on NoSuchOption exceptions when thrown from subcommands. ([#399](https://github.com/ajalt/clikt/issues/399))
- When `treatUnknownOptionsAsArgs` is true, grouped unknown short options will now be treated as arguments rather than reporting an error.

## 3.5.2
### Changed
- Updated Kotlin to 1.8.10

### Fixed
- Fix `CliktCommand.prompt` on NodeJS targets that would hang due to KT-55817 ([#387](https://github.com/ajalt/clikt/issues/387))

## 3.5.1
### Changed
- Updated Kotlin to 1.7.20

### Fixed
- Support unicode in environment variable values on Native Windows. ([#362](https://github.com/ajalt/clikt/issues/362))
- Support environment variables for options in a mutually exclusive options group. ([#384](https://github.com/ajalt/clikt/issues/384))

## 3.5.0
### Added
- Added `hidden` parameter to `CliktCommand`, which will prevent the command from being displayed as a subcommand in help output  ([#353](https://github.com/ajalt/clikt/issues/353))
- Publish artifacts for the `macosArm64` target. Note that this target is not tested on CI. ([#352](https://github.com/ajalt/clikt/issues/352))

### Changed
- Default values for arguments will now be included in help output when `showDefaultValues=true` is set on your help formatter ([#357](https://github.com/ajalt/clikt/issues/357))

### Fixed
- Fix flags and other options with defaults not being usable in `mutuallyExclusiveOptions` ([#349](https://github.com/ajalt/clikt/issues/349))
- Fix `CompletionCommand` generating completion for itself ([#355](https://github.com/ajalt/clikt/issues/355))

## 3.4.2
### Deprecated
- `TermUi.echo`, `TermUi.prompt`, and `TermUi.confirm`. Use the equivalent methods on `CliktCommand` instead. ([#344](https://github.com/ajalt/clikt/issues/344))

## 3.4.1
### Added
- Added `obj` setter to context builder as an alternative to `currentContext.obj`
- Added `option().boolean()` and `argument().boolean()`
- `uint()` and `ulong()` parameter type conversions.
- `CliktCommand.test` extension for testing your commands and their output

### Changed
- Updated Kotlin to 1.6.20

## 3.4.0
### Changed
- `unique()` now works with any option with a list type, not just `multiple()` options ([#332](https://github.com/ajalt/clikt/issues/332))
- Updated Kotlin to 1.6.10

### Fixed
- Fixed co-occurring option groups returning null when all options in the group are defined in environment variables ([#330](https://github.com/ajalt/clikt/issues/330))

## 3.3.0
### Added
- Added `default` parameter to `argument().multiple()` ([#305](https://github.com/ajalt/clikt/issues/305))
- `Context.originalArgv` that allows you to read the command line arguments from within a command's `run` ([#290](https://github.com/ajalt/clikt/issues/290))
- `context { envarReader = {...} }` to set a custom function to read from environment variables ([#299](https://github.com/ajalt/clikt/issues/299))

### Changed
- `defaultLazy` values can now reference other parameters, as long the referenced parameters do not also reference other parameters
- You can now call `CliktCommand.context` multiple times on the same command, and all builder blocks will be applied 
- Validate values entered to a `prompt` option, and show another prompt if the validation fails ([#288](https://github.com/ajalt/clikt/issues/288))
- Updated kotlin to 1.5.31

### Fixed
- Report error when excess arguments are given to a command with `allowMultipleSubcommands=true` ([#303](https://github.com/ajalt/clikt/issues/303))

## 3.2.0
### Added
- `InputStream.isCliktParameterDefaultStdin` and `OutputStream.isCliktParameterDefaultStdout` to check if the streams returned from `inputStream`/`outputStream` options are proxying stdin/stdout ([#272](https://github.com/ajalt/clikt/issues/272))

### Changed
- Make parameters of `mutuallyExclusiveOptions` covariant to allow validation without explicit type annotations. ([#265](https://github.com/ajalt/clikt/issues/265))
- Updated kotlin to 1.5.0

### Fixed
- Reading from an option or argument property on a command that hasn't been invoked will now always throw an `IllegalStateException`

## 3.1.0
### Added
- Added `required()` and `defaultLazy()` for nullable flag options like `switch()`. ([#240](https://github.com/ajalt/clikt/issues/240))
- Added support for generating autocomplete scripts for Fish shells ([#189](https://github.com/ajalt/clikt/issues/189))
- Added `CompletionCommand` and `CliktCommand.completionOption()` that will print an autocomplete script when invoked, as an alternative to using environment variables.

### Changed
- Updated Kotlin to 1.4.21
- `@argfiles` now allow line breaks in quoted values, which are included in the value verbatim. You can now end lines with `\` to concatenate them with the following line. ([#248](https://github.com/ajalt/clikt/issues/248))

## 3.0.1
### Deprecated
- Deprecated calling `echo` with `err` or `lineSeparator` but no `message`. 


## 3.0.0
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
### Added
- Added `error` parameter to `PrintMessage` and `PrintHelpMessage`. When `true`, `CliktCommand.main` will exit with status code 1. ([#187](https://github.com/ajalt/clikt/issues/187))

### Changed
- When `printHelpOnEmptyArgs` is `true` and no arguments are present, or when `invokeWithoutSubcommand` is `false` and no subcommand is present, `CliktCommand.main` will now exit with status code 1 rather than 0. 
- `restrictTo` now works with any `Comparable` value, not just `Number`.
- `CliktCommand.main` now accepts `Array<out String>`, not just `Array<String>`. ([#196](https://github.com/ajalt/clikt/issues/196))

### Fixed
- Fixed option values being reset when calling multiple subcommands with `allowMultipleSubcommands=true` ([#190](https://github.com/ajalt/clikt/issues/190))

## 2.7.1
### Fixed
- Fixed NPE thrown in some cases when using `defaultByName` ([#179](https://github.com/ajalt/clikt/issues/179))

## 2.7.0
### Added
- Ability to use custom program exit status codes via `ProgramResult`.
- `inputStream` and `outputStream` conversions for options and arguments. ([#157](https://github.com/ajalt/clikt/issues/157) and [#159](https://github.com/ajalt/clikt/issues/159))
- `splitPair`, `toMap`, and `associate` extensions on `option`. ([#166](https://github.com/ajalt/clikt/issues/166))
- `treatUnknownOptionsAsArgs` parameter to `CliktCommand`. ([#152](https://github.com/ajalt/clikt/issues/152))
- `defaultByName` function for `groupChoice` and `groupSwitch` options. ([#171](https://github.com/ajalt/clikt/issues/171))

### Changed
- Update Kotlin to 1.3.71
- Improved command name inference. Now, a class like `MyAppCommand` will infer its `commandName` as `my-app` rather than `myappcommand`. You can still specify the name manually as before. ([#168](https://github.com/ajalt/clikt/pull/168))

### Fixed
- Correctly parse short options with attached values that contain `=`

## 2.6.0
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
### Added
- `CompletionCandidates.Fixed` now has a secondary convenience constructor that take a `vararg` of `String`s
- `CompletionCadidates.Custom`, which allows you to call other binaries or write a script to generate completions. This class is currently experimental. ([#79](https://github.com/ajalt/clikt/issues/79))
- `Option.wrapValue` and `Argument.wrapValue` to make it easier to reuse existing conversion functions.
- `ignoreCase` parameter to `choice()` and `enum()` conversion functions.

### Changed
- `option()` and `argument()` now take optional `completionCandidates` parameters to override how completion is generated. The constructor and `copy` functions of `OptionsWithValues` and `ProcessedArgument` have changed to support default values.
- The overloads of `findObject` ([1](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.core/-context/find-object/) [2](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.core/find-object/)) that take a default value have been renamed `findOrSetObject`. The existing names are marked with `@Deprecated`, and IntelliJ can convert your call sites automatically. ([#110](https://github.com/ajalt/clikt/issues/110))
- `enum()` parameters now accept case-insensitive values by default. You change this behavior by passing `ignoreCase = false` to `enum()` ([#115](https://github.com/ajalt/clikt/issues/115))

### Fixed
- `groupChoice` help output now includes the choices in the help output metavar
- `TermUi.edit*` functions could freeze on certain editors ([#99](https://github.com/ajalt/clikt/issues/99), thanks @iampravikant and @sebokopter)
- Shell completion can now handle command names with dashes. ([#104](https://github.com/ajalt/clikt/issues/104))
- Arguments with `=` in them could be incorrectly interpreted as options ([#106](https://github.com/ajalt/clikt/issues/106))

## 2.3.0
### Added
- `option().groupSwitch()`, which works like `groupChoice()`, but uses a `switch()` option rather than a `choice()` option.
- `UsageError` now has a `statusCode` parameter (which defaults to 1). If you're using `ClicktCommand.main`, the value of `statusCode` will be passed to `exitProcess`. 

### Changed
- Shell completion code is now printed by throwing a `PrintCompletionMessage` (a subclass of `PrintMessage`) rather than calling `echo` directly.

## 2.2.0
### Added
- Added [`enum()` conversion](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.types/enum/) for options and arguments. ([#84](https://github.com/ajalt/clikt/issues/84))

### Changed
- There are now several ways of [preventing @-file expansion](https://ajalt.github.io/clikt/advanced/#preventing-file-expansion)

### Fixed
- Help output missing items when no help text is specified. ([#85](https://github.com/ajalt/clikt/issues/85))
- Help output not grouping options in groups passed to `groupChoice`. ([#88](https://github.com/ajalt/clikt/issues/88))

## 2.1.0
### Added
- Ability to prevent [rewrapping individual paragraphs](https://ajalt.github.io/clikt/documenting/#preventing-rewrapping) in help output.
- Added parameter `required` to `Option.multiple()` to require at least one instance of the option on the command line.

### Changed
- `CliktCommand.toString()` now includes the names and values of all parameters and subcommands.

### Fixed
- Create subcommand context when `helpOptionNames` is empty. ([#64](https://github.com/ajalt/clikt/issues/64))

## 2.0.0
### Added
- [Bash autocomplete script generation](https://ajalt.github.io/clikt/autocomplete/). A property named `completionCandidates` has been added to `Argument` and `Option` interfaces, and corresponding parameters have been added to the various implementation constructors, as well as the `convert` functions. You can use this to control the values autocomplete that will be suggested.
- [`option().split()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/split/), and the corresponding [`OptionWithValues.valueSplit`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/-option-with-values/value-split/).
- Marking options as deprecated with [`option().deprecated()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/deprecated/)
- You can manually set the pattern to split envvars on by passing a pattern to the `envvarSplit` parameter of [`option()`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/option/)
- [Option groups](https://ajalt.github.io/clikt/documenting/#grouping-options-in-help), [mutually exclusive groups](https://ajalt.github.io/clikt/options/#prompting-for-input), [co-occurring groups](https://ajalt.github.io/clikt/options/#co-occurring-option-groups), and [choice options with groups](https://ajalt.github.io/clikt/options/#choice-options-with-groups)
- Support for [Command line argument files](https://ajalt.github.io/clikt/advanced/#command-line-argument-files-files) a.k.a. "@-files"

### Changed
- If multiple `--` tokens are present on the command line, all subsequent occurrences after the first are now parsed as positional arguments. Previously, subsequent `--` tokens were skipped.  
- The `PlaintextHelpFormatter` has been replaced with `CliktHelpFormatter`, which is more customizable. See [the docs](https://ajalt.github.io/clikt/documenting/) for more info, or the [new sample](https://github.com/ajalt/clikt/tree/master/samples/ansicolors) for an example of customizing help output to use ANSI colors.
- Some of the properties and constructor parameters for [`OptionWithValues`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.options/-option-with-values/) and [`ProcessedArgument`](https://ajalt.github.io/clikt/api/clikt/com.github.ajalt.clikt.parameters.arguments/-processed-argument/) have changed.
- The `OptionDelegate` interface has changed, and `GroupableOption` and `ParameterHolder` interfaces have been added to work with option groups.
- [Parameter validation](https://ajalt.github.io/clikt/parameters/#parameter-validation) now occurs after all parameter delegates have set their values, so the lambdas passed to `validate` may reference other parameters. 

## 1.7.0
### Added
- `printHelpOnEmptyArgs` parameter to `CliktCommand` constructor. ([#41](https://github.com/ajalt/clikt/issues/41))

### Fixed
- Usage errors now correctly print subcommand names. ([#47](https://github.com/ajalt/clikt/issues/47))
- Arguments with `multiple(required=true)` now report an error if no argument is given on the command line. ([#36](https://github.com/ajalt/clikt/issues/36))

## 1.6.0
### Added
- `.multiple().unique()` modifier for options and arguments.

### Fixed
- Support multi-line input when redirecting stdin

## 1.5.0
### Added
- Ability to use alternate output streams rather than stdin and stdout by setting `Context.console` or by passing a console to `TermUI` functions.

## 1.4.0
### Added
- `path()` type for parameter values

### Changed
- Clikt now targets JVM 8 bytecode
- Responses to `TermUi.confirm()` are now case-insensitive

## 1.3.0
### Added
- `defaultLazy` extension for options and arguments

### Changed
- `main` now prints messages to stderr instead of stdout

### Fixed
- Parameter help messages are now wrapped more consistently

## 1.2.0
### Added
- Default parameter to `option().default()`

### Changed
- Treat tokens with unknown prefixes as arguments (this makes it easier
  to pass in file paths without using `--`).

## 1.1.0
### Added
- `List<String>` overloads to `CliktCommand.parse` and `main`
- `err` parameter to `TermUi.echo`
- `error` property to `Abort`
