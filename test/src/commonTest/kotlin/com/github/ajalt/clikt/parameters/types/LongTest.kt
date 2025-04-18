package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class LongTypeTest {
    @[Test JsName("long_option")]
    fun `long option`() = forAll(
        row("", null),
        row("--xx=4", 4L),
        row("-x5", 5L),
        row("-5", 5L),
        row("-0", 0L),
    ) { argv, ex ->
        class C : TestCommand() {
            val x: Long? by option("-x", "--xx").long(acceptsValueWithoutName = true)
            override fun run_() {
                x shouldBe ex
            }
        }

        C().parse(argv)
    }

    @Test
    @Suppress("unused")
    @JsName("long_option_error")
    fun `long option error`() {
        class C : TestCommand(called = false) {
            val foo by option().long()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .formattedMessage shouldBe "invalid value for --foo: bar is not a valid integer"
    }

    @[Test JsName("long_option_with_default")]
    fun `long option with default`() = forAll(
        row("", 111L),
        row("--xx=4", 4L),
        row("-x5", 5L)
    ) { argv, expected ->
        class C : TestCommand() {
            val x: Long by option("-x", "--xx").long().default(111L)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("long_argument")]
    fun `long argument`() = forAll(
        row("", null, emptyList()),
        row("1 2", 1L, listOf(2L)),
        row("1 2 3", 1L, listOf(2L, 3L))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().long().optional()
            val y by argument().long().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }
}
