package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class OptionChoiceTest {
    @Test
    @JsName("choice_option_strings")
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
            .formattedMessage shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("--xx FOO") }
            .formattedMessage shouldBe "Invalid value for \"--xx\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_option_map")
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
            .formattedMessage shouldBe "Invalid value for \"-x\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("--xx=baz") }
            .formattedMessage shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("-x FOO") }
            .formattedMessage shouldBe "Invalid value for \"-x\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_option_insensitive")
    fun `choice option insensitive`() {
        class C : TestCommand() {
            val x by option("-x").choice("foo", "bar", ignoreCase = true)
            val y by option("-y").choice("foo" to 1, "bar" to 2, ignoreCase = true)
        }

        C().apply {
            parse("-xFOO -yFOO")
            x shouldBe "foo"
            y shouldBe 1
        }

        C().apply {
            parse("-xbar -ybAR")
            x shouldBe "bar"
            y shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("-xbaz") }
            .formattedMessage shouldBe "Invalid value for \"-x\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_argument_strings")
    fun `choice argument strings`() {
        class C : TestCommand() {
            val x by argument().choice("foo", "bar")
            override fun run_() {
                registeredArguments()[0].name shouldBe "X"
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
            .formattedMessage shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("FOO") }
            .formattedMessage shouldBe "Invalid value for \"X\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_argument_map")
    fun `choice argument map`() {
        class C : TestCommand() {
            val x by argument().choice("foo" to 1, "bar" to 2)
            override fun run_() {
                registeredArguments()[0].name shouldBe "X"
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
            .formattedMessage shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("FOO") }
            .formattedMessage shouldBe "Invalid value for \"X\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_argument_insensitive")
    fun `choice argument insensitive`() {
        class C : TestCommand() {
            val x by argument().choice("foo", "bar", ignoreCase = true)
            val y by argument().choice("foo" to 1, "bar" to 2, ignoreCase = true)
        }

        C().apply {
            parse("FOO FOO")
            x shouldBe "foo"
            y shouldBe 1
        }

        C().apply {
            parse("bar bAR")
            x shouldBe "bar"
            y shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("baz foo") }
            .formattedMessage shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"
    }
}
