package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.MissingParameter
import com.github.ajalt.clikt.testing.assertThrows
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

class ArgumentTest {
    @Test
    fun `one required argument`() {
        class C : CliktCommand() {
            val x by argument()
            override fun run() = Unit
        }

        assertThrows<MissingParameter> {
            C().parse(splitArgv(""))
        }
    }

    @Test
    fun `one optional argument`() = parameterized(
            row("", null),
            row("asd", "asd")
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().optional()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one argument nargs=2`() {
        class C : CliktCommand() {
            val x by argument().paired()
            override fun run() {
                assertThat(x).isEqualTo("1" to "2")
            }
        }

        C().parse(splitArgv("1 2"))

        assertThrows<MissingParameter> {
            C().parse(splitArgv(""))
        }
    }

    @Test
    fun `one optional argument nargs=2`() = parameterized(
            row("", null),
            row("foo bar", "foo" to "bar")
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().paired().optional()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one argument nargs=-1`() = parameterized(
            row("", emptyList()),
            row("foo", listOf("foo")),
            row("foo bar", listOf("foo", "bar")),
            row("foo bar baz", listOf("foo", "bar", "baz"))
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().multiple()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two arguments nargs=-1,1`() = parameterized(
            row("foo", emptyList(), "foo"),
            row("foo bar", listOf("foo"), "bar"),
            row("foo bar baz", listOf("foo", "bar"), "baz")
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val foo by argument().multiple()
            val bar by argument()
            override fun run() {
                assertThat(foo).called("foo").isEqualTo(ex)
                assertThat(bar).called("bar").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two arguments nargs=-1,1 empty argv`() {
        class C : CliktCommand() {
            val foo by argument().multiple()
            val bar by argument()
            override fun run() = fail("should not be called. $foo, $bar")
        }
        assertThrows<MissingParameter>("bar") {
            C().parse(splitArgv(""))
        }
    }

    @Test
    fun `two arguments nargs=1,-1`() = parameterized(
            row("", null, emptyList<String>()),
            row("foo", "foo", emptyList()),
            row("foo bar", "foo", listOf("bar")),
            row("foo bar baz", "foo", listOf("bar", "baz"))
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val foo by argument().optional()
            val bar by argument().multiple()
            override fun run() {
                assertThat(foo).called("foo").isEqualTo(ex)
                assertThat(bar).called("bar").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two arguments nargs=1,-1 empty argv`() {
        class C : CliktCommand() {
            val foo by argument()
            val bar by argument().multiple()
            override fun run() = fail("should not be called. $foo, $bar")
        }

        assertThrows<MissingParameter>("foo") {
            C().parse(splitArgv(""))
        }
    }
}
