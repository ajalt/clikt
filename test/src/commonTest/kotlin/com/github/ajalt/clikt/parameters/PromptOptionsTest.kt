package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.nullableFlag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.test
import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.YesNoPrompt
import com.github.ajalt.mordant.terminal.prompt
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused")
class PromptOptionsTest {
    @[Test JsName("command_prompt")]
    fun `command prompt`() {
        class C : TestCommand() {
            override fun run_() {
                terminal.prompt("Foo") shouldBe "bar"
                terminal.prompt("Baz") { ConversionResult.Valid(it.toInt()) } shouldBe 1
            }
        }
        C().test("", stdin = "bar\n1").output shouldBe "Foo: Baz: "
    }

    @[Test JsName("command_confirm")]
    fun `command confirm`() {
        class C : TestCommand() {
            override fun run_() {
                YesNoPrompt("Foo", terminal, default = false).ask() shouldBe true
            }
        }
        C().test("", stdin = "y").output shouldBe "Foo [y/N]: "
    }

    @[Test JsName("prompt_option")]
    fun `prompt option`() {
        class C : TestCommand() {
            val foo by option().prompt()
            val bar by option().prompt()
            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe "bar"
            }
        }
        C().test("", stdin = "foo\nbar").output shouldBe "Foo: Bar: "
    }

    @[Test JsName("prompt_option_after_error")]
    fun `prompt option after error`() {
        class C : TestCommand(false) {
            val foo by option().int()
            val bar by option().prompt()
        }

        val result = C().test("--foo=x")
        result.stdout shouldBe ""
        result.stderr shouldContain "invalid value for --foo: x is not a valid integer"
    }

    @[Test JsName("prompt_option_requireConfirmation")]
    fun `prompt option requireConfirmation`() {
        class C : TestCommand() {
            val foo by option().prompt(requireConfirmation = true)
            override fun run_() {
                foo shouldBe "foo"
            }
        }
        C().test("", stdin = "foo\nfoo").output shouldBe "Foo: Repeat for confirmation: "
    }

    @[Test JsName("prompt_flag")]
    fun `prompt flag`() {
        class C : TestCommand() {
            val foo by option().nullableFlag().prompt()
            val bar by option().nullableFlag().prompt()
            val baz by option().nullableFlag()
            override fun run_() {
                foo shouldBe true
                bar shouldBe false
                baz shouldBe null
            }
        }
        C().test("", stdin = "yes\nf").output shouldBe "Foo: Bar: "
    }

    @[Test JsName("prompt_option_validate")]
    fun `prompt option validate`() {
        class C : TestCommand() {
            val foo by option().prompt().check { it.length > 1 }
            override fun run_() {
                foo shouldBe "foo"
            }
        }
        C().test("", stdin = "f\nfoo").output shouldBe "Foo: invalid value for --foo: f\nFoo: "
    }

    @[Test JsName("custom_console_inherited_by_subcommand")]
    fun `custom console inherited by subcommand`() {
        class C : TestCommand() {
            val foo by option().prompt()
            override fun run_() {
                foo shouldBe "bar"
            }
        }

        val r = TestCommand().subcommands(C()).test("c", stdin = "bar")
        r.output shouldBe "Foo: "
    }

    @[Test JsName("custom_name")]
    fun `custom name`() {
        class C : TestCommand() {
            val foo by option().prompt("INPUT")
            override fun run_() {
                foo shouldBe "foo"
            }
        }
        C().test("", stdin = "foo").output shouldBe "INPUT: "
    }

    @[Test JsName("inferred_names")]
    fun `inferred names`() {
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
        C().test("", stdin = "foo\nbar\nbaz").output shouldBe "Foo: Bar: Some thing: "
    }

    @[Test JsName("prompt_default")]
    fun `prompt default`() {
        class C : TestCommand() {
            val foo by option().prompt(default = "baz")
            override fun run_() {
                foo shouldBe "bar"
            }
        }

        C().test("", stdin = "bar").output shouldBe "Foo (baz): "
    }

    @[Test JsName("prompt_default_no_stdin")]
    fun `prompt default no stdin`() {
        class C : TestCommand() {
            val foo by option().prompt(default = "baz")
            override fun run_() {
                foo shouldBe "baz"
            }
        }

        C().test("").output shouldBe "Foo (baz): "
    }
}
