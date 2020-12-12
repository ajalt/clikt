package com.github.ajalt.clikt.parsers

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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test


class ParserTest {
    @get:Rule
    var testFolder = TemporaryFolder()

    @Test
    fun `parsing @file`() {
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

        val file = testFolder.newFile()
        file.writeText("""
        |--foo 123 # comment
        |--bar='a b "\''
        |\\ "" \# #
        |a\
        | b
        |c\
        |d
        |'e
        |f'
        """.trimMargin())

        C().parse("@${file.path}")
    }

    @Test
    fun `parsing @file recursive`() {
        class C : TestCommand() {
            val foo by option()
            val arg by argument()

            override fun run_() {
                foo shouldBe "123"
                arg shouldBe "456"
            }
        }

        val file1 = testFolder.newFile()
        file1.writeText("--foo 123 456")
        val file2 = testFolder.newFile()
        file2.writeText("@${file1.path.replace("\\", "\\\\")}")
        C().parse("@${file2.path}")
    }

    @Test
    fun `parsing @file unclosed quotes`() {
        class C : TestCommand(called = false) {
            val arg by argument()
        }

        val file = testFolder.newFile()
        file.writeText("""
        |'a b "\'
        |
        """.trimMargin())

        shouldThrow<UsageError> { C().parse("@${file.path}") }
                .text shouldContain "unclosed quote"
    }

    @Test
    fun `passing @file after --`() {
        class C : TestCommand() {
            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().parse("-- @file")
    }

    @Test
    fun `escaping @file`() {
        class C : TestCommand() {
            val arg by argument()

            override fun run_() {
                arg shouldBe "@file"
            }
        }

        C().parse("@@file")
    }

    @Test
    fun `@file after arg`() {
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

        val file = testFolder.newFile()
        file.writeText("bar")

        val argv = "foo @${file.path}"
        C(true).parse(argv)
        C(false).parse(argv)
    }

    @Test
    fun `disabling @file`() {
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
