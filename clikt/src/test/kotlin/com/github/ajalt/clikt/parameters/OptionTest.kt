package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.*
import org.junit.Test

class OptionTest {
    @Test
    fun `inferEnvvar`() = parameterized(
            row(setOf("--foo"), null, null, null),
            row(setOf(""), "foo", null, "foo"),
            row(setOf("--bar"), null, "FOO", "FOO_BAR"),
            row(setOf("/bar"), null, "FOO", "FOO_BAR"),
            row(setOf("-b"), null, "FOO", "FOO_B"),
            row(setOf("-b", "--bar"), null, "FOO", "FOO_BAR")
    ) { (names, envvar, prefix, expected) ->
        assertThat(inferEnvvar(names, envvar, prefix)).isEqualTo(expected)
    }

    @Test
    fun `zero options`() {
        class C : CliktCommand() {
            var called = false
            override fun run() {
                called = true
            }
        }

        C().apply {
            assertFalse(called)
            parse(arrayOf())
            assertTrue(called)
        }
    }

    @Test
    fun `no such option`() {
        class C : NeverCalledCliktCommand() {
            val foo by option()
            val bar by option()
            val baz by option()
        }

        assertThrows<NoSuchOption> {
            C().parse(splitArgv("--qux"))
        }.hasMessage("no such option: \"--qux\".")

        assertThrows<NoSuchOption> {
            C().parse(splitArgv("--fo"))
        }.hasMessage("no such option: \"--fo\". Did you mean \"--foo\"?")

        assertThrows<NoSuchOption> {
            C().parse(splitArgv("--ba"))
        }.hasMessage("no such option: \"--ba\". (Possible options: --bar, --baz)")
    }

    @Test
    fun `one option`() = parameterized(
            row("", null),
            row("--xx=", ""),
            row("--xx 3", "3"),
            row("--xx --xx", "--xx"),
            row("--xx=asd", "asd"),
            row("-x 4", "4"),
            row("-x -x", "-x"),
            row("-xfoo", "foo")) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            var called = false
            override fun run() {
                called = true
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().apply {
            assertFalse(called)
            parse(splitArgv(argv))
            assertTrue(called)
        }
    }

    @Test
    fun `two options, one name each`() {
        class C : CliktCommand() {
            val x by option("-x")
            val y by option("--yy")
            override fun run() {
                assertThat(x).isEqualTo("3")
                assertThat(y).isEqualTo("4")
            }
        }
        C().parse(splitArgv("-x 3 --yy 4"))
    }

    @Test
    fun `two options`() = parameterized(
            row("--xx 3 --yy 4", "3", "4"),
            row("--xx 3 -y 4", "3", "4"),
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
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }


    @Test
    fun `two options nargs=2`() = parameterized(
            row("", null, null),
            row("--xx 1 3", "1" to "3", null),
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
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").paired()
            val y by option("-y", "--yy").paired()
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two options nargs=3`() {
        val xvalue = Triple("1", "2", "3")
        val yvalue = Triple("5", "6", "7")
        parameterized(
                row("", null, null),
                row("--xx 1 2 3", xvalue, null),
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
        ) { (argv, ex, ey) ->
            class C : CliktCommand() {
                val x by option("-x", "--xx").triple()
                val y by option("-y", "--yy").triple()
                override fun run() {
                    assertThat(x).called("x").isEqualTo(ex)
                    assertThat(y).called("y").isEqualTo(ey)
                }
            }

            C().parse(splitArgv(argv))
        }
    }

    @Test
    fun `two options nargs=2 usage errors`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx").paired()
            val y by option("-y", "--yy").paired()
            override fun run() {
                fail("should not be called $x, $y")
            }
        }
        assertThrows<IncorrectOptionNargs> { C().parse(splitArgv("-x")) }
                .hasMessage("-x option requires 2 arguments")
        assertThrows<UsageError> { C().parse(splitArgv("--yy foo bar baz")) }
                .hasMessage("Got unexpected extra argument (baz)")
    }

    @Test
    fun `flag options`() = parameterized(
            row("", false, false, null),
            row("-x", true, false, null),
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
    ) { (argv, ex, ey, ez) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").flag("-X", "--no-xx")
            val y by option("-y", "--yy").flag()
            val z by option("-z", "--zz")
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
                assertThat(z).called("z").isEqualTo(ez)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `switch options`() = parameterized(
            row("", null, -1),
            row("-x -y", 1, 3),
            row("--xx -yy", 2, 4)) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by option().switch("-x" to 1, "--xx" to 2)
            val y by option().switch("-y" to 3, "-yy" to 4).default(-1)
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `counted options`() = parameterized(
            row("", 0, false, null),
            row("-x", 1, false, null),
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
    ) { (argv, ex, ey, ez) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").counted()
            val y by option("-y", "--yy").flag()
            val z by option("-z", "--zz")
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
                assertThat(z).called("z").isEqualTo(ez)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `default option`() = parameterized(
            row("", "def"),
            row("--xx 3", "3"),
            row("-x4", "4")) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").default("def")
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `required option`() {
        class C : CliktCommand() {
            val x by option().required()
            override fun run() {
                assertThat(x).isEqualTo("foo")
            }
        }

        C().parse(splitArgv("--x=foo"))

        assertThrows<MissingParameter> { C().parse(splitArgv("")) }
                .hasMessage("Missing option \"--x\".")
    }

    @Test
    fun `option metavars`() {
        class C : CliktCommand() {
            val x by option()
            val y by option(metavar = "FOO").default("")
            val z by option(metavar = "FOO").convert("BAR") { it }
            val w by option().convert("BAR") { it }
            val u by option().flag()
            override fun run() {
                assertThat(options).allMatch {
                    it is EagerOption || // skip help option
                            "--x" in it.names && it.metavar == "TEXT" ||
                            "--y" in it.names && it.metavar == "FOO" ||
                            "--z" in it.names && it.metavar == "FOO" ||
                            "--w" in it.names && it.metavar == "BAR" ||
                            "--u" in it.names && it.metavar == null
                }
            }
        }

        C().parse(splitArgv(""))
    }

    @Test
    fun `option validators`() {
        var calledX = false
        var calledY = false

        class C : CliktCommand() {
            val x by option().validate {
                calledX = true
                require(it == "foo") { "invalid value $it" }
            }
            val y by option().flag().validate {
                calledY = true
                require(it)
            }

            override fun run() {
                assertThat(x).isEqualTo("foo")
                assertThat(y).isTrue()
            }
        }

        C().parse(splitArgv("--x foo --y"))
        assertTrue(calledX)
        assertTrue(calledY)
    }


    @Test
    fun `convert catches exceptions`() {
        class C : NeverCalledCliktCommand() {
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

        assertThrows<BadParameterValue> { C().parse(splitArgv("--x=uerr")) }
                .matches { it is BadParameterValue && it.paramName == "--x" }
        assertThrows<BadParameterValue> { C().parse(splitArgv("--x=err")) }
                .matches { it is BadParameterValue && it.paramName == "--x" }
    }

    @Test
    fun `one option with slash prefix`() = parameterized(
            row("", null),
            row("/xx=", ""),
            row("/xx 3", "3"),
            row("/xx=asd", "asd"),
            row("/x 4", "4"),
            row("/x /xx /xx foo", "foo"),
            row("/xfoo", "foo")) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("/x", "/xx")
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one option with java prefix`() = parameterized(
            row("", null),
            row("-xx=", ""),
            row("-xx 3", "3"),
            row("-xx=asd", "asd"),
            row("-x 4", "4"),
            row("-x -xx -xx foo", "foo"),
            row("-xfoo", "foo")) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "-xx")
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two options with chmod prefixes`() = parameterized(
            row("", false, false),
            row("+x", true, false),
            row("-x", false, false),
            row("-x +x", true, false),
            row("+x -x", false, false),
            row("+y", false, true),
            row("-y", false, false),
            row("-y +y", false, true),
            row("+y -y", false, false),
            row("-x -y", false, false),
            row("-x -y +xy", true, true)) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by option("+x").flag("-x")
            val y by option("+y").flag("-y")
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `normalized tokens`() = parameterized(
            row("", null),
            row("--XX 3", "3"),
            row("--xx 3", "3"),
            row("-Xx", "x")) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().context { tokenTransformer = { it.toLowerCase() } }.parse(splitArgv(argv))
    }

    @Test
    fun `aliased tokens`() = parameterized(
            row("", null),
            row("--xx 3", "3"),
            row("--yy 3", "3")) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().context { tokenTransformer = { "--xx" } }.parse(splitArgv(argv))
    }
}
