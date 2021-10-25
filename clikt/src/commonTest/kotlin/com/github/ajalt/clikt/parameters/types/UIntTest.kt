package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.NoSuchOption
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

class UIntTest {
    @Test
    @JsName("uint_option")
    fun `uint option`() = forAll(
        row("", null),
        row("-x0", 0u),
        row("-${UInt.MAX_VALUE}", UInt.MAX_VALUE),
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x").uint(acceptsValueWithoutName = true)
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("uint_option_error")
    fun `uint option error`() {
        class C : TestCommand(called = false) {
            @Suppress("unused")
            val foo by option().uint()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .formattedMessage shouldBe "Invalid value for \"--foo\": bar is not a valid integer"

        shouldThrow<NoSuchOption> { C().parse("-2") }
        shouldThrow<BadParameterValue> { C().parse("--foo=-1") }
    }

    @Test
    @JsName("uint_argument")
    fun `uint argument`() {
        class C : TestCommand(treatUnknownOptionsAsArgs = true) {
            val a by argument().uint()
        }

        C().parse("2").a shouldBe 2u
        shouldThrow<BadParameterValue> { C().parse("-1") }
    }
}
