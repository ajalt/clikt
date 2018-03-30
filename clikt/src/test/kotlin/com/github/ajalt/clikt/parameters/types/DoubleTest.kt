package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoubleTest {
    @Test
    fun `double option`() = parameterized(
            row("", null),
            row("--xx 3", 3.0),
            row("--xx=4.0", 4.0),
            row("-x5.5", 5.5)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").double()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `double option error`() {
        class C : NeverCalledCliktCommand() {
            val foo by option().double()
        }

        assertThrows<BadParameterValue> { C().parse(splitArgv("--foo bar")) }
                .hasMessage("Invalid value for \"--foo\": bar is not a valid floating point value")
    }

    @Test
    fun `double option with default`() = parameterized(
            row("", -1.0),
            row("--xx 3", 3.0),
            row("--xx=4.0", 4.0),
            row("-x5.5", 5.5)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").double().default(-1.0)
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `double argument`() = parameterized(
            row("", null, emptyList<Float>()),
            row("1.1", 1.1, emptyList()),
            row("1.1 2", 1.1, listOf(2.0)),
            row("1.1 2 3", 1.1, listOf(2.0, 3.0))) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by argument().double().optional()
            val y by argument().double().multiple()
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }
}
