<h1 align="center">
    <img src="docs/img/wordmark.svg">
    <p><img src="docs/img/animation.png"></p>
</h1>

Clikt *(pronounced "clicked")* is a multiplatform Kotlin library that makes writing command line
interfaces simple and intuitive. It's the "Command Line Interface for Kotlin".

It is designed to make the process of writing command line tools effortless
while supporting a wide variety of use cases and allowing advanced
customization when needed.

Clikt has:

 * arbitrary nesting of commands
 * composable, type safe parameter values
 * generation of help output and shell autocomplete scripts
 * multiplatform packages for JVM, Node.js, and native Linux, Windows and macOS 

What does it look like? Here's a complete example of a simple Clikt program:

```kotlin
class Hello : CliktCommand() {
    val count: Int by option().int().default(1).help("Number of greetings")
    val name: String by option().prompt("Your name").help("The person to greet")

    override fun run() {
        repeat(count) {
            echo("Hello $name!")
        }
    }
}

fun main(args: Array<String>) = Hello().main(args)
```

And here's what it looks like when run:

<p align="center"><img src="docs/img/readme_screenshot1.png"></p>

The help page is generated for you:

<p align="center"><img src="docs/img/readme_screenshot2.png"></p>

Errors are also taken care of:

<p align="center"><img src="docs/img/readme_screenshot3.png"></p>


## Documentation

The full documentation can be found on [the website](https://ajalt.github.io/clikt).

There are also a number of [sample applications](samples). You can run
them with the included [`runsample` script](runsample).

## Installation

Clikt is distributed through [Maven Central](https://search.maven.org/artifact/com.github.ajalt.clikt/clikt).

```kotlin
dependencies {
   implementation("com.github.ajalt.clikt:clikt:5.0.2")

   // optional support for rendering markdown in help messages
   implementation("com.github.ajalt.clikt:clikt-markdown:5.0.2")
}
```

There is also a smaller core module available. [See the docs for details](https://ajalt.github.io/clikt/advanced/#core-module).


###### If you're using Maven instead of Gradle, use `<artifactId>clikt-jvm</artifactId>`

#### Multiplatform

Clikt supports most multiplatform targets.
[See the docs](https://ajalt.github.io/clikt/advanced/#multiplatform-support) 
for more information about functionality supported on each target. You'll need to use Gradle 6 or
newer.

#### Snapshots

<details>
<summary>Snapshot builds are also available</summary>
   
<a href="https://oss.sonatype.org/content/repositories/snapshots/com/github/ajalt/clikt/clikt/"><img src="https://img.shields.io/nexus/s/com.github.ajalt.clikt/clikt?color=blue&label=latest%20shapshot&server=https%3A%2F%2Foss.sonatype.org"/></a>
   
<p>
You'll need to add the Sonatype snapshots repository: 
      
```kotlin
repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}
```
</p>
</details>

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
