package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import com.github.ajalt.clikt.testing.withEnv
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused")
class FlagValueTest {
    @[Test JsName("acceptsValue_parses_attached_value")]
    fun `acceptsValue parses an attached value`() = forAll(
        row("", false),
        row("--x", true),
        row("--x=true", true),
        row("--x=false", false),
        row("--x=yes", true),
        row("--x=ON", true),
        row("--x=0", false),
        row("--x=true --x=no", false),
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("--x").flag(acceptsValue = true)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("acceptsValue_rejects_empty_value")]
    fun `acceptsValue rejects an empty value`() {
        class C : TestCommand(called = false) {
            val x by option("--x").flag(acceptsValue = true)
        }
        shouldThrow<BadParameterValue> { C().parse("--x=") }
    }

    @[Test JsName("acceptsValue_requires_attached_value")]
    fun `acceptsValue requires the value to be attached`() {
        class C : TestCommand() {
            val x by option("--x").flag(acceptsValue = true)
            val arg by argument()
            override fun run_() {
                // the unattached "true" is a positional, not the flag's value
                x shouldBe true
                arg shouldBe "true"
            }
        }
        C().parse("--x true")
    }

    @[Test JsName("acceptsValue_reads_envvar")]
    fun `acceptsValue reads its value from an envvar`() {
        class C : TestCommand() {
            val x by option("--x", envvar = "X").flag(default = true, acceptsValue = true)
            override fun run_() {
                x shouldBe false
            }
        }
        C().withEnv("X" to "false").parse("")
    }

    @[Test JsName("acceptsValue_rejects_empty_envvar")]
    fun `acceptsValue rejects an empty envvar value`() {
        class C : TestCommand(called = false) {
            val x by option("--x", envvar = "X").flag(default = true, acceptsValue = true)
        }
        shouldThrow<BadParameterValue> { C().withEnv("X" to "").parse("") }
    }

    @[Test JsName("value_on_secondary_name_is_an_error")]
    fun `a value on a secondary name is an error pointing to the positive form`() {
        class C : TestCommand(called = false) {
            val cache by option("--cache").flag("--no-cache", default = true, acceptsValue = true)
        }
        val message = shouldThrow<BadParameterValue> { C().parse("--no-cache=true") }.formattedMessage
        message shouldContain "maybe you mean --cache=true"
        message shouldContain "Or use --no-cache to disable"
    }
}
