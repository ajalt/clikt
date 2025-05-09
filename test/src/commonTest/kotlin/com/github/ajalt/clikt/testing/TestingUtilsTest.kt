package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.mordant.rendering.AnsiLevel
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class TestingUtilsTest {
    @[Test JsName("testing_envvar")]
    fun `testing envvar`() {
        class C : TestCommand(called = false) {
            val o1 by option()
            val o2 by option(envvar = "O")

            override fun run_() {
                o1 shouldBe "foo"
                o2 shouldBe "bar"
                echo("$o1")
                echo("$o2")
            }
        }

        val result = C().test("--o1=foo", envvars = mapOf("O" to "bar"))
        result.stdout shouldBe "foo\nbar\n"
        result.stderr shouldBe ""
        result.output shouldBe "foo\nbar\n"
        result.statusCode shouldBe 0
    }

    @[Test JsName("testing_error")]
    fun `testing error`() {
        @Suppress("unused")
        class C : TestCommand(called = false) {
            val o by option()
        }

        val ex = """
            |Usage: c [<options>]
            |
            |Error: no such option --foo. Did you mean --o?
            |
        """.trimMargin()
        val result = C().test("--foo bar", stdin = "unused")
        result.stdout shouldBe ""
        result.stderr shouldBe ex
        result.output shouldBe ex
        result.statusCode shouldBe 1
    }

    @[Test JsName("testing_with_prompt")]
    fun `testing with prompt`() {
        class C : TestCommand() {
            val o1 by option().prompt()
            val o2 by option().prompt()

            override fun run_() {
                o1 shouldBe "foo"
                o2 shouldBe "bar"
                echo("err", err = true)
            }
        }

        val result = C().test("", stdin = "foo\nbar")
        result.stdout shouldBe "O1: O2: "
        result.stderr shouldBe "err\n"
        result.output shouldBe "O1: O2: err\n"
        result.statusCode shouldBe 0
    }

    @[Test JsName("test_TerminalInfo_configuration")]
    fun `test TerminalInfo configuration`() {
        class C : TestCommand() {
            override fun run_() {
                with(currentContext.terminal) {
                    terminalInfo.ansiLevel shouldBe AnsiLevel.NONE
                    size.width shouldBe 11
                    size.height shouldBe 22
                    terminalInfo.ansiHyperLinks shouldBe true
                    terminalInfo.outputInteractive shouldBe true
                    terminalInfo.inputInteractive shouldBe true
                }
            }
        }
        C().test(
            "",
            ansiLevel = AnsiLevel.NONE,
            width = 11,
            height = 22,
            hyperlinks = true,
            outputInteractive = true,
            inputInteractive = true,
        )
    }
}
