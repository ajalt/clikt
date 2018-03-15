package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RangeTest {
    @Test
    fun `restrictTo option min`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(min = 1)
            override fun run() = Unit
        }

        softly {
            C().apply {
                parse(splitArgv(""))
                assertThat(x).isNull()
            }
            C().apply {
                parse(splitArgv("-x1"))
                assertThat(x).isEqualTo(1)
            }
            C().apply {
                parse(splitArgv("-x3"))
                assertThat(x).isEqualTo(3)
            }
            assertThrows<BadParameterValue> { C().parse(splitArgv("--xx=0")) }
                    .hasMessage("Invalid value for \"--xx\": 0 is smaller than the minimum valid value of 1.")
        }
    }

    @Test
    fun `restrictTo option min clamp`() = parameterized(
            row("", null),
            row("--xx 3", 3),
            row("--xx=1", 1),
            row("--xx -123", 1),
            row("-x0", 1)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(min = 1, clamp = true)
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `restrictTo option max`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(max = 1)
            override fun run() = Unit
        }

        softly {
            C().apply {
                parse(splitArgv(""))
                assertThat(x).isNull()
            }
            C().apply {
                parse(splitArgv("-x1"))
                assertThat(x).isEqualTo(1)
            }
            C().apply {
                parse(splitArgv("-x0"))
                assertThat(x).isEqualTo(0)
            }
            assertThrows<BadParameterValue> { C().parse(splitArgv("--xx=2")) }
                    .hasMessage("Invalid value for \"--xx\": 2 is larger than the maximum valid value of 1.")
        }
    }

    @Test
    fun `restrictTo option max clamp`() = parameterized(
            row("", null),
            row("--xx 0", 0),
            row("--xx=1", 1),
            row("--xx 123", 1),
            row("-x2", 1)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(max = 1, clamp = true)
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `restrictTo option range`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(1..2)
            override fun run() = Unit
        }

        softly {
            C().apply {
                parse(splitArgv(""))
                assertThat(x).isNull()
            }
            C().apply {
                parse(splitArgv("-x1"))
                assertThat(x).isEqualTo(1)
            }
            C().apply {
                parse(splitArgv("-x2"))
                assertThat(x).isEqualTo(2)
            }
            assertThrows<BadParameterValue> { C().parse(splitArgv("--xx=3")) }
                    .hasMessage("Invalid value for \"--xx\": 3 is not in the valid range of 1 to 2.")
            assertThrows<BadParameterValue> { C().parse(splitArgv("-x0")) }
                    .hasMessage("Invalid value for \"-x\": 0 is not in the valid range of 1 to 2.")
        }
    }

    @Test
    fun `restrictTo option default`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().default(2).restrictTo(1..2)
            val y by option("-y", "--yy").int().default(3).restrictTo(min = 3, max = 4)
            override fun run() = Unit
        }

        softly {
            C().apply {
                parse(splitArgv(""))
                assertThat(x).isEqualTo(2)
                assertThat(y).isEqualTo(3)
            }
            C().apply {
                parse(splitArgv("-x1"))
                assertThat(x).isEqualTo(1)
                assertThat(y).isEqualTo(3)
            }
            C().apply {
                parse(splitArgv("-y4"))
                assertThat(x).isEqualTo(2)
                assertThat(y).isEqualTo(4)
            }
            assertThrows<BadParameterValue> { C().parse(splitArgv("--xx=3")) }
                    .hasMessage("Invalid value for \"--xx\": 3 is not in the valid range of 1 to 2.")
            assertThrows<BadParameterValue> { C().parse(splitArgv("-y10")) }
                    .hasMessage("Invalid value for \"-y\": 10 is not in the valid range of 3 to 4.")
        }
    }

    @Test
    fun `restrictTo argument`() {
        class C : CliktCommand() {
            val x by argument().int().restrictTo(min = 1, max=2)
            val y by argument().int().restrictTo(3..4)
            val z by argument().int().optional().restrictTo(min=5, max=6)
            val w by argument().int().optional().restrictTo(7..8)
            override fun run() = Unit
        }

        softly {
            C().apply {
                parse(splitArgv("1 3 5 7"))
                assertThat(x).isEqualTo(1)
                assertThat(y).isEqualTo(3)
                assertThat(z).isEqualTo(5)
                assertThat(w).isEqualTo(7)
            }
            C().apply {
                parse(splitArgv("1 3"))
                assertThat(x).isEqualTo(1)
                assertThat(y).isEqualTo(3)
                assertThat(z).isEqualTo(null)
                assertThat(w).isEqualTo(null)
            }
            C().apply {
                parse(splitArgv("2 4 6 8"))
                assertThat(x).isEqualTo(2)
                assertThat(y).isEqualTo(4)
                assertThat(z).isEqualTo(6)
                assertThat(w).isEqualTo(8)
            }
            assertThrows<BadParameterValue> { C().parse(splitArgv("0 4 6 8")) }
                    .hasMessage("Invalid value for \"X\": 0 is not in the valid range of 1 to 2.")
            assertThrows<BadParameterValue> { C().parse(splitArgv("1 4 6 10")) }
                    .hasMessage("Invalid value for \"W\": 10 is not in the valid range of 7 to 8.")
        }
    }

    @Test
    fun `restrictTo argument clamp`() {
        class C : CliktCommand() {
            val x by argument().int().restrictTo(min = 1, max=2, clamp=true)
            val y by argument().int().restrictTo(3..4, clamp=true)
            val z by argument().int().optional().restrictTo(min=5, max=6, clamp=true)
            val w by argument().int().optional().restrictTo(7..8, clamp=true)
            override fun run() = Unit
        }

        softly {
            C().apply {
                parse(splitArgv("0 0 0 0"))
                assertThat(x).isEqualTo(1)
                assertThat(y).isEqualTo(3)
                assertThat(z).isEqualTo(5)
                assertThat(w).isEqualTo(7)
            }
            C().apply {
                parse(splitArgv("0 0"))
                assertThat(x).isEqualTo(1)
                assertThat(y).isEqualTo(3)
                assertThat(z).isEqualTo(null)
                assertThat(w).isEqualTo(null)
            }
            C().apply {
                parse(splitArgv("9 9 9 9"))
                assertThat(x).isEqualTo(2)
                assertThat(y).isEqualTo(4)
                assertThat(z).isEqualTo(6)
                assertThat(w).isEqualTo(8)
            }
        }
    }
}
