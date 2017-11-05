package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.softly
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test

private var intArg1: Int = -111111111
private var intArg2: Int = -222222222
private var intArg3: Int = -333333333
private var intListArg: List<Int> = emptyList()
private var anyArg: Any? = null

private fun String.f0() {}

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

@ClicktCommand("f12name")
private fun f12(@IntOption("--yy", "-y") y: Int) {
    intArg2 = y
}

@AddVersionOption("1.2.3")
private fun f13(@IntOption("--xx", "-x") x: Int) {
    intArg1 = x
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

    fun f3(@IntArgument x: Int, @IntArgument y: Int) {
        (1).f3b(x, y)
    }

    private fun Int.f3b(x: Int, y: Int) {
        f2(this + x, y)
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
    fun `one option`() = parameterized(
            row(listOf("--xx", "3")),
            row(listOf("--xx=3")),
            row(listOf("-x", "3")),
            row(listOf("-x3"))) { (argv) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f1)

        assertThat(intArg1).called("x").isEqualTo(3)
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
    fun `two options`() = parameterized(
            row(listOf("--xx", "3", "--yy", "4")),
            row(listOf("--xx", "3", "-y", "4")),
            row(listOf("-x", "3", "--yy", "4")),
            row(listOf("-x3", "--yy", "4")),
            row(listOf("--xx", "3", "-y4")),
            row(listOf("--xx=3", "--yy=4")),
            row(listOf("-x3", "--yy=4")),
            row(listOf("-x", "3", "-y", "4")),
            row(listOf("-x3", "-y", "4")),
            row(listOf("-x", "3", "-y4")),
            row(listOf("-x3", "-y4"))
    ) { (argv) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f2)

        assertThat(intArg2).called("x").isEqualTo(3)
        assertThat(intArg3).called("y").isEqualTo(4)
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
    fun `two options first default`() = parameterized(
            row(listOf("--yy", "4")),
            row(listOf("--yy=4")),
            row(listOf("-y", "4")),
            row(listOf("-y4"))
    ) { (argv) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f2)

        assertThat(intArg2).called("x").isEqualTo(0)
        assertThat(intArg3).called("y").isEqualTo(4)
    }

    @Test
    fun `two options second default`() = parameterized(
            row(listOf("--xx", "3")),
            row(listOf("--xx=3")),
            row(listOf("-x", "3")),
            row(listOf("-x3"))
    ) { (argv) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f2)

        assertThat(intArg2).called("x").isEqualTo(3)
        assertThat(intArg3).called("y").isEqualTo(0)
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
    fun `bound member function and extension`() {
        parser.parse(arrayOf("3", "5"), C()::f3)
        softly {
            assertThat(intArg1).isEqualTo(4)
            assertThat(intArg2).isEqualTo(5)
        }
    }

    @Test
    fun `unbound member function and extension`() {
        softly {
            assertThatThrownBy { parser.parse(arrayOf("3", "5"), C::f3) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessageContaining("unbound method")
            assertThatThrownBy { parser.parse(arrayOf(), String::f0) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessageContaining("unbound method")
        }
    }

    @Test
    fun `subcommand`() {
        parser.addCommand(::f1, ::f2)

        parameterized(
                row(listOf("--xx", "2", "f2", "--xx", "3", "--yy", "4")),
                row(listOf("--xx", "2", "f2", "--xx", "3", "-y", "4")),
                row(listOf("--xx", "2", "f2", "-x", "3", "--yy", "4")),
                row(listOf("--xx", "2", "f2", "-x3", "--yy", "4")),
                row(listOf("--xx", "2", "f2", "--xx", "3", "-y4")),
                row(listOf("--xx", "2", "f2", "--xx=3", "--yy=4")),
                row(listOf("--xx", "2", "f2", "-x3", "--yy=4")),
                row(listOf("--xx", "2", "f2", "-x", "3", "-y", "4")),
                row(listOf("--xx", "2", "f2", "-x3", "-y", "4")),
                row(listOf("--xx", "2", "f2", "-x", "3", "-y4")),
                row(listOf("--xx", "2", "f2", "-x3", "-y4")),

                row(listOf("--xx=2", "f2", "--xx", "3", "--yy", "4")),
                row(listOf("--xx=2", "f2", "--xx", "3", "-y", "4")),
                row(listOf("--xx=2", "f2", "-x", "3", "--yy", "4")),
                row(listOf("--xx=2", "f2", "-x3", "--yy", "4")),
                row(listOf("--xx=2", "f2", "--xx", "3", "-y4")),
                row(listOf("--xx=2", "f2", "--xx=3", "--yy=4")),
                row(listOf("--xx=2", "f2", "-x3", "--yy=4")),
                row(listOf("--xx=2", "f2", "-x", "3", "-y", "4")),
                row(listOf("--xx=2", "f2", "-x3", "-y", "4")),
                row(listOf("--xx=2", "f2", "-x", "3", "-y4")),
                row(listOf("--xx=2", "f2", "-x3", "-y4")),

                row(listOf("-x", "2", "f2", "--xx", "3", "--yy", "4")),
                row(listOf("-x", "2", "f2", "--xx", "3", "-y", "4")),
                row(listOf("-x", "2", "f2", "-x", "3", "--yy", "4")),
                row(listOf("-x", "2", "f2", "-x3", "--yy", "4")),
                row(listOf("-x", "2", "f2", "--xx", "3", "-y4")),
                row(listOf("-x", "2", "f2", "--xx=3", "--yy=4")),
                row(listOf("-x", "2", "f2", "-x3", "--yy=4")),
                row(listOf("-x", "2", "f2", "-x", "3", "-y", "4")),
                row(listOf("-x", "2", "f2", "-x3", "-y", "4")),
                row(listOf("-x", "2", "f2", "-x", "3", "-y4")),
                row(listOf("-x", "2", "f2", "-x3", "-y4")),

                row(listOf("-x2", "f2", "--xx", "3", "--yy", "4")),
                row(listOf("-x2", "f2", "--xx", "3", "-y", "4")),
                row(listOf("-x2", "f2", "-x", "3", "--yy", "4")),
                row(listOf("-x2", "f2", "-x3", "--yy", "4")),
                row(listOf("-x2", "f2", "--xx", "3", "-y4")),
                row(listOf("-x2", "f2", "--xx=3", "--yy=4")),
                row(listOf("-x2", "f2", "-x3", "--yy=4")),
                row(listOf("-x2", "f2", "-x", "3", "-y", "4")),
                row(listOf("-x2", "f2", "-x3", "-y", "4")),
                row(listOf("-x2", "f2", "-x", "3", "-y4")),
                row(listOf("-x2", "f2", "-x3", "-y4"))
        ) { (argv) ->
            setup()
            parser.parse(argv.toTypedArray(), ::f1)

            assertThat(intArg1).called("f1 x").isEqualTo(2)
            assertThat(intArg2).called("f2 x").isEqualTo(3)
            assertThat(intArg3).called("f2 y").isEqualTo(4)
        }
    }

    @Test
    fun `subcommand with custom name`() {
        parser.addCommand(::f1, ::f12)
        parser.parse(arrayOf("-x2", "f12name", "-y", "3"), ::f1)

        softly {
            assertThat(intArg1).isEqualTo(2)
            assertThat(intArg2).isEqualTo(3)

            assertThatThrownBy { parser.parse(arrayOf("-x2", "f12", "-y", "3"), ::f1) }
                    .isInstanceOf(NoSuchOption::class.java)
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
    fun `one argument nargs=-1`() = parameterized(
            row(emptyList(), emptyList()),
            row(listOf("3"), listOf(3)),
            row(listOf("3", "4"), listOf(3, 4)),
            row(listOf("3", "4", "4"), listOf(3, 4, 4))
    ) { (argv, expected) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f6)

        assertThat(intListArg).isEqualTo(expected)
    }

    @Test
    fun `two argument nargs=-1,1`() = parameterized(
            row(emptyList(), emptyList(), 0),
            row(listOf("3"), listOf(), 3),
            row(listOf("3", "4"), listOf(3), 4),
            row(listOf("3", "4", "4"), listOf(3, 4), 4)
    ) { (argv, listArg, intArg) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f7)

        assertThat(intListArg).called("x").isEqualTo(listArg)
        assertThat(intArg1).called("y").isEqualTo(intArg)
    }

    @Test
    fun `two argument nargs=1,-1`() = parameterized(
            row(emptyList(), 0, emptyList()),
            row(listOf("3"), 3, listOf()),
            row(listOf("3", "4"), 3, listOf(4)),
            row(listOf("3", "4", "5"), 3, listOf(4, 5))
    ) { (argv, intArg, listArg) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f8)

        assertThat(intArg1).called("x").isEqualTo(intArg)
        assertThat(intListArg).called("y").isEqualTo(listArg)
    }

    @Test
    fun `two options inferred names`() = parameterized(
            row(emptyList(), 0, 0),
            row(listOf("--x", "3", "--yy", "4"), 3, 4),
            row(listOf("--x", "3"), 3, 0),
            row(listOf("--x=3"), 3, 0),
            row(listOf("--yy", "4"), 0, 4),
            row(listOf("--yy=4"), 0, 4)
    ) { (argv, arg1, arg2) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f9)

        assertThat(intArg1).called("x").isEqualTo(arg1)
        assertThat(intArg2).called("y").isEqualTo(arg2)
    }

    @Test
    fun `passed context`() {
        parser.parse(arrayOf("3", "4"), ::f10)
        assertThat(intArg1).isEqualTo(3)
        assertThat(intArg2).isEqualTo(4)
        assertThat(anyArg).asList().hasSize(3).allMatch { it is Context }
    }

    @Test
    fun `flag options`() = parameterized(
            row(emptyList(), 0, 0, 0),
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
            row(listOf("-xz3"), 1, 0, 3)
    ) { (argv, x, y, z) ->
        setup()
        parser.parse(argv.toTypedArray(), ::f11)

        assertThat(intArg1).called("x").isEqualTo(x)
        assertThat(intArg2).called("y").isEqualTo(y)
        assertThat(intArg3).called("z").isEqualTo(z)
    }

    @Test
    fun `version param option value`() {
        parser.parse(arrayOf("--xx=3"), ::f13)
        assertThat(intArg1).isEqualTo(3)
    }

    @Test
    fun `version param no values`() {
        parser.parse(arrayOf(), ::f13)
        assertThat(intArg1).isEqualTo(0)
    }

    @Test
    fun `version param version option`() {
        assertThatThrownBy { parser.parse(arrayOf("--version"), ::f13) }
                .isInstanceOf(PrintMessage::class.java)
                .hasMessage("f13, version 1.2.3")
    }

    @Test
    fun `version param version and option value`() {
        assertThatThrownBy { parser.parse(arrayOf("--version", "--xx=3"), ::f13) }
                .isInstanceOf(PrintMessage::class.java)
                .hasMessage("f13, version 1.2.3")
        assertThat(intArg1).isEqualTo(-111111111)
    }
}
