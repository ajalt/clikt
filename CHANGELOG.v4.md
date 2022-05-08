# Changelog

## 4.0 (Unreleased)
### Added
- Added `obj` setter to context builder as an alternative to `currentContext.obj`
- Added `option().boolean()` and `argument().boolean()`
- `uint()` and `ulong()` parameter type conversions.
- `CliktCommand.test` extension for testing your commands and their output

### Changed
- `prompt` and `confirm` are now implemented with mordant's prompt functionality, and the method parameters have changed to match mordant's
- When using `treatUnknownOptionsAsArgs`, grouped short options like `-abc` will be treated as an argument rather than reporting an error as long as they don't match any short options in the command. ([#340](https://github.com/ajalt/clikt/pull/340)) 

### Removed
- `CliktConsole`. Mordant is now used for all input and output. If you were defining a custom console, instead define a mordant `TerminalInterface` and set it on your context's `Terminal`.
- `TermUi.echo`, `TermUi.prompt`, and `TermUi.confirm`. Use the equivalent methods on your `CliktCommand`, or use mordant's prompts directly.

