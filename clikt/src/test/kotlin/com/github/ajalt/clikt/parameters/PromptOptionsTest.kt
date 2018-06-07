package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.contrib.java.lang.system.TextFromStandardInputStream

class PromptOptionsTest {
    @Rule
    @JvmField
    val stdout = SystemOutRule().enableLog().muteForSuccessfulTests()

    @Rule
    @JvmField
    val stdin = TextFromStandardInputStream.emptyStandardInputStream()

    @Test
    fun `manual prompt`() {
        stdin.provideLines("bar")
        val input = TermUi.prompt("Foo")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: "
        input shouldBe "bar"
    }

    @Test
    fun `manual prompt conversion`() {
        stdin.provideLines("bar", "11")
        val input = TermUi.prompt("Foo") { it.toIntOrNull() ?: throw UsageError("boo") }
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: Error: boo\nFoo: "
        input shouldBe 11
    }

    @Test
    fun `prompt option`() {
        stdin.provideLines("bar")

        class C : CliktCommand() {
            val foo by option().prompt()
            override fun run() {
                foo shouldBe "bar"
            }
        }
        C().parse(emptyArray())
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: "
    }

    @Test
    fun `prompt custom name`() {
        stdin.provideLines("foo")

        class C : CliktCommand() {
            val foo by option().prompt("INPUT")
            override fun run() {
                foo shouldBe "foo"
            }
        }
        C().parse(emptyArray())
        stdout.logWithNormalizedLineSeparator shouldBe "INPUT: "
    }

    @Test
    fun `prompt inferred names`() {
        stdin.provideLines("foo", "bar", "baz")

        class C : CliktCommand() {
            val foo by option().prompt()
            val bar by option("/bar").prompt()
            val baz by option("--some-thing").prompt()
            override fun run() {
                foo shouldBe "foo"
                bar shouldBe "bar"
                baz shouldBe "baz"
            }
        }
        C().parse(emptyArray())
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: Bar: Some thing: "
    }

    @Test
    fun `prompt two options`() {
        stdin.provideLines("foo", "bar")

        class C : CliktCommand() {
            val foo by option().prompt()
            val bar by option().prompt()
            override fun run() {
                foo shouldBe "foo"
                bar shouldBe "bar"
            }
        }
        C().parse(emptyArray())
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: Bar: "
    }

    @Test
    fun `prompt default`() {
        stdin.provideLines("bar")

        class C : CliktCommand() {
            val foo by option().prompt(default = "baz")
            override fun run() {
                foo shouldBe "bar"
            }
        }

        C().parse(emptyArray())
        stdout.logWithNormalizedLineSeparator shouldBe "Foo [baz]: "
    }

    @Test
    fun `prompt default no stdin`() {
        stdin.provideLines("")

        class C : CliktCommand() {
            val foo by option().prompt(default = "baz")
            override fun run() {
                foo shouldBe "baz"
            }
        }

        C().parse(emptyArray())
    }
}
