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

class ChoiceTypeTest {
    private enum class TestEnum { A, B }

    @Test
    fun `choice option strings`() {
        class C : TestCommand() {
            val x by option("-x", "--xx").choice("foo", "bar")
        }

        C().apply {
            parse("-xfoo")
            x shouldBe "foo"
        }

        C().apply {
            parse("--xx=bar")
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse("--xx baz") }
                .message shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    fun `choice option map`() {
        class C : TestCommand() {
            val x by option("-x", "--xx")
                    .choice("foo" to 1, "bar" to 2)
        }

        C().apply {
            parse("-xfoo")
            x shouldBe 1
        }

        C().apply {
            parse("--xx=bar")
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("-x baz") }
                .message shouldBe "Invalid value for \"-x\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("--xx=baz") }
                .message shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    fun `choice argument strings`() {
        class C : TestCommand() {
            val x by argument().choice("foo", "bar")
            override fun run_() {
                _arguments[0].name shouldBe "X"
            }
        }

        C().apply {
            parse("foo")
            x shouldBe "foo"
        }

        C().apply {
            parse("bar")
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse("baz") }
                .message shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    fun `choice argument map`() {
        class C : TestCommand() {
            val x by argument().choice("foo" to 1, "bar" to 2)
            override fun run_() {
                _arguments[0].name shouldBe "X"
            }
        }

        C().apply {
            parse("foo")
            x shouldBe 1
        }

        C().apply {
            parse("bar")
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("baz") }
                .message shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"
    }


    @Test
    fun `enum option`() = forall(
            row("", null),
            row("--xx A", TestEnum.A),
            row("--xx=A", TestEnum.A),
            row("-xB", TestEnum.B)
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
    fun `enum option key`() = forall(
            row("", null),
            row("-xa", TestEnum.A),
            row("-xb", TestEnum.B)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x").enum<TestEnum> { it.name.toLowerCase() }
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    fun `enum option error`() {
        @Suppress("unused")
        class C : TestCommand() {
            val foo by option().enum<TestEnum>()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
                .message shouldBe "Invalid value for \"--foo\": invalid choice: bar. (choose from A, B)"
    }

    @Test
    fun `enum option with default`() = forall(
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
    fun `enum argument`() = forall(
            row("", null, emptyList()),
            row("A", TestEnum.A, emptyList()),
            row("A A B", TestEnum.A, listOf(TestEnum.A, TestEnum.B))
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
    fun `enum argument key`() = forall(
            row("", emptyList()),
            row("a", listOf(TestEnum.A)),
            row("a b", listOf(TestEnum.A, TestEnum.B))
    ) { argv, ex ->
        class C : TestCommand() {
            val x by argument().enum<TestEnum> { it.name.toLowerCase() }.multiple()
            override fun run_() {
                x shouldBe ex
            }
        }

        C().parse(argv)
    }
}
