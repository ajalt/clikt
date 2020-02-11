package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import io.kotest.data.forall
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.tables.row
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused")
class DoubleTest {
    @Test
    @JsName("double_option")
    fun `double option`() = forall(
            row("", null),
            row("--xx 3", 3.0),
            row("--xx=4.0", 4.0),
            row("-x5.5", 5.5)) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").double()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("double_option_error")
    fun `double option error`() {
        class C : TestCommand() {
            val foo by option().double()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
                .message shouldBe "Invalid value for \"--foo\": bar is not a valid floating point value"
    }

    @Test
    @JsName("double_option_with_default")
    fun `double option with default`() = forall(
            row("", -1.0),
            row("--xx=4.0", 4.0),
            row("-x5.5", 5.5)) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").double().default(-1.0)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("double_argument")
    fun `double argument`() = forall(
            row("", null, emptyList<Float>()),
            row("1.1 2", 1.1, listOf(2.0)),
            row("1.1 2 3", 1.1, listOf(2.0, 3.0))) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().double().optional()
            val y by argument().double().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }
}
