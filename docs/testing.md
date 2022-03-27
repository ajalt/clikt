# Testing Clikt Commands

Clikt includes the [`test`][test] extension to help testing commands and their output.

=== "Test"
    ```kotlin
    @Test
    fun testHello() {
        val command = Hello()
        val result = command.test("--name Foo")
        assertEqual(result.stdout, "Hello, Foo!")
        assertEqual(result.exitCode, 0)
        assertEqual(command.name, "Foo")
    }
    ```

=== "Command"
    ```kotlin
    class Hello: CliktCommand() {
        val name by option()
        override fun run() {
            echo("Hello, $name!")
        }
    }
    ```

Calling `test` will run the command with the given arguments and return a [result][test-result] object that contains the
captured outputs and result status code. You can check the captured output with the `stdout` property of the result,
errors output with `stderr`, or both combined in with `output`.

!!! caution

    Output printed with Kotlin's `print` and `println` functions are not captured. 
    Use `echo` instead.

## Testing Environment Variables

You can set environment variables for your command by passing in a map of `envvars`.

=== "Test"
    ```kotlin
    @Test
    fun testHello() {
        val command = Hello()
        val result = command.test(""", envvars=mapOf("HELLO_NAME" to "Foo"))
        assertEqual(result.stdout, "Hello, Foo!")
    }
    ```

=== "Command"
    ```kotlin
    class Hello: CliktCommand() {
        val name by option(envvar="HELLO_NAME")
        override fun run() {
            echo("Hello, $name!")
        }
    }
    ```

To keep tests reproducible, only the envvar values you provide to `test` are visible to the command. To include system
envvars as well, pass `includeSystemEnvvars=true` to `test`.

## Testing Prompt Options

If you use [`prompt`][prompt] options, you can use the `stdin` parameter of `test` to pass a string containg all the
lines of input. If you have multiple prompts, each input should be separated by `\n`.

=== "Test"
    ```kotlin
    @Test
    fun testAdder() {
        val command = Adder()
        val result = command.test("", stdin = "2\n3")
        assertEqual(result.stdout, "first: second: result: 2 + 3 = 5")
    }
    ```

=== "Command"
    ```kotlin
    class Adder : TestCommand() {
        val first by option().prompt()
        val second by option().prompt()
    
        override fun run_() {
            echo("result: $first + $second = ${first + second}")
        }
    }
    ```

## Custom Testing

If the `test` helper doesn't cover all the use cases you need to test, you can run your command yourself. 

In unit tests, you won't want to use [`CliktCommand.main`][main], since it calls `exitProcess` when errors occur.
Instead, you should instead use [`CliktCommand.parse`][parse], which throws exceptions with error details rather than
printing the details and exiting the process. See the documentation on [exceptions](exceptions.md) for more information
on the exceptions that can be thrown.

If your command uses environment variables, you can [configure the context][override-envvar]
to return test values for them.

To capture output, [override the command's console][replacing-stdin].

[main]:                api/clikt/com.github.ajalt.clikt.core/-clikt-command/main.html
[override-envvar]:     options.md#overriding-system-environment-variables
[parse]:               api/clikt/com.github.ajalt.clikt.core/-clikt-command/parse.html
[replacing-stdin]:     advanced.md#replacing-stdin-and-stdout
[test-result]:         api/clikt/com.github.ajalt.clikt.testing/-clikt-command-test-result/index.html
[test]:                api/clikt/com.github.ajalt.clikt.testing/test.html
