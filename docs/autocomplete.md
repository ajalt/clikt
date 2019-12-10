# Bash Autocomplete

Clikt includes built-in support for generating autocomplete scripts for bash and zsh.

## Supported Functionality

Currently subcommand, option, and [command alias][command-aliases] names can be completed, as well as
values for options and arguments. `choice` parameters are completed with their possible values.
Other parameter types are completed as file or directory names.
[`Context.allowInterspersedArgs`][allowInterspersedArgs] is supported.

For example:

```
$ ./repo <TAB><TAB>
commit clone pull

$ ./repo -<TAB>
--config -h --help --repo-home --verbose

$./repo --repo-home ./g<TAB>
./git ./got ./good
```

## Enabling Completion

Clikt handles autocomplete by generating a shell script that defines the completion. You generate
the script once each time your CLI changes, and load it each time your start your shell.

To generate the shell script, you need to invoke your program with a special environment variable.
You can set the variable name manually with the `autoCompleteEnvvar` parameter in the
[`CliktCommand` constructor][CliktCommand]. By default it's your command's name capitalized,
with `-` replaced with `_`, and prefixed with another `_`.
So if your command name is `my-command`, the variable would be `_MY_COMMAND_COMPLETE=bash`
or `_MY_COMMAND_COMPLETE=zsh`, depending on your current shell.

For example to activate bash autocomplete for this command:

```kotlin
class MyProgram: CliktCommand() {
    // ...
}
```

You can generate the completion script and save it to a file like this:

```bash
$ _MY_PROGRAM_COMPLETE=bash ./my-program > ~/my-program-completion.sh
```

Finally, source the file to activate completion:

```bash
$ source ~/my-program-completion.sh
```

You can add that source command to your .bashrc so that completion is always available:

```bash
$ echo source ~/my-program-completion.sh >> ~/.bashrc
```

You'll need to regenerate the completion script any time your command structure changes.

## Customizing Completions

There is built-in completion for values for [`choice`][choice] parameters,
and for parameters converted with [`file`][file] and [`path`][path].

You can add completion for other parameters with the `completionCandidates` parameter to
[`option()`][option] and [`argument()`][argument]. The value can be one of the following:

- `None`: The default. The parameter's values will not be completed.
- `Path`: Completions will be filesystem paths.
- `Hostname`: Completions will be read from the system's hosts file.
- `Username`: Completions will be taken from the system's users.
- `Fixed`: Completions are given as a fixed set of strings.

## Limitations

[Token Normalization][token-normalization] is not supported.

If you have arguments that occur after a `multiple` argument, those arguments won't be
autocompleted. Partial command lines are ambiguous in those situations, and Clikt assumes that
you're trying to complete the `multiple` argument rather than the later ones.

Bash must be at least version 3, or Zsh must be at least version 4.1.


[command-aliases]:       advanced.md#command-aliases
[allowInterspersedArgs]: api/clikt/com.github.ajalt.clikt.core/-context/allow-interspersed-args.md
[CliktCommand]:          api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.md
[choice]:                api/clikt/com.github.ajalt.clikt.parameters.types/choice.md
[file]:                  api/clikt/com.github.ajalt.clikt.parameters.types/file.md
[path]:                  api/clikt/com.github.ajalt.clikt.parameters.types/path.md
[option]:                api/clikt/com.github.ajalt.clikt.parameters.options/option.md
[argument]:              api/clikt/com.github.ajalt.clikt.parameters.arguments/argument.md
[token-normalization]:   advanced.md#token-normalization
