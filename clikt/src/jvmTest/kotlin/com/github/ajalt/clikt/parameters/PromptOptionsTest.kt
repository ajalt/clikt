package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.contrib.java.lang.system.TextFromStandardInputStream
import kotlin.test.Test

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
    fun `command prompt`() {
        stdin.provideLines("bar")

        class C: TestCommand() {
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

        class C: TestCommand() {
            override fun run_() {
                confirm("Foo") shouldBe true
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
    fun `custom console`() {
        val console = object : CliktConsole {
            val prompts = mutableListOf<String>()
            val prints = mutableListOf<String>()

            override fun promptForLine(prompt: String, hideInput: Boolean): String? {
                prompts += prompt
                return "bar"
            }

            override fun print(text: String, error: Boolean) {
                prints += text
            }

            override val lineSeparator: String get() = "\n"
        }

        class C : TestCommand() {
            init {
                context {
                    this.console = console
                }
            }

            val foo by option().prompt()
            override fun run_() {
                foo shouldBe "bar"
            }
        }
        C().parse("")
        console.prompts should containExactly("Foo: ")
        console.prints should beEmpty()
        stdout.logWithNormalizedLineSeparator shouldBe ""
    }

    @Test
    fun `custom console inherited by subcommand`() {
        val console = object : CliktConsole {
            val prompts = mutableListOf<String>()
            val prints = mutableListOf<String>()

            override fun promptForLine(prompt: String, hideInput: Boolean): String? {
                prompts += prompt
                return "bar"
            }

            override fun print(text: String, error: Boolean) {
                prints += text
            }

            override val lineSeparator: String get() = "\n"
        }

        class C : TestCommand() {
            init {
                context {
                    this.console = console
                }
            }

            override fun run_() {}
        }

        class S : TestCommand() {
            val foo by option().prompt()
            override fun run_() {
                foo shouldBe "bar"
            }
        }

        C().subcommands(S()).parse(listOf("s"))
        console.prompts should containExactly("Foo: ")
        console.prints should beEmpty()
        stdout.logWithNormalizedLineSeparator shouldBe ""
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
    fun `two options`() {
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
    fun default() {
        stdin.provideLines("bar")

        class C : TestCommand() {
            val foo by option().prompt(default = "baz")
            override fun run_() {
                foo shouldBe "bar"
            }
        }

        C().parse("")
        stdout.logWithNormalizedLineSeparator shouldBe "Foo [baz]: "
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
