---
sidebar: clikt_sidebar
permalink: index.html
topnav: topnav
---

<div align="center" style="margin-bottom:42px;">
{% include image.html file="wordmark.svg" max-width="700" %}
</div>

Clikt *(pronounced "clicked")* is a Kotlin library for creating
beautiful command line interfaces in a composable way with as little
code as necessary. It's the "Command Line Interface for Kotlin". It’s
highly configurable but comes with sensible defaults out of the box.

It aims to make the process of writing command line tools simple and
intuitive while supporting a wide variety of use cases, and allowing
advanced customization when needed.

Clikt has:

 * arbitrary nesting of commands
 * composable, type safe parameter values
 * automatic help page generation

 What does it look like? Here's a complete example of a simple Clikt
 program:

 ```kotlin
class Hello : CliktCommand() {
    val count: Int by option(help="Number of greetings").int().default(1)
    val name: String by option(help="The person to greet").prompt("Your name")

    override fun run() {
        for (i in 1..count) {
            TermUi.echo("Hello $name!")
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

# Documentation Contents

{% include toc.html %}

# API Reference

* [Commands and Exceptions](api/clikt/com.github.ajalt.clikt.core/index.html)
* [Options](api/clikt/com.github.ajalt.clikt.parameters.options/index.html)
* [Arguments](api/clikt/com.github.ajalt.clikt.parameters.arguments/index.html)
* [Parameter Type Conversions](api/clikt/com.github.ajalt.clikt.parameters.types/index.html)
* [Output Formatting](api/clikt/com.github.ajalt.clikt.output/index.html)

{% include links.html %}
