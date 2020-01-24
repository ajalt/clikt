package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Assert.*
import org.junit.Test

@Suppress("unused")
class OptionTest {
    @Test
    fun inferEnvvar() = forall(
            row(setOf("--foo"), null, null, null),
            row(setOf("--bar"), null, "FOO", "FOO_BAR"),
            row(setOf("/bar"), null, "FOO", "FOO_BAR"),
            row(setOf("-b"), null, "FOO", "FOO_B"),
            row(setOf("-b", "--bar"), null, "FOO", "FOO_BAR")
    ) { names, envvar, prefix, expected ->
        inferEnvvar(names, envvar, prefix) shouldBe expected
    }

    @Test
    fun `zero options`() {
        TestCommand(called = true).parse(arrayOf())
    }

    @Test
    fun `no such option`() {
        class C : TestCommand(called = false) {
            val foo by option()
            val bar by option()
            val baz by option()
        }

        shouldThrow<NoSuchOption> {
            C().parse("--qux")
        }.message shouldBe "no such option: \"--qux\"."

        shouldThrow<NoSuchOption> {
            C().parse("--fo")
        }.message shouldBe "no such option: \"--fo\". Did you mean \"--foo\"?"

        shouldThrow<NoSuchOption> {
            C().parse("--ba")
        }.message shouldBe "no such option: \"--ba\". (Possible options: --bar, --baz)"
    }

    @Test
    fun `one option`() = forall(
            row("", null),
            row("--xx 3", "3"),
            row("--xx --xx", "--xx"),
            row("--xx=asd", "asd"),
            row("-x 4", "4"),
            row("-x -x", "-x"),
            row("-xfoo", "foo")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    fun `two options, one name each`() {
        class C : TestCommand() {
            val x by option("-x")
            val y by option("--yy")
            override fun run_() {
                x shouldBe "3"
                y shouldBe "4"
            }
        }
        C().parse("-x 3 --yy 4")
    }

    @Test
    fun `two options`() = forall(
            row("--xx 3 --yy 4", "3", "4"),
            row("-x 3 --yy 4", "3", "4"),
            row("-x3 --yy 4", "3", "4"),
            row("--xx 3 -y4", "3", "4"),
            row("--xx=3 --yy=4", "3", "4"),
            row("-x3 --yy=4", "3", "4"),
            row("-x 3 -y 4", "3", "4"),
            row("-x3 -y 4", "3", "4"),
            row("-x 3 -y4", "3", "4"),
            row("-x3 -y4", "3", "4"),
            row("--yy 4", null, "4"),
            row("--yy=4", null, "4"),
            row("-y 4", null, "4"),
            row("-y4", null, "4"),
            row("--xx 3", "3", null),
            row("--xx=3", "3", null),
            row("-x 3", "3", null),
            row("-x3", "3", null)
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }


    @Test
    fun `two options nvalues=2`() = forall(
            row("", null, null),
            row("--yy 5 7", null, "5" to "7"),
            row("--xx 1 3 --yy 5 7", "1" to "3", "5" to "7"),
            row("--xx 1 3 -y 5 7", "1" to "3", "5" to "7"),
            row("-x 1 3 --yy 5 7", "1" to "3", "5" to "7"),
            row("-x1 3 --yy 5 7", "1" to "3", "5" to "7"),
            row("--xx 1 3 -y5 7", "1" to "3", "5" to "7"),
            row("--xx=1 3 --yy=5 7", "1" to "3", "5" to "7"),
            row("-x1 3 --yy=5 7", "1" to "3", "5" to "7"),
            row("-x 1 3 -y 5 7", "1" to "3", "5" to "7"),
            row("-x1 3 -y 5 7", "1" to "3", "5" to "7"),
            row("-x 1 3 -y5 7", "1" to "3", "5" to "7"),
            row("-x1 3 -y5 7", "1" to "3", "5" to "7")
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx").pair()
            val y by option("-y", "--yy").pair()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    fun `two options nvalues=3`() {
        val xvalue = Triple("1", "2", "3")
        val yvalue = Triple("5", "6", "7")
        forall(
                row("", null, null),
                row("--yy 5 6 7", null, yvalue),
                row("--xx 1 2 3 --yy 5 6 7", xvalue, yvalue),
                row("--xx 1 2 3 -y 5 6 7", xvalue, yvalue),
                row("-x 1 2 3 --yy 5 6 7", xvalue, yvalue),
                row("-x1 2 3 --yy 5 6 7", xvalue, yvalue),
                row("--xx 1 2 3 -y5 6 7", xvalue, yvalue),
                row("--xx=1 2 3 --yy=5 6 7", xvalue, yvalue),
                row("-x1 2 3 --yy=5 6 7", xvalue, yvalue),
                row("-x 1 2 3 -y 5 6 7", xvalue, yvalue),
                row("-x1 2 3 -y 5 6 7", xvalue, yvalue),
                row("-x 1 2 3 -y5 6 7", xvalue, yvalue),
                row("-x1 2 3 -y5 6 7", xvalue, yvalue)
        ) { argv, ex, ey ->
            class C : TestCommand() {
                val x by option("-x", "--xx").triple()
                val y by option("-y", "--yy").triple()
                override fun run_() {
                    x shouldBe ex
                    y shouldBe ey
                }
            }

            C().parse(argv)
        }
    }

    @Test
    fun `two options nvalues=2 usage errors`() {
        class C : TestCommand(called = false) {
            val x by option("-x", "--xx").pair()
            val y by option("-y", "--yy").pair()
        }
        shouldThrow<IncorrectOptionValueCount> { C().parse("-x") }.message shouldBe
                "-x option requires 2 arguments"
        shouldThrow<UsageError> { C().parse("--yy foo bar baz") }.message shouldBe
                "Got unexpected extra argument (baz)"
    }

    @Test
    fun `two options with split`() = forall(
            row("", null, null),
            row("-x 5 -y a", listOf(5), listOf("a")),
            row("-x 5,6 -y a:b", listOf(5, 6), listOf("a", "b"))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x").int().split(",")
            val y by option("-y").split(Regex(":"))
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    fun `flag options`() = forall(
            row("", false, false, null),
            row("-xx", true, false, null),
            row("-xX", false, false, null),
            row("-Xx", true, false, null),
            row("-x --no-xx", false, false, null),
            row("--xx", true, false, null),
            row("--no-xx", false, false, null),
            row("--no-xx --xx", true, false, null),
            row("-y", false, true, null),
            row("--yy", false, true, null),
            row("-xy", true, true, null),
            row("-yx", true, true, null),
            row("-x -y", true, true, null),
            row("--xx --yy", true, true, null),
            row("-x -y -z foo", true, true, "foo"),
            row("--xx --yy --zz foo", true, true, "foo"),
            row("-xy -z foo", true, true, "foo"),
            row("-xyzxyz", true, true, "xyz"),
            row("-xXyzXyz", false, true, "Xyz"),
            row("-xzfoo", true, false, "foo")
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option("-x", "--xx").flag("-X", "--no-xx")
            val y by option("-y", "--yy").flag()
            val z by option("-z", "--zz")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @Test
    fun `switch options`() = forall(
            row("", null, -1),
            row("--xx -yy", 2, 4)) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option().switch("-x" to 1, "--xx" to 2)
            val y by option().switch("-y" to 3, "-yy" to 4).default(-1)
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    fun `counted options`() = forall(
            row("", 0, false, null),
            row("-x -x", 2, false, null),
            row("-xx", 2, false, null),
            row("-xx -xx", 4, false, null),
            row("--xx -y --xx", 2, true, null),
            row("--xx", 1, false, null),
            row("-y", 0, true, null),
            row("--yy", 0, true, null),
            row("-xy", 1, true, null),
            row("-yx", 1, true, null),
            row("-x -y", 1, true, null),
            row("--xx --yy", 1, true, null),
            row("-x -y -z foo", 1, true, "foo"),
            row("--xx --yy --zz foo", 1, true, "foo"),
            row("-xy -z foo", 1, true, "foo"),
            row("-xyx", 2, true, null),
            row("-xyxzxyz", 2, true, "xyz"),
            row("-xyzxyz", 1, true, "xyz"),
            row("-xzfoo", 1, false, "foo")
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option("-x", "--xx").counted()
            val y by option("-y", "--yy").flag()
            val z by option("-z", "--zz")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @Test
    fun `default option`() = forall(
            row("", "def"),
            row("-x4", "4")) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").default("def")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    fun `defaultLazy option`() = forall(
            row("", "default", true),
            row("-xbar", "bar", false)) { argv, expected, ec ->
        var called = false

        class C : TestCommand() {
            val x by option("-x", "--x").defaultLazy { called = true; "default" }
            override fun run_() {
                x shouldBe expected
                called shouldBe ec
            }
        }

        called shouldBe false
        C().parse(argv)
    }

    @Test
    fun `required option`() {
        class C : TestCommand() {
            val x by option().required()
            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().parse("--x=foo")

        shouldThrow<MissingParameter> {
            C().parse("")
        }.message shouldBe "Missing option \"--x\"."
    }

    @Test
    fun `multiple option default`() {
        class C : TestCommand() {
            val x by option().multiple()
            override fun run_() {
                x shouldBe listOf()
            }
        }

        C().parse("")
    }

    @Test
    fun `multiple option custom default`() {
        class C : TestCommand() {
            val x by option().multiple(listOf("foo"))
            override fun run_() {
                x shouldBe listOf("foo")
            }
        }

        C().parse("")
    }

    @Test
    fun `multiple with unique option default`() {
        val command = object : TestCommand() {
            val x by option().multiple().unique()
            override fun run_() {
                x shouldBe emptySet()
            }
        }

        command.parse("")
    }

    @Test
    fun `multiple with unique option custom default`() {
        val command = object : TestCommand() {
            val x by option().multiple(listOf("foo", "bar", "bar")).unique()
            override fun run_() {
                x shouldBe setOf("foo", "bar")
            }
        }

        command.parse("")
    }

    @Test
    fun `multiple with unique option parsed`() = forall(
            row("--arg foo", setOf("foo")),
            row("--arg foo --arg bar --arg baz", setOf("foo", "bar", "baz")),
            row("--arg foo --arg foo --arg foo", setOf("foo"))
    ) { argv, expected ->
        val command = object : TestCommand() {
            val arg by option().multiple().unique()
            override fun run_() {
                arg shouldBe expected
            }
        }
        command.parse(argv)
    }

    @Test
    fun `multiple required option`() {
        class C(called: Boolean) : TestCommand(called) {
            val x by option().multiple(required = true)
        }

        C(true).apply { parse("--x 1"); x shouldBe listOf("1") }
        C(true).apply { parse("--x 2 --x 3"); x shouldBe listOf("2", "3") }

        shouldThrow<MissingParameter> { C(false).parse("") }
                .message shouldBe "Missing option \"--x\"."
    }

    @Test
    fun `option metavars`() {
        class C : TestCommand() {
            val x by option()
            val y by option(metavar = "FOO").default("")
            val z by option(metavar = "FOO").convert("BAR") { it }
            val w by option().convert("BAR") { it }
            val u by option().flag()
            override fun run_() {
                _options.forEach {
                    if (it is EagerOption || // skip help option
                            "--x" in it.names && it.metavar == "TEXT" ||
                            "--y" in it.names && it.metavar == "FOO" ||
                            "--z" in it.names && it.metavar == "FOO" ||
                            "--w" in it.names && it.metavar == "BAR" ||
                            "--u" in it.names && it.metavar == null)
                    else fail("bad option $it")
                }
            }
        }

        C().parse("")
    }

    @Test
    fun `option validator basic`() {
        var called = false

        class C : TestCommand() {
            val x by option().validate {
                called = true
                require(it == "foo") { "invalid value $it" }
            }
        }

        with(C()) {
            parse("--x=foo")
            x shouldBe "foo"
        }
        assertTrue(called)

        called = false
        C().parse("")
        assertFalse(called)
    }

    @Test
    fun `option validator required`() {
        var called = false

        class C : TestCommand() {
            val x by option().required().validate {
                called = true
                require(it == "foo") { "invalid value $it" }
            }

            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().parse("--x=foo")
        assertTrue(called)

        called = false
        shouldThrow<MissingParameter> { C().parse("") }
    }

    @Test
    fun `option validator flag`() {
        var called = false

        class C : TestCommand() {
            val x by option().flag().validate {
                called = true
                require(it)
            }

            override fun run_() {
                x shouldBe true
            }
        }

        C().parse("--x")
        assertTrue(called)
    }


    @Test
    fun `convert catches exceptions`() {
        class C : TestCommand(called = false) {
            init {
                context { allowInterspersedArgs = false }
            }

            val x by option().convert {
                when (it) {
                    "uerr" -> fail("failed")
                    "err" -> throw NumberFormatException("failed")
                }
                it
            }
        }

        shouldThrow<BadParameterValue> { C().parse("--x=uerr") }.paramName shouldBe "--x"
        shouldThrow<BadParameterValue> { C().parse("--x=err") }.paramName shouldBe "--x"
    }

    @Test
    fun `one option with slash prefix`() = forall(
            row("", null),
            row("/xx 3", "3"),
            row("/xx=asd", "asd"),
            row("/x 4", "4"),
            row("/x /xx /xx foo", "foo"),
            row("/xfoo", "foo")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("/x", "/xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    fun `one option with java prefix`() = forall(
            row("", null),
            row("-xx 3", "3"),
            row("-xx=asd", "asd"),
            row("-x 4", "4"),
            row("-x -xx -xx foo", "foo"),
            row("-xfoo", "foo")) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "-xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    fun `two options with chmod prefixes`() = forall(
            row("", false, false),
            row("-x", false, false),
            row("-x +x", true, false),
            row("+x -x", false, false),
            row("+y", false, true),
            row("-y", false, false),
            row("-y +y", false, true),
            row("+y -y", false, false),
            row("-x -y", false, false),
            row("-x -y +xy", true, true)) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("+x").flag("-x")
            val y by option("+y").flag("-y")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    fun `normalized tokens`() = forall(
            row("", null),
            row("--XX=FOO", "FOO"),
            row("--xx=FOO", "FOO"),
            row("-XX", "X")) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().context { tokenTransformer = { it.toLowerCase() } }.parse(argv)
    }

    @Test
    fun `aliased tokens`() = forall(
            row("", null),
            row("--yy 3", "3")) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().context { tokenTransformer = { "--xx" } }.parse(argv)
    }

    @Test
    fun `deprecated warning options`() {
        class C : TestCommand() {
            val g by option()
            val f by option().flag().deprecated()
            val x by option().deprecated()
            val y by option().deprecated("warn")
            val z by option().deprecated()
            override fun run_() {
                messages shouldBe listOf(
                        "WARNING: option --f is deprecated",
                        "WARNING: option --x is deprecated",
                        "warn"
                )
            }
        }
        C().context { printExtraMessages = false }.parse("--g=0 --f --x=1 --y=2")
    }

    @Test
    fun `deprecated error option`() {
        class C : TestCommand(called = false) {
            val x by option().flag().deprecated(error = true)
            val y by option().deprecated("err", error = true)
        }
        shouldThrow<CliktError> { C().parse("--x") }
                .message shouldBe "ERROR: option --x is deprecated"

        shouldThrow<CliktError> { C().parse("--y=1") }
                .message shouldBe "err"
    }

    @Test
    fun `wrapValue options`() = forall(
            row("", null),
            row("--x=1", listOf(1))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option().int().wrapValue { listOf(it) }
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }
}
