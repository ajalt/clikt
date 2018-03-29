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

class FloatTest {
    @Test
    fun `float option`() = parameterized(
            row("", null),
            row("--xx 3", 3f),
            row("--xx=4.0", 4f),
            row("-x5.5", 5.5f)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").float()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `float option error`() {
        class C : NeverCalledCliktCommand() {
            val foo by option().float()
        }

        assertThrows<BadParameterValue> { C().parse(splitArgv("--foo bar")) }
                .hasMessage("Invalid value for \"--foo\": bar is not a valid floating point value")
    }

    @Test
    fun `float option with default`() = parameterized(
            row("", -1f),
            row("--xx 3", 3f),
            row("--xx=4.0", 4f),
            row("-x5.5", 5.5f)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").float().default(-1f)
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `float argument`() = parameterized(
            row("", null, emptyList<Float>()),
            row("1.1", 1.1f, emptyList()),
            row("1.1 2", 1.1f, listOf(2f)),
            row("1.1 2 3", 1.1f, listOf(2f, 3f))) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by argument().float().optional()
            val y by argument().float().multiple()
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }
}
