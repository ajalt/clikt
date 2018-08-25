# Changelog

## [Unreleased]
### Added
- Ability to use alternate output streams rather than stdin and stdout by setting `Context.console`.

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
- Default parameter to option().default()

### Changed
- Treat tokens with unknown prefixes as arguments (this makes it easier
  to pass in file paths without using `--`).

## [1.1.0] - 2018-04-15
### Added
- `List<String>` overloads to `CliktCommand.parse` and `main`
- `err` parameter to `TermUi.echo`
- `error` property to `Abort`

