package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.testing.NeverCalledCliktCommand
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class EagerOptionsTest {
    @Test
    fun `version default`() {
        class C : NeverCalledCliktCommand(name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        shouldThrow<PrintMessage> {
            C().parse(splitArgv("--version"))
        }.message shouldBe "prog version 1.2.3"
    }

    @Test
    fun `version custom message`() {
        class C : NeverCalledCliktCommand(name = "prog") {
            init {
                versionOption("1.2.3", names = setOf("--foo")) { "$it bar" }
            }
        }

        shouldThrow<PrintMessage> {
            C().parse(splitArgv("--foo"))
        }.message shouldBe "1.2.3 bar"
    }

    @Test
    fun `multiple eager options`() {
        class C : NeverCalledCliktCommand(name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        shouldThrow<PrintHelpMessage> {
            C().parse(splitArgv("--help --version"))
        }

        shouldThrow<PrintMessage> {
            C().parse(splitArgv("--version --help"))
        }.message shouldBe "prog version 1.2.3"
    }
}
