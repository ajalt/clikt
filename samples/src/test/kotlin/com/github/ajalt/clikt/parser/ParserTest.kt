package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.FlagOption
import com.github.ajalt.clikt.options.IntArgument
import com.github.ajalt.clikt.options.IntOption
import com.github.ajalt.clikt.options.PassContext
import com.github.ajalt.clikt.testing.softForEach
import com.github.ajalt.clikt.testing.softly
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

private var intArg1: Int = -111111111
private var intArg2: Int = -222222222
private var intArg3: Int = -333333333
private var intListArg: List<Int> = emptyList()
private var anyArg: Any? = null

private fun f0() {}

private fun f1(@IntOption("--xx", "-x") x: Int) {
    intArg1 = x
}

private fun f2(@IntOption("--xx", "-x") x: Int, @IntOption("--yy", "-y") y: Int) {
    intArg2 = x
    intArg3 = y
}

private fun f3(@IntOption("-x") x: Int, @IntOption("--yy") y: Int) {
    intArg2 = x
    intArg3 = y
}

private fun f4(@IntArgument x: Int) {
    intArg1 = x
}

private fun f5(@IntArgument(nargs = 2) x: List<Int>) {
    intArg1 = x[0]
    intArg2 = x[1]
}

private fun f6(@IntArgument(nargs = -1) x: List<Int>) {
    intListArg = x
}

private fun f7(@IntArgument(nargs = -1) x: List<Int>, @IntArgument(required = false) y: Int) {
    intListArg = x
    intArg1 = y
}

private fun f8(@IntArgument(required = false) x: Int, @IntArgument(nargs = -1) y: List<Int>) {
    intArg1 = x
    intListArg = y
}

private fun f9(@IntOption x: Int, @IntOption yy: Int) {
    intArg1 = x
    intArg2 = yy
}

private fun f10(@PassContext c1: Context, @IntArgument x: Int, @PassContext c2: Context,
                @IntArgument y: Int, @PassContext c3: Context) {
    intArg1 = x
    intArg2 = y
    anyArg = listOf(c1, c2, c3)
}

private fun f11(@FlagOption("--xx", "-x") x: Boolean, @FlagOption("--yy", "-y") y: Boolean,
                @IntOption("--zz", "-z") z: Int) {
    intArg1 = if (x) 1 else 0
    intArg2 = if (y) 1 else 0
    intArg3 = z
}

private class C {
    companion object {
        fun f1(@IntOption x: Int, @IntArgument yy: Int) {
            intArg1 = x
            intArg2 = yy
        }
    }

    fun f2(@IntArgument x: Int, @IntOption yy: Int) {
        intArg1 = x
        intArg2 = yy
    }
}

class ParserTest {
    private val parser = Parser()

    @Before
    fun setup() {
        intArg1 = -111111111
        intArg2 = -222222222
        intArg3 = -333333333
        intListArg = emptyList()
        anyArg = null
    }

    @Test
    fun `zero options`() {
        parser.parse(emptyArray(), ::f0)
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
    fun `two options single name`() {
        parser.parse(arrayOf("-x", "3", "--yy", "4"), ::f3)
        softly {
            assertThat(intArg2).isEqualTo(3)
            assertThat(intArg3).isEqualTo(4)
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
    fun `companion member function`() {
        parser.parse(arrayOf("--x", "3", "4"), C.Companion::f1)
        softly {
            assertThat(intArg1).isEqualTo(3)
            assertThat(intArg2).isEqualTo(4)
        }
    }

    @Test
    fun `member function`() {
        parser.parse(arrayOf("3", "--yy", "4"), C()::f2)
        softly {
            assertThat(intArg1).isEqualTo(3)
            assertThat(intArg2).isEqualTo(4)
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

    @Test
    fun `one argument`() {
        parser.parse(arrayOf("3"), ::f4)
        assertThat(intArg1).isEqualTo(3)
    }

    @Test
    fun `one argument default`() {
        parser.parse(arrayOf(), ::f4)
        assertThat(intArg1).isEqualTo(0)
    }

    @Test
    fun `one argument nargs=2`() {
        parser.parse(arrayOf("33", "44"), ::f5)
        assertThat(intArg1).isEqualTo(33)
        assertThat(intArg2).isEqualTo(44)
    }

    @Test
    fun `one argument nargs=-1`() {
        softForEach(emptyArray<String>() to emptyList(),
                arrayOf("3") to listOf(3),
                arrayOf("3", "4") to listOf(3, 4),
                arrayOf("3", "4", "4") to listOf(3, 4, 4)) {
            setup()
            parser.parse(it.first, ::f6)

            assertThat(intListArg).isEqualTo(it.second)
        }
    }

    @Test
    fun `two argument nargs=-1,1`() {
        softForEach(emptyList<String>() to (emptyList<Int>() to 0),
                listOf("3") to (listOf<Int>() to 3),
                listOf("3", "4") to (listOf(3) to 4),
                listOf("3", "4", "4") to (listOf(3, 4) to 4)) {
            setup()
            parser.parse(it.first.toTypedArray(), ::f7)

            assertThat(intListArg).called("x").isEqualTo(it.second.first)
            assertThat(intArg1).called("y").isEqualTo(it.second.second)
        }
    }

    @Test
    fun `two argument nargs=1,-1`() {
        softForEach(emptyList<String>() to (0 to emptyList()),
                listOf("3") to (3 to listOf()),
                listOf("3", "4") to (3 to listOf(4)),
                listOf("3", "4", "5") to (3 to listOf(4, 5))) {
            setup()
            parser.parse(it.first.toTypedArray(), ::f8)

            assertThat(intArg1).called("x").isEqualTo(it.second.first)
            assertThat(intListArg).called("y").isEqualTo(it.second.second)
        }
    }

    @Test
    fun `two options inferred names`() {
        softForEach(emptyList<String>() to (0 to 0),
                listOf("--x", "3", "--yy", "4") to (3 to 4),
                listOf("--x", "3") to (3 to 0),
                listOf("--x=3") to (3 to 0),
                listOf("--yy", "4") to (0 to 4),
                listOf("--yy=4") to (0 to 4)) {
            setup()
            parser.parse(it.first.toTypedArray(), ::f9)

            assertThat(intArg1).called("x").isEqualTo(it.second.first)
            assertThat(intArg2).called("y").isEqualTo(it.second.second)
        }
    }

    @Test
    fun `passed context`() {
        parser.parse(arrayOf("3", "4"), ::f10)
        assertThat(intArg1).isEqualTo(3)
        assertThat(intArg2).isEqualTo(4)
        assertThat(anyArg).asList().hasSize(3).allMatch { it is Context }
    }

    private data class row(val argv: List<String>, val x: Int, val y: Int, val z: Int)

    @Test
    fun `flag options`() {
        softForEach(row(emptyList(), 0, 0, 0),
                row(listOf("-x"), 1, 0, 0),
                row(listOf("--xx"), 1, 0, 0),
                row(listOf("-y"), 0, 1, 0),
                row(listOf("--yy"), 0, 1, 0),
                row(listOf("-xy"), 1, 1, 0),
                row(listOf("-yx"), 1, 1, 0),
                row(listOf("-x", "-y"), 1, 1, 0),
                row(listOf("--xx", "--yy"), 1, 1, 0),
                row(listOf("-x", "-y", "-z", "3"), 1, 1, 3),
                row(listOf("--xx", "--yy", "--zz", "3"), 1, 1, 3),
                row(listOf("-xy", "-z", "3"), 1, 1, 3),
                row(listOf("-xyz3"), 1, 1, 3),
                row(listOf("-xz3"), 1, 0, 3)) {
            setup()
            parser.parse(it.argv.toTypedArray(), ::f11)

            assertThat(intArg1).called("x").isEqualTo(it.x)
            assertThat(intArg2).called("y").isEqualTo(it.y)
            assertThat(intArg3).called("z").isEqualTo(it.z)
        }
    }
}
