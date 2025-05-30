package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.NoSuchOption
import com.github.ajalt.clikt.parameters.arguments.argument
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

class ULongTest {
    @[Test JsName("ulong_option")]
    fun `ulong option`() = forAll(
        row("", null),
        row("-x0", 0uL),
        row("-${ULong.MAX_VALUE}", ULong.MAX_VALUE),
    ) { argv, expected ->
        class C : TestCommand() {
            val x: ULong? by option("-x").ulong(acceptsValueWithoutName = true)
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @[Test JsName("ulong_option_error")]
    fun `ulong option error`() {
        class C : TestCommand(called = false) {
            @Suppress("unused")
            val foo by option().ulong()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .formattedMessage shouldBe "invalid value for --foo: bar is not a valid integer"

        shouldThrow<NoSuchOption> { C().parse("-2") }
        shouldThrow<BadParameterValue> { C().parse("--foo=-1") }
    }

    @[Test JsName("ulong_option_with_default")]
    fun `ulong option with default`() = forAll(
        row("", 111uL),
        row("--xx=4", 4uL),
        row("-x5", 5uL)
    ) { argv, expected ->
        class C : TestCommand() {
            val x: ULong by option("-x", "--xx").ulong().default(111uL)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("int_option_with_default")]
    fun `int option with default`() = forAll(
        row("", 111),
        row("--xx=4", 4),
        row("-x5", 5)
    ) { argv, expected ->
        class C : TestCommand() {
            val x: Int by option("-x", "--xx").int().default(111)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("ulong_argument")]
    fun `ulong argument`() {
        class C : TestCommand(treatUnknownOptionsAsArgs = true) {
            val a by argument().ulong()
        }

        C().parse("2").a shouldBe 2uL
        shouldThrow<BadParameterValue> { C().parse("-1") }
    }
}
