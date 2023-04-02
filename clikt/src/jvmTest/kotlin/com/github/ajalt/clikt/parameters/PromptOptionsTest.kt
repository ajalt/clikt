package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import com.github.ajalt.clikt.testing.test
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.contrib.java.lang.system.TextFromStandardInputStream
import kotlin.test.Test

class PromptOptionsTest {
    @Rule
    @JvmField
    val stdout: SystemOutRule = SystemOutRule().enableLog().muteForSuccessfulTests()

    @Rule
    @JvmField
    val stdin: TextFromStandardInputStream = TextFromStandardInputStream.emptyStandardInputStream()

    @Test
    fun `command prompt`() {
        stdin.provideLines("bar")

        class C : TestCommand() {
            override fun run_() {
                prompt("Foo") shouldBe "bar"
            }
        }
        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: "
    }

    @Test
    fun `command confirm`() {
        stdin.provideLines("y")

        class C : TestCommand() {
            override fun run_() {
                confirm("Foo", default=false) shouldBe true
            }
        }
        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo [y/N]: "
    }

    @Test
    fun `prompt option`() {
        stdin.provideLines("foo", "bar")

        class C : TestCommand() {
            val foo by option().prompt()
            val bar by option().prompt()
            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe "bar"
            }
        }
        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: Bar: "
    }

    @Test
    fun `prompt option validate`() {
        stdin.provideLines("f", "foo")

        class C : TestCommand() {
            val foo by option().prompt().check { it.length > 1 }
            override fun run_() {
                foo shouldBe "foo"
            }
        }
        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: Invalid value for \"--foo\": f\nFoo: "
    }


    @Test
    fun `custom console`() {
        class C : TestCommand() {
            val foo by option().prompt()
            override fun run_() {
                foo shouldBe "bar"
            }
        }
        val r = C().test("", stdin="bar")
        r.output shouldBe "Foo: "
    }

    @Test
    fun `custom console inherited by subcommand`() {
        class C : TestCommand()

        class S : TestCommand() {
            val foo by option().prompt()
            override fun run_() {
                foo shouldBe "bar"
            }
        }

        val r= C().subcommands(S()).test("s", stdin="bar")
        r.output shouldBe "Foo: "
    }

    @Test
    fun `custom name`() {
        stdin.provideLines("foo")

        class C : TestCommand() {
            val foo by option().prompt("INPUT")
            override fun run_() {
                foo shouldBe "foo"
            }
        }
        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "INPUT: "
    }

    @Test
    fun `inferred names`() {
        stdin.provideLines("foo", "bar", "baz")

        class C : TestCommand() {
            val foo by option().prompt()
            val bar by option("/bar").prompt()
            val baz by option("--some-thing").prompt()
            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe "bar"
                baz shouldBe "baz"
            }
        }
        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo: Bar: Some thing: "
    }

    @Test
    fun default() {
        stdin.provideLines("bar")

        class C : TestCommand() {
            val foo by option().prompt(default = "baz")
            override fun run_() {
                foo shouldBe "bar"
            }
        }

        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo (baz): "
    }

    @Test
    fun `default no stdin`() {
        stdin.provideLines("")

        class C : TestCommand() {
            val foo by option().prompt(default = "baz")
            override fun run_() {
                foo shouldBe "baz"
            }
        }

        C().parse("")
    }
}
