package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.contrib.java.lang.system.TextFromStandardInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class PromptOptionsTest {
    @Rule
    @JvmField
    val stdout = SystemOutRule().enableLog()

    @Rule
    @JvmField
    val stdin = TextFromStandardInputStream.emptyStandardInputStream()

    @Test
    fun `prompt`() {
        stdin.provideLines("bar")

        class C : CliktCommand() {
            val foo by option().prompt()
            override fun run() {
                assertThat(foo).isEqualTo("bar")
            }
        }
        C().parse(emptyArray())
        assertThat(stdout.logWithNormalizedLineSeparator).isEqualTo("Foo: ")
    }

    @Test
    fun `prompt two options`() {
        stdin.provideLines("foo", "bar")

        class C : CliktCommand() {
            val foo by option().prompt()
            val bar by option().prompt()
            override fun run() {
                assertThat(foo).isEqualTo("foo")
                assertThat(bar).isEqualTo("bar")
            }
        }
        C().parse(emptyArray())
        assertThat(stdout.logWithNormalizedLineSeparator).isEqualTo("Foo: Bar: ")
    }

    @Test
    fun `prompt default`() {
        stdin.provideLines("bar")

        class C : CliktCommand() {
            val foo by option().prompt(default = "baz")
            override fun run() {
                assertThat(foo).isEqualTo("bar")
            }
        }

        C().parse(emptyArray())
        assertThat(stdout.logWithNormalizedLineSeparator).isEqualTo("Foo [baz]: ")
    }

    @Test
    fun `prompt default no stdin`() {
        stdin.provideLines("")

        class C : CliktCommand() {
            val foo by option().prompt(default = "baz")
            override fun run() {
                assertThat(foo).isEqualTo("baz")
            }
        }

        C().parse(emptyArray())
    }
}
