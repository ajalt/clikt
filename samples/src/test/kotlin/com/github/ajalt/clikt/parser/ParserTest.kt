package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.IntOption
import com.github.ajalt.clikt.testing.softForEach
import com.github.ajalt.clikt.testing.softly
import org.junit.Before
import org.junit.Test

private var intArg1: Int = -111111111
private var intArg2: Int = -222222222
private var intArg3: Int = -333333333

private fun f1(@IntOption("--xx", "-x") x: Int) {
    intArg1 = x
}

private fun f2(@IntOption("--xx", "-x") x: Int, @IntOption("--yy", "-y") y: Int) {
    intArg2 = x
    intArg3 = y
}

class ParserTest {
    private val parser = Parser()

    @Before
    fun setup() {
        intArg1 = -111111111
        intArg2 = -222222222
        intArg3 = -333333333
    }

    @Test
    fun `one option`() {
        softForEach(arrayOf("--xx", "3"),
                arrayOf("--xx=3"),
                arrayOf("-x", "3"),
                arrayOf("-x3")) {
            setup()
            parser.parse(it, ::f1)

            assertThat(intArg1).called("x").isEqualTo(3)
        }
    }

    @Test
    fun `two options`() {
        softForEach(arrayOf("--xx", "3", "--yy", "4"),
                arrayOf("--xx", "3", "-y", "4"),
                arrayOf("-x", "3", "--yy", "4"),
                arrayOf("-x3", "--yy", "4"),
                arrayOf("--xx", "3", "-y4"),
                arrayOf("--xx=3", "--yy=4"),
                arrayOf("-x3", "--yy=4"),
                arrayOf("-x", "3", "-y", "4"),
                arrayOf("-x3", "-y", "4"),
                arrayOf("-x", "3", "-y4"),
                arrayOf("-x3", "-y4")) {
            setup()
            parser.parse(it, ::f2)

            assertThat(intArg2).called("x").isEqualTo(3)
            assertThat(intArg3).called("y").isEqualTo(4)
        }
    }

    @Test
    fun `two options both default`() {
        parser.parse(emptyArray(), ::f2)
        softly {
            assertThat(intArg2).isEqualTo(0)
            assertThat(intArg3).isEqualTo(0)
        }
    }

    @Test
    fun `two options first default`() {
        softForEach(
                arrayOf("--yy", "4"),
                arrayOf("--yy=4"),
                arrayOf("-y", "4"),
                arrayOf("-y4")) {
            setup()
            parser.parse(it, ::f2)

            assertThat(intArg2).called("x").isEqualTo(0)
            assertThat(intArg3).called("y").isEqualTo(4)
        }
    }

    @Test
    fun `two options second default`() {
        softForEach(
                arrayOf("--xx", "3"),
                arrayOf("--xx=3"),
                arrayOf("-x", "3"),
                arrayOf("-x3")) {
            setup()
            parser.parse(it, ::f2)

            assertThat(intArg2).called("x").isEqualTo(3)
            assertThat(intArg3).called("y").isEqualTo(0)
        }
    }

    @Test
    fun `subcommand`() {
        parser.addCommand(::f1, ::f2)

        softForEach(
                arrayOf("--xx", "2", "f2", "--xx", "3", "--yy", "4"),
                arrayOf("--xx", "2", "f2", "--xx", "3", "-y", "4"),
                arrayOf("--xx", "2", "f2", "-x", "3", "--yy", "4"),
                arrayOf("--xx", "2", "f2", "-x3", "--yy", "4"),
                arrayOf("--xx", "2", "f2", "--xx", "3", "-y4"),
                arrayOf("--xx", "2", "f2", "--xx=3", "--yy=4"),
                arrayOf("--xx", "2", "f2", "-x3", "--yy=4"),
                arrayOf("--xx", "2", "f2", "-x", "3", "-y", "4"),
                arrayOf("--xx", "2", "f2", "-x3", "-y", "4"),
                arrayOf("--xx", "2", "f2", "-x", "3", "-y4"),
                arrayOf("--xx", "2", "f2", "-x3", "-y4"),

                arrayOf("--xx=2", "f2", "--xx", "3", "--yy", "4"),
                arrayOf("--xx=2", "f2", "--xx", "3", "-y", "4"),
                arrayOf("--xx=2", "f2", "-x", "3", "--yy", "4"),
                arrayOf("--xx=2", "f2", "-x3", "--yy", "4"),
                arrayOf("--xx=2", "f2", "--xx", "3", "-y4"),
                arrayOf("--xx=2", "f2", "--xx=3", "--yy=4"),
                arrayOf("--xx=2", "f2", "-x3", "--yy=4"),
                arrayOf("--xx=2", "f2", "-x", "3", "-y", "4"),
                arrayOf("--xx=2", "f2", "-x3", "-y", "4"),
                arrayOf("--xx=2", "f2", "-x", "3", "-y4"),
                arrayOf("--xx=2", "f2", "-x3", "-y4"),

                arrayOf("-x", "2", "f2", "--xx", "3", "--yy", "4"),
                arrayOf("-x", "2", "f2", "--xx", "3", "-y", "4"),
                arrayOf("-x", "2", "f2", "-x", "3", "--yy", "4"),
                arrayOf("-x", "2", "f2", "-x3", "--yy", "4"),
                arrayOf("-x", "2", "f2", "--xx", "3", "-y4"),
                arrayOf("-x", "2", "f2", "--xx=3", "--yy=4"),
                arrayOf("-x", "2", "f2", "-x3", "--yy=4"),
                arrayOf("-x", "2", "f2", "-x", "3", "-y", "4"),
                arrayOf("-x", "2", "f2", "-x3", "-y", "4"),
                arrayOf("-x", "2", "f2", "-x", "3", "-y4"),
                arrayOf("-x", "2", "f2", "-x3", "-y4"),

                arrayOf("-x2", "f2", "--xx", "3", "--yy", "4"),
                arrayOf("-x2", "f2", "--xx", "3", "-y", "4"),
                arrayOf("-x2", "f2", "-x", "3", "--yy", "4"),
                arrayOf("-x2", "f2", "-x3", "--yy", "4"),
                arrayOf("-x2", "f2", "--xx", "3", "-y4"),
                arrayOf("-x2", "f2", "--xx=3", "--yy=4"),
                arrayOf("-x2", "f2", "-x3", "--yy=4"),
                arrayOf("-x2", "f2", "-x", "3", "-y", "4"),
                arrayOf("-x2", "f2", "-x3", "-y", "4"),
                arrayOf("-x2", "f2", "-x", "3", "-y4"),
                arrayOf("-x2", "f2", "-x3", "-y4")) {
            setup()
            parser.parse(it, ::f1)

            assertThat(intArg1).called("f1 x").isEqualTo(2)
            assertThat(intArg2).called("f2 x").isEqualTo(3)
            assertThat(intArg3).called("f2 y").isEqualTo(4)
        }
    }
}
