package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class ChoiceTypeTest {
    @Test
    fun `choice option strings`() {
        class C : NoRunCliktCommand() {
            val x by option("-x", "--xx").choice("foo", "bar")
        }

        C().apply {
            parse(splitArgv("-xfoo"))
            x shouldBe "foo"
        }

        C().apply {
            parse(splitArgv("--xx=bar"))
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse(splitArgv("--xx baz")) }
                .message shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    fun `choice option map`() {
        class C : NoRunCliktCommand() {
            val x by option("-x", "--xx")
                    .choice("foo" to 1, "bar" to 2)
        }

        C().apply {
            parse(splitArgv("-xfoo"))
            x shouldBe 1
        }

        C().apply {
            parse(splitArgv("--xx=bar"))
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse(splitArgv("-x baz")) }
                .message shouldBe "Invalid value for \"-x\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse(splitArgv("--xx=baz")) }
                .message shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    fun `choice argument strings`() {
        class C : CliktCommand() {
            val x by argument().choice("foo", "bar")
            override fun run() {
                arguments[0].name shouldBe "X"
            }
        }

        C().apply {
            parse(splitArgv("foo"))
            x shouldBe "foo"
        }

        C().apply {
            parse(splitArgv("bar"))
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse(splitArgv("baz")) }
                .message shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    fun `choice argument map`() {
        class C : CliktCommand() {
            val x by argument().choice("foo" to 1, "bar" to 2)
            override fun run() {
                arguments[0].name shouldBe "X"
            }
        }

        C().apply {
            parse(splitArgv("foo"))
            x shouldBe 1
        }

        C().apply {
            parse(splitArgv("bar"))
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse(splitArgv("baz")) }
                .message shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"
    }
}
