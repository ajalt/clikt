package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File
import kotlin.test.Test

class FileTypeTest {
    @Test
    fun `file option with default args`() {
        class C : TestCommand() {
            val x by option("-x", "--xx").file()
            override fun run_() {
                x!!.shouldBeInstanceOf<File>()
            }
        }

        C().parse("-x.")
    }

    @Test
    fun `file argument with default args`() {
        class C : TestCommand() {
            val x by argument().file()
            override fun run_() {
                x.shouldBeInstanceOf<File>()
            }
        }

        C().parse(".")
    }
}
