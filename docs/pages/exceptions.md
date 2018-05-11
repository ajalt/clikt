---
title: Exception Handling
sidebar: clikt_sidebar
permalink: exceptions.html
---

Clikt uses exceptions internally to signal that processing has ended
early for any reason. This includes incorrect command line usage, or
printing a help page.

## Where are Exceptions Handled? {#handling}

When you call {% include api.html pkg="core" class="clikt-command"
fun="main" %}, it will parse the command line and catch any {% include
api.html pkg="core" class="clikt-error" %} and {% include api.html
pkg="core" class="abort" %} exceptions. If it catches one, it will then
print out the appropriate information and exit the process. If the
caught exception is a {% include api.html pkg="core"
class="print-message" %} or {% include api.html pkg="core"
class="print-help-message" %}, the process exit status will be 0 and the
message will be printed to stdout. Otherwise it will exit with status 1
and print the message to stderr.

Any other types of exceptions indicate a programming error, and are not
caught by {% include api.html pkg="core" class="clikt-command"
fun="main" text="main" %}. However, {% include api.html
pkg="parameters.options" fun="convert" %} and the other parameter
transformations will wrap exceptions thrown inside them in a {% include
api.html pkg="core" class="usage-error" %}, so if you define a custom
transformation, you don't have to worry about an exception escaping to
the user.

## Handling Exceptions Manually

{% include api.html pkg="core" class="clikt-command" fun="main" %} is
just a `try`/`catch` block surrounding {% include api.html pkg="core"
class="clikt-command" fun="parse" %}, so if don't want exceptions to be
caught, you can call {% include api.html pkg="core"
class="clikt-command" fun="parse" text="parse" %} wherever you would
normally call {% include api.html pkg="core" class="clikt-command"
fun="main" text="main" %}.

```kotlin
fun main(args: Array<String>) = Cli().parse(args)
```

## Which Exceptions Exist? {#hierarchy}

Clikt will throw {% include api.html pkg="core" class="abort" %} if
it needs to halt execution immediately without a specific message. All
other exceptions are subclasses of {% include api.html pkg="core"
class="usage-error" %}.

The following subclasses exist:

* {% include api.html pkg="core" class="print-message" %} : The exception's message should be printed.
* {% include api.html pkg="core" class="print-help-message" %} : The help page for the exception's command should be printed.
* {% include api.html pkg="core" class="usage-error" %} : The command line was incorrect in some way. All other exceptions subclass from this. These exceptions are automatically augmented with extra information about the current parameter, if possible.
* {% include api.html pkg="core" class="bad-parameter-value" %} : A parameter was given the correct number of values, but of invalid format or type.
* {% include api.html pkg="core" class="missing-parameter" %} : A required parameter was not provided.
* {% include api.html pkg="core" class="no-such-option" %} : An option was provided that does not exist.
* {% include api.html pkg="core" class="incorrect-option-value-count" %} : An option was supplied but the number of values supplied to the option was incorrect.
* {% include api.html pkg="core" class="incorrect-argument-value-count" %} : An argument was supplied but the number of values supplied was incorrect.


{% include links.html %}
