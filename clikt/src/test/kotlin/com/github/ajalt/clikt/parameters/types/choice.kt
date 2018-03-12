package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.assertThrows
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ChoiceTypeTest {
    @Test
    fun `choice option strings`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx").choice("foo", "bar")
            override fun run() = Unit
        }

        C().apply {
            parse(splitArgv("-xfoo"))
            assertThat(x).isEqualTo("foo")
        }

        C().apply {
            parse(splitArgv("--xx=bar"))
            assertThat(x).isEqualTo("bar")
        }

        assertThrows<BadParameter> {
            C().parse(splitArgv("--xx baz"))
        }.hasMessage("Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)")
    }

    @Test
    fun `choice option map`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx")
                    .choice("foo" to 1, "bar" to 2)

            override fun run() = Unit
        }

        C().apply {
            parse(splitArgv("-xfoo"))
            assertThat(x).isEqualTo(1)
        }

        C().apply {
            parse(splitArgv("--xx=bar"))
            assertThat(x).isEqualTo(2)
        }

        assertThrows<BadParameter> {
            C().parse(splitArgv("-x baz"))
        }.hasMessage("Invalid value for \"-x\": invalid choice: baz. (choose from foo, bar)")

        assertThrows<BadParameter> {
            C().parse(splitArgv("--xx=baz"))
        }.hasMessage("Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)")
    }

    @Test
    fun `choice argument strings`() {
        class C : CliktCommand() {
            val x by argument().choice("foo", "bar")
            override fun run() {
                assertThat(arguments[0].name).isEqualTo("X")
            }
        }

        C().apply {
            parse(splitArgv("foo"))
            assertThat(x).isEqualTo("foo")
        }

        C().apply {
            parse(splitArgv("bar"))
            assertThat(x).isEqualTo("bar")
        }

        assertThrows<BadParameter> {
            C().parse(splitArgv("baz"))
        }.hasMessage("Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)")
    }

    @Test
    fun `choice argument map`() {
        class C : CliktCommand() {
            val x by argument().choice("foo" to 1, "bar" to 2)
            override fun run() {
                assertThat(arguments[0].name).isEqualTo("X")
            }
        }

        C().apply {
            parse(splitArgv("foo"))
            assertThat(x).isEqualTo(1)
        }

        C().apply {
            parse(splitArgv("bar"))
            assertThat(x).isEqualTo(2)
        }

        assertThrows<BadParameter> {
            C().parse(splitArgv("baz"))
        }.hasMessage("Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)")
    }
}
