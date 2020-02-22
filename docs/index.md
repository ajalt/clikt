---
title: 'Clikt: Multiplatform command line parser for Kotlin'
---

<div align="center" style="margin-bottom:42px;max-width:700px">
    <img alt="wordmark" src="img/wordmark.svg" />
</div>

Clikt *(pronounced "clicked")* is a multiplatform Kotlin library that makes writing command line
interfaces simple and intuitive. It's the "Command Line Interface for Kotlin".

It is designed to make the process of writing command line tools effortless
while supporting a wide variety of use cases and allowing advanced
customization when needed.

Clikt has:

 * arbitrary nesting of commands
 * composable, type safe parameter values
 * support for a wide variety of command line interface styles
 * multiplatform packages for JVM, NodeJS, and native Linux, Windows and MacOS 

 What does it look like? Here's a complete example of a simple Clikt program:

```kotlin
class Hello : CliktCommand() {
    val count: Int by option(help="Number of greetings").int().default(1)
    val name: String by option(help="The person to greet").prompt("Your name")

    override fun run() {
        repeat(count) {
            echo("Hello $name!")
        }
    }
}

fun main(args: Array<String>) = Hello().main(args)
```

And here's what it looks like when run:

```
 $ ./hello --count=3
 Your name: John
 Hello John!
 Hello John!
 Hello John!
```

The help page is generated for you:

```
$ ./hello --help
Usage: hello [OPTIONS]

Options:
  --count INT  Number of greetings
  --name TEXT  The person to greet
  -h, --help   Show this message and exit
```

Errors are also taken care of:

```
$ ./hello --whoops
Usage: hello [OPTIONS]

Error: no such option: "--whoops".
```

# API Reference

* [Commands and Exceptions](api/clikt/com.github.ajalt.clikt.core/index.md)
* [Options](api/clikt/com.github.ajalt.clikt.parameters.options/index.md)
* [Arguments](api/clikt/com.github.ajalt.clikt.parameters.arguments/index.md)
* [Parameter Type Conversions](api/clikt/com.github.ajalt.clikt.parameters.types/index.md)
* [Output Formatting](api/clikt/com.github.ajalt.clikt.output/index.md)
