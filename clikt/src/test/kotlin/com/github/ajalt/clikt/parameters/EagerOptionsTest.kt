package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.testing.assertThrows
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Fail.fail
import org.junit.Test

class EagerOptionsTest {
    @Test
    fun `version default`() {
        class C : CliktCommand(name = "prog") {
            init {
                versionOption("1.2.3")
            }

            override fun run() = fail("should not be called")
        }

        assertThrows<PrintMessage> {
            C().parse(splitArgv("--version"))
        }.hasMessage("prog version 1.2.3")
    }

    @Test
    fun `version custom message`() {
        class C : CliktCommand(name = "prog") {
            init {
                versionOption("1.2.3", names = setOf("--foo")) { "$it bar" }
            }

            override fun run() = fail("should not be called")
        }

        assertThrows<PrintMessage> {
            C().parse(splitArgv("--foo"))
        }.hasMessage("1.2.3 bar")
    }

    @Test
    fun `multiple eager options`() {
        class C : CliktCommand(name = "prog") {
            init {
                versionOption("1.2.3")
            }

            override fun run() = fail("should not be called")
        }

        assertThrows<PrintHelpMessage> {
            C().parse(splitArgv("--help --version"))
        }

        assertThrows<PrintMessage> {
            C().parse(splitArgv("--version --help"))
        }.hasMessage("prog version 1.2.3")
    }
}
