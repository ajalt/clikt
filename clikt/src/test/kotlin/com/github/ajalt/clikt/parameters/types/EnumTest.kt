package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Test

@Suppress("unused")
class EnumTest {
    enum class TestEnum { A, B }

    @Test
    fun `enum option`() = forall(
            row("", null),
            row("--xx A", TestEnum.A),
            row("--xx=A", TestEnum.A),
            row("-xB", TestEnum.B)) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").enum<TestEnum>()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    fun `enum option error`() {
        class C : TestCommand() {
            val foo by option().enum<TestEnum>()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
                .message shouldBe "Invalid value for \"--foo\": Unknown enum constant TestEnum.bar"
    }

    @Test
    fun `enum option with default`() = forall(
            row("", TestEnum.B),
            row("--xx A", TestEnum.A),
            row("--xx=A", TestEnum.A),
            row("-xA", TestEnum.A)) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").enum<TestEnum>().default(TestEnum.B)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    fun `enum argument`() = forall(
            row("", null, emptyList()),
            row("A", TestEnum.A, emptyList()),
            row("A A B", TestEnum.A, listOf(TestEnum.A, TestEnum.B))) { argv, ex, ey ->
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
}
