package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class ChoiceTypeTest {
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
}
