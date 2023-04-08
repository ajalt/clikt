# Changelog

## 4.0 (Unreleased)
### Added
- You can now use markdown in your help strings, including tables and lists. Clikt uses the Mordant library for rendering.
- Help output and error messages now include colors by default. You can disable this or customize the styling be configuring the `context.terminal`
- Added `Option.varargValues()` to create an option that accepts a variable number of values, and `Option.optionalValue()` to create an option whose value is optional.
- Added `obj` setter to context builder as an alternative to `currentContext.obj`
- Added `boolean()` parameter type conversions.
- Added `uint()` and `ulong()` parameter type conversions.
- Added `CliktCommand.test` extension for testing your commands and their output
- Clikt will now report multiple errors if they occur, rather than just the first. ([#367](https://github.com/ajalt/clikt/issues/367))
- Added `CliktCommand.allHelpParams()`, which can be overridden to change which parameters are displayed in help output
- Added `Context.argumentFileReader` which allows custom loading of argument files 

### Changed
- `prompt` and `confirm` are now implemented with mordant's prompt functionality, and the method parameters have changed to match mordant's
- When using `treatUnknownOptionsAsArgs`, grouped short options like `-abc` will be treated as an argument rather than reporting an error as long as they don't match any short options in the command. ([#340](https://github.com/ajalt/clikt/pull/340)) 
- Update kotlin to 1.7.0
- Clikt no longer automatically calls `trimIndent` on strings passed to `help`. Call `trimIndent` or `trimMargin` yourself if necessary.
- The constructor of `UsageError` and its subclasses no longer takes a `context` parameter. The context is now inferred automatically.
- `Context.Builder.helpOptionNames` now accepts any iterable rather than just a set.

### Fixed
- When parsing a command line with more than one error, Clikt will now always report the error that occurs earliest ([#361](https://github.com/ajalt/clikt/issues/361))

### Removed
- Removed `CliktConsole`. Mordant is now used for all input and output. If you were defining a custom console, instead define a mordant `TerminalInterface` and set it on your context's `Terminal`.
- Removed `TermUi.echo`, `TermUi.prompt`, and `TermUi.confirm`. Use the equivalent methods on your `CliktCommand`, or use mordant's prompts directly.
- Removed legacy JS publications. Now only the JS/IR artifacts are published.  
- Removed `CliktHelpFormatter`. Use `MordantHelpFormatter` instead.
