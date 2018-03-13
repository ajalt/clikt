package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.assertThrows
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class CliktCommandTest {
    @Test
    fun `invokeWithoutSubcommand=false`() {
        class C : CliktCommand(name = "foo") {
            var ran = false
            override fun run() {
                ran = true
            }
        }

        C().apply {
            parse(emptyArray())
            assertThat(ran).isTrue()
        }

        var child = C()
        C().subcommands(child).apply {
            assertThrows<PrintHelpMessage> {
                parse(emptyArray())
            }
            assertThat(ran).isFalse()
            assertThat(child.ran).isFalse()
        }

        child = C()
        C().subcommands(child).apply {
            parse(splitArgv("foo"))
            assertThat(ran).isTrue()
            assertThat(context.invokedSubcommand).isEqualTo(child)
            assertThat(child.ran).isTrue()
            assertThat(child.context.invokedSubcommand).isNull()
        }
    }

    @Test
    fun `invokeWithoutSubcommand=true`() {
        class C : CliktCommand(name = "foo", invokeWithoutSubcommand = true) {
            var ran = false
            override fun run() {
                ran = true
            }
        }

        C().apply {
            parse(emptyArray())
            assertThat(ran).isTrue()
        }

        var child = C()
        C().subcommands(listOf(child)).apply {
            parse(emptyArray())
            assertThat(ran).isTrue()
            assertThat(context.invokedSubcommand).isNull()
            assertThat(child.ran).isFalse()
        }

        child = C()
        C().subcommands(child).apply {
            parse(splitArgv("foo"))
            assertThat(ran).isTrue()
            assertThat(context.invokedSubcommand).isEqualTo(child)
            assertThat(child.ran).isTrue()
            assertThat(child.context.invokedSubcommand).isNull()
        }
    }
}
