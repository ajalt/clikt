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

class EnumTest {
    private enum class TestEnum { A, B }

    @Test
    @JsName("enum_option")
    fun `enum option`() = forAll(
        row("", null),
        row("--xx A", TestEnum.A),
        row("--xx a", TestEnum.A),
        row("--xx=A", TestEnum.A),
        row("-xB", TestEnum.B),
        row("-xb", TestEnum.B)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").enum<TestEnum>()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_option_key")
    fun `enum option key`() = forAll(
        row("", null),
        row("-xAz", TestEnum.A),
        row("-xaZ", TestEnum.A),
        row("-xBz", TestEnum.B),
        row("-xBZ", TestEnum.B)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x").enum<TestEnum> { it.name + "z" }
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_option_error")
    fun `enum option error`() {
        @Suppress("unused")
        class C : TestCommand() {
            val foo by option().enum<TestEnum>(ignoreCase = false)
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .formattedMessage shouldBe "Invalid value for \"--foo\": invalid choice: bar. (choose from A, B)"

        shouldThrow<BadParameterValue> { C().parse("--foo a") }
            .formattedMessage shouldBe "Invalid value for \"--foo\": invalid choice: a. (choose from A, B)"
    }

    @Test
    @JsName("enum_option_with_default")
    fun `enum option with default`() = forAll(
        row("", TestEnum.B),
        row("--xx A", TestEnum.A),
        row("--xx=A", TestEnum.A),
        row("-xA", TestEnum.A)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").enum<TestEnum>().default(TestEnum.B)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("enum_argument")
    fun `enum argument`() = forAll(
        row("", null, emptyList()),
        row("A", TestEnum.A, emptyList()),
        row("b", TestEnum.B, emptyList()),
        row("A a B", TestEnum.A, listOf(TestEnum.A, TestEnum.B))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().enum<TestEnum>().optional()
            val y by argument().enum<TestEnum>().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_argument_key")
    fun `enum argument key`() = forAll(
        row("", emptyList()),
        row("az", listOf(TestEnum.A)),
        row("AZ", listOf(TestEnum.A)),
        row("aZ Bz", listOf(TestEnum.A, TestEnum.B))
    ) { argv, ex ->
        class C : TestCommand() {
            val x by argument().enum<TestEnum> { it.name + "z" }.multiple()
            override fun run_() {
                x shouldBe ex
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_argument_error")
    fun `enum argument error`() {
        @Suppress("unused")
        class C : TestCommand() {
            val foo by argument().enum<TestEnum>(ignoreCase = false)
        }

        shouldThrow<BadParameterValue> { C().parse("bar") }
            .formattedMessage shouldBe "Invalid value for \"FOO\": invalid choice: bar. (choose from A, B)"

        shouldThrow<BadParameterValue> { C().parse("a") }
            .formattedMessage shouldBe "Invalid value for \"FOO\": invalid choice: a. (choose from A, B)"
    }
}
