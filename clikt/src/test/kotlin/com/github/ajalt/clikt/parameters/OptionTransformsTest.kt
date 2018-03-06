package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import org.junit.Test

class OptionTransformsTest {

    @Test
    fun `int`() = parameterized(
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
    fun `int default`() = parameterized(
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
}
