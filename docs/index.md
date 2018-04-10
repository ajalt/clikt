---
sidebar: clikt_sidebar
permalink: index.html
topnav: topnav
---

<div align="center" style="margin-bottom:42px;">
{% include image.html file="wordmark.svg" max-width="700" %}
</div>

Clikt *(pronounced "clicked")* is a Kotlin library that makes writing
command line interfaces simple and intuitive. It the "Command Line
Interface for Kotlin".

Inspired by Python's [Click](https://github.com/pallets/click), it is
designed to make the process of writing command line tools efortless
while supporting a wide variety of use cases and allowing advanced
customization when needed.

Clikt has:

 * arbitrary nesting of commands
 * composable, type safe parameter values
 * support for a wide variety of command line interface styles

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

# License

    Copyright 2018 AJ Alt

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

{% include links.html %}
