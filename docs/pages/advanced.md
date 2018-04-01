---
title: Advanced Patters
sidebar: home_sidebar
permalink: advanced.html
---

<!--  TODO: add docs links -->

Clikt has reasonable behavior by default, but is also very customizable
for advanced use cases.

## Command Aliases

Clikt allows commands to alias command names to sequences of tokens.
This allows you to implement common patterns like allowing the user to
invoke a command by typing a prefix of its name, or user-defined aliases
like the way you can configure git to accept `git ci` as an alias for
`git commit`.

To implement command aliases, override `CliktCommand.aliases()` in your
command. This function is called once at the start of parsing, and
return a map of aliases to the tokens that they alias to.

To implement git-style aliases:

```kotlin
class Repo : NoRunCliktCommand() {
    // You could load the aliases from a config file etc.
    override fun aliases(): Map<String, List<String>> = mapOf(
            "ci" to listOf("commit"),
            "cm" to listOf("commit", "-m")
    )
}

class Commit: CliktCommand() {
    val message by option("-m").default("")
    override fun run() {
        TermUi.echo("Committing with message: $message")
    }
}

fun main(args: Array<String>) = Repo().subcommands(Commit()).main(args)
```

And on the comand line:

```
$ ./repo ci -m 'my message'
Committing with message: my message
```

```
$ ./repo cm 'my message'
Committing with message: my message
```

Note that aliases are not expanded recursively: none of the tokens that
an alias expands to will be expanded again, even if they match another
alias.

You also use this fucionality to implement command prefixes:

```kotlin
class Tool : CliktCommand() {
    override fun aliases(): Map<String, List<String>> {
        val prefixCounts = mutableMapOf<String, Int>().withDefault { 0 }
        val prefixes = mutableMapOf<String, List<String>>()
        for (name in registeredSubcommandNames()) {
            if (name.length < 3) continue
            for (i in 1..name.lastIndex) {
                val prefix = name.substring(0..i)
                prefixCounts[prefix] = prefixCounts.getValue(prefix) + 1
                prefixes[prefix] = listOf(name)
            }
        }
        return prefixes.filterKeys { prefixCounts.getValue(it) == 1 }
    }

    override fun run() = Unit
}

class Foo: CliktCommand() {
    override fun run() {
        TermUi.echo("Running Foo")
    }
}

class Bar: CliktCommand() {
    override fun run() {
        TermUi.echo("Running Bar")
    }
}

fun main(args: Array<String>) = Tool().subcommands(Foo(), Bar()).main(args)
```

Which alows you to call the subcommands like this:

```
$ ./tool ba
Running Bar
```

## Token Normalization

To prevent ambiguities in parsing, aliases are only supported for
command names. However, there's another way to modify user input that
works on more types of tokens. You can set a `tokenTransformer` on the
[command's context](commands.html#customcontext) that will be called for
each option and command name that is input. This can be used to
implement case-insenitive parsing, for example:

```kotlin
class Hello : CliktCommand() {
    init {
        context { tokenTransformer = { it.toLowerCase() } }
    }

    val name by option()
    override fun run() = TermUi.echo("Hello $name!")
}
```

```
$ ./hello --NAME=Foo
Hello Foo!
```


{% include links.html %}
