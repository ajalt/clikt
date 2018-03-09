package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class PromptOptionsTest {
    private fun setup(stdin: String): ByteArrayOutputStream {
        System.setIn(ByteArrayInputStream(stdin.toByteArray()))
        return ByteArrayOutputStream().also { System.setOut(PrintStream(it)) }
    }

    @Test
    fun `prompt`() {
        val stdout = setup(stdin = "bar\n")

        class C : CliktCommand() {
            val foo by option().prompt()
            override fun run() {
                assertThat(foo).isEqualTo("bar")
            }
        }
        C().parse(emptyArray())
        assertThat(String(stdout.toByteArray())).isEqualTo("Foo: ")
    }

    @Test
    fun `prompt default`() {
        val stdout = setup(stdin = "bar\n")

        class C : CliktCommand() {
            val foo by option().prompt(default = "baz")
            override fun run() {
                assertThat(foo).isEqualTo("bar")
            }
        }

        C().parse(emptyArray())
        assertThat(String(stdout.toByteArray())).isEqualTo("Foo [baz]: ")
    }

    @Test
    fun `prompt default no stdin`() {
        setup(stdin = "\n")

        class C : CliktCommand() {
            val foo by option().prompt(default = "baz")
            override fun run() {
                assertThat(foo).isEqualTo("baz")
            }
        }

        C().parse(emptyArray())
    }
}
