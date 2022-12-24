package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test


class AtFileTest {
    private fun <T : CliktCommand> T.withAtFiles(vararg content: Pair<String, String>): T {
        return context { argumentFileReader = { content.toMap().getValue(it).trimMargin() } }
    }

    @Test
    @JsName("parsing_atfile")
    fun `parsing atfile`() {
        class C : TestCommand() {
            val foo by option()
            val bar by option()
            val arg1 by argument()
            val arg2 by argument()
            val arg3 by argument()
            val arg4 by argument()
            val arg5 by argument()
            val arg6 by argument()

            override fun run_() {
                foo shouldBe "123"
                bar shouldBe "a b \"'"
                arg1 shouldBe "\\"
                arg2 shouldBe ""
                arg3 shouldBe "#"
                arg4 shouldBe "ab"
                arg5 shouldBe "cd"
                arg6 shouldBe "e\nf"
            }
        }


        C().withAtFiles(
            "foo" to """
        |--foo 123 # comment
        |--bar='a b "\''
        |\\ "" \# #
        |a\
        | b
        |c\
        |d
        |'e
        |f'
        """
        ).parse("@foo")
    }

    @Test
    @JsName("parsing_atfile_recursive")
    fun `parsing atfile recursive`() {
        class C : TestCommand() {
            val foo by option()
            val arg by argument()

            override fun run_() {
                foo shouldBe "123"
                arg shouldBe "456"
            }
        }

        C().withAtFiles(
            "foo" to "--foo 123 456",
            "bar" to "@foo"
        ).parse("@foo")
    }

    @Test
    @JsName("parsing_atfile_unclosed_quotes")
    @Suppress("unused")
    fun `parsing atfile unclosed quotes`() {
        class C : TestCommand(called = false) {
            val arg by argument()
        }

        shouldThrow<UsageError> {
            C().withAtFiles(
                "foo" to """
                |'a b "\'
                |
                """
            ).parse("@foo")
        }.message shouldContain "unclosed quote"
    }

    @Test
    @JsName("parsing_atfile_after_dash")
    fun `passing atfile after --`() {
        class C : TestCommand() {
            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().withAtFiles().parse("-- @file")
    }

    @Test
    @JsName("escaping_atfile")
    fun `escaping atfile`() {
        class C : TestCommand() {
            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().withAtFiles().parse("@@file")
    }

    @Test
    @JsName("atfile_after_arg")
    fun `atfile after arg`() {
        class C(a: Boolean) : TestCommand() {
            init {
                context {
                    allowInterspersedArgs = a
                }
            }

            val arg by argument().multiple()

            override fun run_() {
                arg shouldBe listOf("foo", "bar")
            }
        }

        C(true).withAtFiles("baz" to "bar").parse("foo @baz")
        C(false).withAtFiles("baz" to "bar").parse("foo @baz")
    }

    @Test
    @JsName("disabling_atfile")
    fun `disabling atfile`() {
        class C : TestCommand() {
            init {
                context {
                    expandArgumentFiles = false
                }
            }

            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().parse("@file")
    }
}
