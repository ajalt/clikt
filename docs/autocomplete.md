# Shell Autocomplete

Clikt includes built-in support for generating autocomplete scripts for bash, zsh and fish shells.

=== "Example"
    ```text
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

### With an environment variable

You can generate the completion script by invoking your program with a special environment variable.

You can set the variable name manually with the `autoCompleteEnvvar` parameter in the
[`CliktCommand` constructor][CliktCommand]. By default it's your command's name capitalized,
with `-` replaced with `_`, and prefixed with another `_`.
So if your command name is `my-command`, the variable would be `_MY_COMMAND_COMPLETE=bash`,
 `_MY_COMMAND_COMPLETE=zsh`, or `_MY_COMMAND_COMPLETE=fish`, depending on your current shell.

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

### With an option

If you'd prefer not to use environment variables, you can add a special option to your command with
the [`completionOption`][completionOption] function. Invoking your program with this option will
generate the completion script:

=== "Example 1"
    ```kotlin
    class MyCommand: CliktCommand() {
        init {
            completionOption()
        }
        // ...
    }
    ```

=== "Example 2"
    ```kotlin
    class MyCommand: CliktCommand() {
        //..
    }
    
    fun main(args: Array<String>) = MyCommand().completionOption().main(args)
    ```

=== "Usage"
    ```text
    $ ./my-command --generate-completion=bash > ~/my-program-completion.sh
    ```

### With a subcommand

A third option is to add a subcommand that will generate the completion when invoked.

=== "Example 1"
    ```kotlin
    class MyCommand: CliktCommand() {
        init {
            subcommands(CompletionCommand())
        }
        // ...
    }
    ```

=== "Example 2"
    ```kotlin
    class MyCommand: CliktCommand() {
        //..
    }

    fun main(args: Array<String>) = MyCommand().subcommands(CompletionCommand()).main(args)
    ```

=== "Usage"
    ```text
    $ ./my-command generate-completion bash > ~/my-program-completion.sh
    ```

### Using the generated script

Once you've generated the completion script, source the file to activate completion:

```bash
$ source ~/my-program-completion.sh
```

You can add that source command to your startup script so that completion is always available. For
example, with bash:

```bash
$ echo source ~/my-program-completion.sh >> ~/.bashrc
```

You'll need to regenerate the completion script any time your command structure changes.

## Supported Functionality

### Bash and Zsh

Currently subcommand, option, and [command alias][command-aliases] names can be completed, as well as
values for options and arguments. `choice` parameters are completed with their possible values.
Other parameter types are completed as file or directory names.
[`Context.allowInterspersedArgs`][allowInterspersedArgs] is supported.

### Fish

Fish's completion mechanism is more limited that Bash's. Subcommands can be completed, options can
be completed as long as they start with a `-`. Completion suggestions for positional arguments are
the union of all positional arguments. Other advanced Clikt features are not supported. 

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
- `Custom`: Completions are generated from a custom script.

### `Custom` completion candidates

The `Custom` type takes a block that returns code to add to the script which generates completions
for the given parameter.

If you just want to call another script or binary that prints all possible completion words to
stdout, you can use [fromStdout].

Both Bash and ZSH scripts use Bash's Programmable Completion system (ZSH via a comparability layer).
The string returned from [generator] should be the body of a function that will be passed to
`compgen -F`.

Specifically, you should set the variable `COMPREPLY` to the completion(s) for the current word
being typed. The word being typed can be retrieved from the `COMP_WORDS` array at index
`COMP_CWORD`.

=== "Example with fromStdout"
    ```kotlin
    class Hello: CliktCommand() {
        // This example uses `echo`, but you would use your own binary
        // or script that prints the completions.
        val name by option(completionCandidates =
            CompletionCandidates.Custom.fromStdout("echo completion1 completion2")
        )
        override fun run() {
            echo("Hello, $name!")
        }
    }
    ```

=== "Example with full script"
    ```kotlin
    class Hello: CliktCommand() {
        // This is identical to the previous example
        val name by option(completionCandidates = CompletionCandidates.Custom {
            """
            WORDS=${'$'}(echo completion1 completion2)
            COMPREPLY=(${'$'}(compgen -W "${'$'}WORDS" -- "${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"))
            """.trimIndent()
        })
        override fun run() {
            echo("Hello, $name!")
        }
    }
    ```

## Limitations

[Token Normalization][token-normalization] is not supported.

If you have arguments that occur after a `multiple` argument, those arguments won't be
autocompleted. Partial command lines are ambiguous in those situations, and Clikt assumes that
you're trying to complete the `multiple` argument rather than the later ones.

Bash must be at least version 3, or Zsh must be at least version 4.1.


[allowInterspersedArgs]: api/clikt/com.github.ajalt.clikt.core/-context/allow-interspersed-args.html
[argument]:              api/clikt/com.github.ajalt.clikt.parameters.arguments/argument.html
[choice]:                api/clikt/com.github.ajalt.clikt.parameters.types/choice.html
[CliktCommand]:          api/clikt/com.github.ajalt.clikt.core/-clikt-command/index.html
[command-aliases]:       advanced.md#command-aliases
[file]:                  api/clikt/com.github.ajalt.clikt.parameters.types/file.html
[fromStdout]:            api/clikt/com.github.ajalt.clikt.completion/-completion-candidates/-custom/from-stdout/
[option]:                api/clikt/com.github.ajalt.clikt.parameters.options/option.html
[path]:                  api/clikt/com.github.ajalt.clikt.parameters.types/path.html
[token-normalization]:   advanced.md#token-normalization
[completionOption]:      api/clikt/com.github.ajalt.clikt.completion/completion-option.html
[CompletionCommand]:     api/clikt/com.github.ajalt.clikt.completion/-completion-command/
