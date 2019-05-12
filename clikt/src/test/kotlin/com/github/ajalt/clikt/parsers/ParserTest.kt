package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.NeverCalledCliktCommand
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


class ParserTest {
    @get:Rule
    var testFolder = TemporaryFolder()

    @Test
    fun parsingArgFile() {
        class C : CliktCommand() {
            val foo by option()
            val bar by option()
            val arg1 by argument()
            val arg2 by argument()
            val arg3 by argument()

            override fun run() {
                foo shouldBe "123"
                bar shouldBe "a b \"'"
                arg1 shouldBe "\\"
                arg2 shouldBe ""
                arg3 shouldBe "#"
            }
        }

        val file = testFolder.newFile()
        file.writeText("""
        |--foo 123 # comment
        |--bar='a b "\''
        |\\ "" \# #
        """.trimMargin())

        C().parse(splitArgv("@${file.path}"))
    }

    @Test
    fun parsingArgFileRecursive() {
        class C : CliktCommand() {
            val foo by option()
            val arg by argument()

            override fun run() {
                foo shouldBe "123"
                arg shouldBe "456"
            }
        }

        val file1 = testFolder.newFile()
        file1.writeText("--foo 123 456")
        val file2 = testFolder.newFile()
        file2.writeText("@${file1.path.replace("\\", "\\\\")}")
        C().parse(splitArgv("@${file2.path}"))
    }

    @Test
    fun parsingArgFileUnclosedQuotes() {
        class C : NeverCalledCliktCommand() {
            val arg by argument()
        }

        val file = testFolder.newFile()
        file.writeText("""
        |'a b "\'
        |
        """.trimMargin())

        shouldThrow<UsageError> { C().parse(splitArgv("@${file.path}")) }
                .text shouldBe "unclosed quote in @-file"
    }
}
