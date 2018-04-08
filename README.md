<h1 align="center">
    <img src="docs/images/wordmark.svg">
    <p><img src="docs/images/animation.png"></p>
</h1>

Clikt *(pronounced "clicked")* is a Kotlin library for creating
beautiful command line interfaces in a composable way with as little
code as necessary. It's the "Command Line Interface for Kotlin". It's
highly configurable but comes with sensible defaults out of the box.

Inspired by Python's [Click](https://github.com/pallets/click), it is
designed to make the process of writing command line tools simple and
intuitive while supporting a wide variety of use cases and allowing
advanced customization when needed.

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

## Documentation

The full documentation can be found on [the website](https://ajalt.github.io/clikt).

There are also a number of [sample applications](samples/). You can run
them with the included [`runsample` script](runsample).

## Installation

Clikt is distributed with [jitpack](https://jitpack.io/#ajalt/clikt).

First, add Jitpack to your gradle repositories.

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

Then add the dependency on Clikt.

```groovy
dependencies {
   compile 'com.github.ajalt:clikt:1.0.0'
}
```

## License

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
