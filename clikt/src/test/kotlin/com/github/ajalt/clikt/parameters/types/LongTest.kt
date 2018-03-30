package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.*
import org.junit.Test

class LongTypeTest {
    @Test
    fun `int option`() = parameterized(
            row("", null),
            row("--xx 3", 3L),
            row("--xx=4", 4L),
            row("-x5", 5L)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").long()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `int option error`() {
        class C : NeverCalledCliktCommand() {
            val foo by option().long()
        }

        assertThrows<BadParameterValue> { C().parse(splitArgv("--foo bar")) }
                .hasMessage("Invalid value for \"--foo\": bar is not a valid integer")
    }

    @Test
    fun `int option with default`() = parameterized(
            row("", 111L),
            row("--xx 3", 3L),
            row("--xx=4", 4L),
            row("-x5", 5L)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").long().default(111L)
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `int argument`() = parameterized(
            row("", null, emptyList<Long>()),
            row("1", 1L, emptyList()),
            row("1 2", 1L, listOf(2L)),
            row("1 2 3", 1L, listOf(2L, 3L))) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by argument().long().optional()
            val y by argument().long().multiple()
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }
}
