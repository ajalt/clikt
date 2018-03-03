package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.testing.Row4
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OptionTest {
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
    fun `one option`() = parameterized(
            row("", null),
            row("--xx 3", "3"),
            row("--xx=asd", "asd"),
            row("-x 4", "4"),
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
    fun `flag options`() = parameterized(
            row("", false, false, null),
            row("-x", true, false, null),
            row("--xx", true, false, null),
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
            row("-xzfoo", true, false, "foo")
    ) { (argv, ex, ey, ez) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").flag()
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
}
