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
private var anyArg1: Any? = null
private var anyArg2: Any? = null

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
    anyArg1 = listOf(c1, c2, c3)
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

private fun f14(@IntOption("--xx", "-x", nargs = 2) x: List<Int>?,
                @IntOption("--yy", "-y", nargs = 2) y: List<Int>?) {
    intArg1 = x?.sum() ?: -1
    intArg2 = y?.sum() ?: -1
}

private fun f15(@StringOption("--xx", "-x") x: String?) {
    anyArg1 = x
}

private fun f16(@StringOption(nargs = 2) xx: List<String>?) {
    anyArg1 = xx
}

private fun f17(@StringOption(default = "default") xx: String) {
    anyArg1 = xx
}

private fun f18(@StringOption("--xx", "-x") x: String?, @StringArgument y: String) {
    anyArg1 = x
    anyArg2 = y
}

private fun f19(@CountedOption("--xx", "-x") x: Int) {
    intArg1 = x
}

private fun f20(@FlagOption("--xx/--no-xx", "-x/-X", "--XX/", "/--NO-XX") x: Boolean) {
    anyArg1 = x
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
    @Before
    fun setup() {
        intArg1 = -111111111
        intArg2 = -222222222
        intArg3 = -333333333
        intListArg = emptyList()
        anyArg1 = null
        anyArg2 = null
    }

    @Test
    fun `zero options`() {
        Command.build(::f0).parse(emptyArray())
    }

    @Test
    fun `one option`() = parameterized(
            row(listOf("--xx", "3")),
            row(listOf("--xx=3")),
            row(listOf("-x", "3")),
            row(listOf("-x3"))) { (argv) ->
        setup()
        Command.build(::f1).parse(argv.toTypedArray())

        assertThat(intArg1).called("x").isEqualTo(3)
    }

    @Test
    fun `two options single name`() {
        Command.build(::f3).parse(arrayOf("-x", "3", "--yy", "4"))

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
        Command.build(::f2).parse(argv.toTypedArray())

        assertThat(intArg2).called("x").isEqualTo(3)
        assertThat(intArg3).called("y").isEqualTo(4)
    }

    @Test
    fun `two options both default`() {
        Command.build(::f2).parse(emptyArray())

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
        Command.build(::f2).parse(argv.toTypedArray())

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
        Command.build(::f2).parse(argv.toTypedArray())

        assertThat(intArg2).called("x").isEqualTo(3)
        assertThat(intArg3).called("y").isEqualTo(0)
    }

    @Test
    fun `companion member function`() {
        Command.build(C.Companion::f1).parse(arrayOf("--x", "3", "4"))
        softly {
            assertThat(intArg1).isEqualTo(3)
            assertThat(intArg2).isEqualTo(4)
        }
    }

    @Test
    fun `member function`() {
        Command.build(C()::f2).parse(arrayOf("3", "--yy", "4"))
        softly {
            assertThat(intArg1).isEqualTo(3)
            assertThat(intArg2).isEqualTo(4)
        }
    }

    @Test
    fun `bound member function and extension`() {
        Command.build(C()::f3).parse(arrayOf("3", "5"))
        softly {
            assertThat(intArg1).isEqualTo(4)
            assertThat(intArg2).isEqualTo(5)
        }
    }

    @Test
    fun `unbound member function and extension`() {
        softly {
            assertThatThrownBy { Command.build(C::f3).parse(arrayOf("3", "5")) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessageContaining("unbound method")
            assertThatThrownBy { Command.build(String::f0).parse(arrayOf()) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessageContaining("unbound method")
        }
    }

    @Test
    fun `subcommand`() {
        val commmand = Command.build(::f1) { subcommand(::f2) }

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
            commmand.parse(argv.toTypedArray())

            assertThat(intArg1).called("f1 x").isEqualTo(2)
            assertThat(intArg2).called("f2 x").isEqualTo(3)
            assertThat(intArg3).called("f2 y").isEqualTo(4)
        }
    }

    @Test
    fun `multiple subcommands`() {
        val commmand = Command.build(::f1) {
            subcommand(::f2)
            subcommand(::f5)
        }

        parameterized(
                row(listOf("-x1", "f5", "2", "3"), 2, 3, -333333333),
                row(listOf("-x1", "f2", "-x2", "-y3"), 1, 2, 3)
        ) { (argv, x, y, z) ->
            setup()
            commmand.parse(argv.toTypedArray())

            assertThat(intArg1).called("x").isEqualTo(x)
            assertThat(intArg2).called("y").isEqualTo(y)
            assertThat(intArg3).called("z").isEqualTo(z)
        }

    }

    @Test
    fun `subcommand with custom name`() {
        val commmand = Command.build(::f1) { subcommand(::f12) }
        commmand.parse(arrayOf("-x2", "f12name", "-y", "3"))

        softly {
            assertThat(intArg1).isEqualTo(2)
            assertThat(intArg2).isEqualTo(3)

            assertThatThrownBy { commmand.parse(arrayOf("-x2", "f12", "-y", "3")) }
                    .isInstanceOf(NoSuchOption::class.java)
        }
    }

    @Test
    fun `argument before subcommand`() {
        Command.build(::f6) { subcommand(::f1) }
                .parse(arrayOf("123", "456", "f1", "-x33"))

        softly {
            assertThat(intListArg).isEqualTo(listOf(123, 456))
            assertThat(intArg1).isEqualTo(33)
        }
    }

    @Test
    fun `one argument`() = parameterized(
            row(arrayOf("3"), 3),
            row(arrayOf(), 0)
    ) { (argv, value) ->
        Command.build(::f4).parse(argv)
        assertThat(intArg1).isEqualTo(value)
    }

    @Test
    fun `one argument nargs=2`() {
        Command.build(::f5).parse(arrayOf("33", "44"))
        softly {
            assertThat(intArg1).isEqualTo(33)
            assertThat(intArg2).isEqualTo(44)
        }
    }

    @Test
    fun `one argument nargs=-1`() = parameterized(
            row(emptyList(), emptyList()),
            row(listOf("3"), listOf(3)),
            row(listOf("3", "4"), listOf(3, 4)),
            row(listOf("3", "4", "4"), listOf(3, 4, 4))
    ) { (argv, expected) ->
        setup()
        Command.build(::f6).parse(argv.toTypedArray())

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
        Command.build(::f7).parse(argv.toTypedArray())

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
        Command.build(::f8).parse(argv.toTypedArray())

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
        Command.build(::f9).parse(argv.toTypedArray())

        assertThat(intArg1).called("x").isEqualTo(arg1)
        assertThat(intArg2).called("y").isEqualTo(arg2)
    }

    @Test
    fun `passed context`() {
        Command.build(::f10).parse(arrayOf("3", "4"))
        softly {
            assertThat(intArg1).isEqualTo(3)
            assertThat(intArg2).isEqualTo(4)
            assertThat(anyArg1).asList().hasSize(3).allMatch { it is Context }
        }
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
        Command.build(::f11).parse(argv.toTypedArray())

        assertThat(intArg1).called("x").isEqualTo(x)
        assertThat(intArg2).called("y").isEqualTo(y)
        assertThat(intArg3).called("z").isEqualTo(z)
    }

    @Test
    fun `flag options with off switch`() = parameterized(
            row(emptyList(), false),
            row(listOf("-x"), true),
            row(listOf("-x", "--no-xx"), false),
            row(listOf("-xX"), false),
            row(listOf("-xXx"), true),
            row(listOf("--xx", "--no-xx", "-xX", "--XX", "--NO-XX", "-x"), true)
    ) { (argv, value) ->
        setup()
        Command.build(::f20).parse(argv.toTypedArray())

        assertThat(anyArg1).isEqualTo(value)
    }

    @Test
    fun `version param option value`() {
        Command.build(::f13).parse(arrayOf("--xx=3"))
        assertThat(intArg1).isEqualTo(3)
    }

    @Test
    fun `version param no values`() {
        Command.build(::f13).parse(arrayOf())
        assertThat(intArg1).isEqualTo(0)
    }

    @Test
    fun `version param version option`() {
        assertThatThrownBy { Command.build(::f13).parse(arrayOf("--version")) }
                .isInstanceOf(PrintMessage::class.java)
                .hasMessage("f13, version 1.2.3")
    }

    @Test
    fun `version param version and option value`() {
        assertThatThrownBy { Command.build(::f13).parse(arrayOf("--version", "--xx=3")) }
                .isInstanceOf(PrintMessage::class.java)
                .hasMessage("f13, version 1.2.3")
        assertThat(intArg1).isEqualTo(-111111111)
    }

    @Test
    fun `two options nargs=2`() = parameterized(
            row(emptyList(), -1, -1),
            row(listOf("--xx", "1", "3"), 4, -1),
            row(listOf("--yy", "5", "7"), -1, 12),
            row(listOf("--xx", "1", "3", "--yy", "5", "7"), 4, 12),
            row(listOf("--xx", "1", "3", "-y", "5", "7"), 4, 12),
            row(listOf("-x", "1", "3", "--yy", "5", "7"), 4, 12),
            row(listOf("-x1", "3", "--yy", "5", "7"), 4, 12),
            row(listOf("--xx", "1", "3", "-y5", "7"), 4, 12),
            row(listOf("--xx=1", "3", "--yy=5", "7"), 4, 12),
            row(listOf("-x1", "3", "--yy=5", "7"), 4, 12),
            row(listOf("-x", "1", "3", "-y", "5", "7"), 4, 12),
            row(listOf("-x1", "3", "-y", "5", "7"), 4, 12),
            row(listOf("-x", "1", "3", "-y5", "7"), 4, 12),
            row(listOf("-x1", "3", "-y5", "7"), 4, 12)
    ) { (argv, arg1, arg2) ->
        setup()
        Command.build(::f14).parse(argv.toTypedArray())

        assertThat(intArg1).called("x").isEqualTo(arg1)
        assertThat(intArg2).called("y").isEqualTo(arg2)
    }

    @Test
    fun `two options nargs=2 usage errors`() {
        assertThatThrownBy { Command.build(::f14).parse(arrayOf("-x")) }
                .isInstanceOf(BadOptionUsage::class.java)
                .hasMessage("-x option requires 2 arguments")

        assertThatThrownBy { Command.build(::f14).parse(arrayOf("--y", "1", "2", "3")) }
                .isInstanceOf(UsageError::class.java)
    }

    @Test
    fun `string option nullable`() = parameterized(
            row(listOf<String>(), null),
            row(listOf("--xx", "3"), "3"),
            row(listOf("--xx=3"), "3"),
            row(listOf("-x", "3"), "3"),
            row(listOf("-x3"), "3")
    ) { (argv, value) ->
        setup()
        Command.build(::f15).parse(argv.toTypedArray())

        assertThat(anyArg1).called("x").isEqualTo(value)
    }

    @Test
    fun `string option nargs=2`() = parameterized(
            row(listOf<String>(), null),
            row(listOf("--xx", "foo", "bar"), listOf("foo", "bar"))
    ) { (argv, value) ->
        setup()
        Command.build(::f16).parse(argv.toTypedArray())

        assertThat(anyArg1).called("x").isEqualTo(value)
    }

    @Test
    fun `string option default`() = parameterized(
            row(listOf(), "default"),
            row(listOf("--xx", "foo"), "foo")
    ) { (argv, value) ->
        setup()
        Command.build(::f17).parse(argv.toTypedArray())

        assertThat(anyArg1).called("x").isEqualTo(value)
    }

    @Test
    fun `value -- before argument`() {
        Command.build(::f18).parse(arrayOf("--xx", "--xx", "--", "--xx"))
        softly {
            assertThat(anyArg1).isEqualTo("--xx")
            assertThat(anyArg2).isEqualTo("--xx")
        }
    }

    @Test
    fun `value -- after argument`() {
        Command.build(::f18).parse(arrayOf("--xx", "--xx", "bar", "--"))
        softly {
            assertThat(anyArg1).isEqualTo("--xx")
            assertThat(anyArg2).isEqualTo("bar")
        }
    }

    @Test
    fun `value -- not given`() {
        val command = Command.build(::f18)
        assertThatThrownBy { command.parse(arrayOf("--xx")) }
                .isInstanceOf(BadOptionUsage::class.java)

        assertThatThrownBy { command.parse(arrayOf("--xx", "--xx", "--yy")) }
                .isInstanceOf(NoSuchOption::class.java)
    }

    @Test
    fun `value -- before subcommand`() {
        Command.build(::f18) {
            subcommand(::f1)
        }.parse(arrayOf("--xx", "--xx", "--", "--yy", "f1", "--xx", "33"))
        softly {
            assertThat(anyArg1).isEqualTo("--xx")
            assertThat(anyArg2).isEqualTo("--yy")
            assertThat(intArg1).isEqualTo(33)
        }
    }


    @Test
    fun `counted option`() = parameterized(
            row(listOf(), 0),
            row(listOf("--xx"), 1),
            row(listOf("-xx"), 2),
            row(listOf("--xx", "-xxx", "-x", "-xx"), 7)) { (argv, value) ->
        setup()
        Command.build(::f19).parse(argv.toTypedArray())

        assertThat(intArg1).isEqualTo(value)
    }
}
