package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
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
        return context { readArgumentFile = { content.toMap().getValue(it).trimMargin() } }
    }

    @[Test JsName("parsing_atfile")]
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

    @[Test JsName("parsing_atfile_recursive")]
    fun `parsing atfile recursive`() {
        class C : TestCommand() {
            val foo by option()
            val arg by argument()

            override fun run_() {
                foo shouldBe "123"
                arg shouldBe "456"
            }
        }

        val c = C().withAtFiles(
            "foo" to "--foo 123 456",
            "bar" to "@foo"
        )
        c.parse("@foo")

        val result = CommandLineParser.parse(c, listOf("@foo"))
        result.originalArgv shouldBe listOf("@foo")
        result.expandedArgv shouldBe listOf("--foo", "123", "456")
    }

    @[Test JsName("parsing_atfile_unclosed_quotes")]
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

    @[Test JsName("parsing_atfile_after_dash")]
    fun `passing atfile after --`() {
        class C : TestCommand() {
            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().withAtFiles().parse("-- @file")
    }

    @[Test JsName("escaping_atfile")]
    fun `escaping atfile`() {
        class C : TestCommand() {
            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().withAtFiles().parse("@@file")
    }

    @[Test JsName("atfile_after_arg")]
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

    @[Test JsName("atfile_after_subcommand")]
    fun `atfile after subcommand`() {
        class C : TestCommand() {
            val o by option().flag()

            override fun run_() {
                o shouldBe true
            }
        }

        TestCommand().subcommands(C())
            .withAtFiles("f" to "--o").parse("c @f")
    }

    @[Test JsName("disabling_atfile")]
    fun `disabling atfile`() {
        class C : TestCommand() {
            init {
                context {
                    readArgumentFile = null
                }
            }

            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().parse("@file")
    }

    @[Test JsName("parsing_atfile_with_alias")]
    fun `parsing atfile with alias`() {
        class C : TestCommand() {
            val foo by option()
            val arg by argument().multiple()

            override fun aliases() = mapOf(
                "alias" to listOf("@foo", "789"),
            )

            override fun run_() {
                foo shouldBe "123"
                arg shouldBe listOf("456", "789")
            }
        }

        val c = C().withAtFiles("foo" to "--foo 123 456")
        c.parse("alias")

        val result = CommandLineParser.parse(c, listOf("alias"))
        result.originalArgv shouldBe listOf("alias")
        result.expandedArgv shouldBe listOf("--foo", "123", "456", "789")
    }
}
