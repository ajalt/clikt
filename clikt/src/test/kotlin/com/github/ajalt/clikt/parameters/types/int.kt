package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.*
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import org.junit.Test

class IntTypeTest {
    @Test
    fun `int option`() = parameterized(
            row("", null),
            row("--xx 3", 3),
            row("--xx=4", 4),
            row("-x5", 5)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `int option with default`() = parameterized(
            row("", 111),
            row("--xx 3", 3),
            row("--xx=4", 4),
            row("-x5", 5)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().default(111)
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `int argument`() = parameterized(
            row("", null, emptyList<Int>()),
            row("1", 1, emptyList()),
            row("1 2", 1, listOf(2)),
            row("1 2 3", 1, listOf(2, 3))) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by argument().int().optional()
            val y by argument().int().multiple()
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }
}
