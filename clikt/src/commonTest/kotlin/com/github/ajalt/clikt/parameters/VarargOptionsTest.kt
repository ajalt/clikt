package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.MissingArgument
import com.github.ajalt.clikt.core.NoSuchOption
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.optionalValue
import com.github.ajalt.clikt.parameters.options.varargValues
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class VarargOptionsTest {
    @Test
    fun optionalValue() = forAll(
        row("", 0),
        row("--o", 1),
        row("--o=2", 2),
    ) { argv, ex ->
        class C : TestCommand() {
            val o by option().int().optionalValue(1).default(0)
            override fun run_() {
                o shouldBe ex
            }
        }
        C().parse(argv)
    }

    @Test
    fun varargValues() = forAll(
        row("", null),
        row("--o", listOf()),
        row("-o1", listOf(1)),
        row("--o=1 2", listOf(1, 2)),
        row("--o=1 2 --", listOf(1, 2)),
        row("--o 1 2 3", listOf(1, 2, 3)),
        row("--o 1 2 --o=3 4 5", listOf(3, 4, 5)),
    ) { argv, ex ->
        class C : TestCommand() {
            val o by option("-o", "--o").int().varargValues(min = 0)
            override fun run_() {
                o shouldBe ex
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("varargValues_NoSuchOption")
    fun `varargValues NoSuchOption`() {
        class C : TestCommand(false) {
            val o by option("-o", "--o").varargValues(min = 0)
        }
        shouldThrow<NoSuchOption> { C().parse("--o --foo") }
    }

    @Test
    @JsName("varargValues_unknownOptionsAsArgs")
    fun `varargValues unknownOptionsAsArgs`() {
        class C : TestCommand(treatUnknownOptionsAsArgs = true) {
            val o by option("-o", "--o").varargValues(min = 0)

            override fun run_() {
                o shouldBe listOf("--foo")
            }
        }
        C().parse("--o --foo")
    }

    @Test
    @JsName("varargValues_subcommand")
    fun `varargValues subcommand`() {
        class Sub : TestCommand() {
            val o by option()

            override fun run_() {
                o shouldBe "bar"
            }
        }

        class C : TestCommand(treatUnknownOptionsAsArgs = true) {
            val o by option("-o", "--o").varargValues(min = 0)

            override fun run_() {
                o shouldBe listOf("foo")
            }
        }
        C().subcommands(Sub()).parse("--o foo sub --o=bar")
    }

    @Test
    @JsName("varargValues_argument")
    fun `varargValues argument`() {
        class C : TestCommand(treatUnknownOptionsAsArgs = true) {
            val o by option("-o", "--o").int().varargValues(min = 0)
            val a by argument().int()

            override fun run_() {
                o shouldBe listOf(1, 2)
                a shouldBe 3
            }
        }
        C().parse("--o 1 2 -- 3")
        shouldThrow<MissingArgument> { C().parse("--o 1 2 3") }
    }
}
