package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.testing.NeverCalledCliktCommand
import com.github.ajalt.clikt.testing.assertThrows
import com.github.ajalt.clikt.testing.splitArgv
import org.junit.Test

class EagerOptionsTest {
    @Test
    fun `version default`() {
        class C : NeverCalledCliktCommand(name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        assertThrows<PrintMessage> {
            C().parse(splitArgv("--version"))
        }.hasMessage("prog version 1.2.3")
    }

    @Test
    fun `version custom message`() {
        class C : NeverCalledCliktCommand(name = "prog") {
            init {
                versionOption("1.2.3", names = setOf("--foo")) { "$it bar" }
            }
        }

        assertThrows<PrintMessage> {
            C().parse(splitArgv("--foo"))
        }.hasMessage("1.2.3 bar")
    }

    @Test
    fun `multiple eager options`() {
        class C : NeverCalledCliktCommand(name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        assertThrows<PrintHelpMessage> {
            C().parse(splitArgv("--help --version"))
        }

        assertThrows<PrintMessage> {
            C().parse(splitArgv("--version --help"))
        }.hasMessage("prog version 1.2.3")
    }
}
