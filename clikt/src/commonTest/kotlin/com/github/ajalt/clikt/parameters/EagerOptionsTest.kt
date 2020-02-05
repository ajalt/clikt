package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.testing.TestCommand
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import kotlin.js.JsName
import kotlin.test.Test

class EagerOptionsTest {
    @Test
    @JsName("version_default")
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
    @JsName("version_custom_message")
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
    @JsName("multiple_eager_options")
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
