[clikt](../../index.md) / [com.github.ajalt.clikt.core](../index.md) / [CliktCommand](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`CliktCommand(help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", epilog: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "", name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, invokeWithoutSubcommand: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, printHelpOnEmptyArgs: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, helpTags: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`> = emptyMap(), autoCompleteEnvvar: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = "")`

The [CliktCommand](index.md) is the core of command line interfaces in Clikt.

Command line interfaces created by creating a subclass of [CliktCommand](index.md) with properties defined with
[option](../../com.github.ajalt.clikt.parameters.options/option.md) and [argument](../../com.github.ajalt.clikt.parameters.arguments/argument.md). You can then parse `argv` by calling [main](main.md), which will take care of printing
errors and help to the user. If you want to handle output yourself, you can use [parse](parse.md) instead.

Once the command line has been parsed and all of the parameters are populated, [run](run.md) is called.

### Parameters

`help` - The help for this command. The first line is used in the usage string, and the entire string is
used in the help output. Paragraphs are automatically re-wrapped to the terminal width.

`epilog` - Text to display at the end of the full help output. It is automatically re-wrapped to the
terminal width.

`name` - The name of the program to use in the help output. If not given, it is inferred from the class
name.

`invokeWithoutSubcommand` - Used when this command has subcommands, and this command is called
without a subcommand. If true, [run](run.md) will be called. By default, a [PrintHelpMessage](../-print-help-message/index.md) is thrown instead.

`printHelpOnEmptyArgs` - If this command is called with no values on the command line, print a
help message (by throwing [PrintHelpMessage](../-print-help-message/index.md)) if this is true, otherwise run normally.

`helpTags` - Extra information about this option to pass to the help formatter.

`autoCompleteEnvvar` - The envvar to use to enable shell autocomplete script generation. Set
to null to disable generation.