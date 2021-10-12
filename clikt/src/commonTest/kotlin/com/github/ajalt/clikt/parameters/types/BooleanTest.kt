package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class BooleanTest {
    @Test
    @JsName("boolean_option")
    fun `boolean option`() = forAll(
        row("", null),
        row("--x=true", true),
        row("--x=t", true),
        row("--x=1", true),
        row("--x=yes", true),
        row("--x=y", true),
        row("--x=on", true),
        row("--x=True", true),
        row("--x=ON", true),
        row("--x=false", false),
        row("--x=f", false),
        row("--x=0", false),
        row("--x=no", false),
        row("--x=n", false),
        row("--x=off", false),
        row("--x=False", false),
        row("--x=OFF", false),
    ) { argv, ex ->
        class C : TestCommand() {
            val x by option().boolean()
            override fun run_() {
                x shouldBe ex
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("boolean_option_error")
    fun `boolean option error`() {
        @Suppress("unused")
        class C : TestCommand(called = false) {
            val foo by option().boolean()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .message shouldBe "Invalid value for \"--foo\": bar is not a valid boolean"
    }

    @Test
    @JsName("boolean_option_with_default")
    fun `boolean option with default`() = forAll(
        row("", true),
        row("--x=true", true),
        row("--x off", false),
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option().boolean().default(true)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("boolean_argument")
    fun `boolean argument`() = forAll(
        row("", emptyList()),
        row("1 0 ON off", listOf(true, false, true, false)),
    ) { argv, ex ->
        class C : TestCommand() {
            val a by argument().boolean().multiple()
            override fun run_() {
                a shouldBe ex
            }
        }

        C().parse(argv)
    }
}
