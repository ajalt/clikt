package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class EagerOptionsTest {
    @Test
    fun `version default`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        shouldThrow<PrintMessage> {
            C().parse("--version")
        }.message shouldBe "prog version 1.2.3"
    }

    @Test
    fun `version custom message`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3", names = setOf("--foo")) { "$it bar" }
            }
        }

        shouldThrow<PrintMessage> {
            C().parse("--foo")
        }.message shouldBe "1.2.3 bar"
    }

    @Test
    fun `multiple eager options`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        shouldThrow<PrintHelpMessage> {
            C().parse("--help --version")
        }

        shouldThrow<PrintMessage> {
            C().parse("--version --help")
        }.message shouldBe "prog version 1.2.3"
    }
}
