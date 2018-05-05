package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.assertThrows
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ContextTest {
    @Test
    fun `find functions single context`() {
        class C : CliktCommand() {
            val o1 by findObject<String>()
            val o2 by findObject { "foo" }
            val o3 by findObject<String>()
            val o4 by findObject<Int>()

            override fun run() {
                assertThat(context.findRoot()).isEqualTo(context)
            }
        }

        val c = C().apply { parse(emptyArray()) }

        assertThat(c.o1).isNull()
        assertThat(c.o2).isEqualTo("foo")
        assertThat(c.o3).isEqualTo("foo")
        assertThat(c.o4).isNull()
    }

    @Test
    fun `find functions parent context`() {
        class Foo

        val foo = Foo()

        class C : CliktCommand(invokeWithoutSubcommand = true) {
            val o1 by findObject<Foo>()
            val o2 by findObject { foo }
            val o3 by findObject<Foo>()
            val o4 by findObject<Int>()

            override fun run() {
                assertThat(context.findRoot()).isEqualTo(context)
            }
        }

        val child = C()
        val parent = C().subcommands(child).apply { parse(emptyArray()) }
        assertThat(parent.o1).isEqualTo(child.o1).isNull()
        assertThat(parent.o2).isEqualTo(child.o2).isEqualTo(foo)
        assertThat(parent.o3).isEqualTo(child.o3).isEqualTo(foo)
        assertThat(parent.o4).isEqualTo(child.o4).isNull()
    }

    @Test
    fun `default help option names`() {
        class C : NoRunCliktCommand()

        assertThrows<PrintHelpMessage> { C().parse(splitArgv("--help")) }
        assertThrows<PrintHelpMessage> { C().parse(splitArgv("-h")) }
        assertThrows<PrintHelpMessage> {
            C().context { helpOptionNames = setOf("-x") }.parse(splitArgv("-x"))
        }
        assertThrows<NoSuchOption> {
            C().context { helpOptionNames = setOf("--x") }.parse(splitArgv("--help"))
        }
    }
}
