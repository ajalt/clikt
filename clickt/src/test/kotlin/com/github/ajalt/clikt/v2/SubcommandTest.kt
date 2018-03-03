package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.Command
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.softly
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SubcommandTest {
    @Test
    fun `subcommand`() = parameterized(
            row("--xx 2 sub --xx 3 --yy 4"),
            row("--xx 2 sub --xx 3 -y 4"),
            row("--xx 2 sub -x 3 --yy 4"),
            row("--xx 2 sub -x3 --yy 4"),
            row("--xx 2 sub --xx 3 -y4"),
            row("--xx 2 sub --xx=3 --yy=4"),
            row("--xx 2 sub -x3 --yy=4"),
            row("--xx 2 sub -x 3 -y 4"),
            row("--xx 2 sub -x3 -y 4"),
            row("--xx 2 sub -x 3 -y4"),
            row("--xx 2 sub -x3 -y4"),

            row("--xx=2 sub --xx 3 --yy 4"),
            row("--xx=2 sub --xx 3 -y 4"),
            row("--xx=2 sub -x 3 --yy 4"),
            row("--xx=2 sub -x3 --yy 4"),
            row("--xx=2 sub --xx 3 -y4"),
            row("--xx=2 sub --xx=3 --yy=4"),
            row("--xx=2 sub -x3 --yy=4"),
            row("--xx=2 sub -x 3 -y 4"),
            row("--xx=2 sub -x3 -y 4"),
            row("--xx=2 sub -x 3 -y4"),
            row("--xx=2 sub -x3 -y4"),

            row("-x 2 sub --xx 3 --yy 4"),
            row("-x 2 sub --xx 3 -y 4"),
            row("-x 2 sub -x 3 --yy 4"),
            row("-x 2 sub -x3 --yy 4"),
            row("-x 2 sub --xx 3 -y4"),
            row("-x 2 sub --xx=3 --yy=4"),
            row("-x 2 sub -x3 --yy=4"),
            row("-x 2 sub -x 3 -y 4"),
            row("-x 2 sub -x3 -y 4"),
            row("-x 2 sub -x 3 -y4"),
            row("-x 2 sub -x3 -y4"),

            row("-x2 sub --xx 3 --yy 4"),
            row("-x2 sub --xx 3 -y 4"),
            row("-x2 sub -x 3 --yy 4"),
            row("-x2 sub -x3 --yy 4"),
            row("-x2 sub --xx 3 -y4"),
            row("-x2 sub --xx=3 --yy=4"),
            row("-x2 sub -x3 --yy=4"),
            row("-x2 sub -x 3 -y 4"),
            row("-x2 sub -x3 -y 4"),
            row("-x2 sub -x 3 -y4"),
            row("-x2 sub -x3 -y4")
    ) { (argv) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).called("x").isEqualTo("2")
            }
        }

        class Sub : CliktCommand(name = "sub") {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run() {
                assertThat(x).called("x").isEqualTo("3")
                assertThat(y).called("y").isEqualTo("4")
            }
        }

        C().subcommands(Sub()).parse(splitArgv(argv))
    }

    @Test
    fun `multiple subcommands`() = parameterized(
            row("-x1 sub1 2 3", true),
            row("-x1 sub2 -x2 -y3", false)
    ) { (argv, firstCalled) ->
        class C : CliktCommand() {
            var called = false
            val x by option("-x", "--xx")
            override fun run() {
                called = true
                assertThat(x).called("parent x").isEqualTo("1")
            }
        }

        class Sub1 : CliktCommand(name = "sub1") {
            var called = false
            val z by argument().paired()
            override fun run() {
                called = true
                assertThat(z).called("z").isEqualTo("2" to "3")
            }
        }

        class Sub2 : CliktCommand(name = "sub2") {
            var called = false
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run() {
                called = true
                assertThat(x).called("x").isEqualTo("2")
                assertThat(y).called("y").isEqualTo("3")
            }
        }

        val s1 = Sub1()
        val s2 = Sub2()
        val c: C = C().subcommands(s1, s2)

        c.parse(splitArgv(argv))

        assertThat(c.called).isTrue
        assertThat(s1.called).isEqualTo(firstCalled)
        assertThat(s2.called).isNotEqualTo(firstCalled)
    }

    @Test
    fun `argument before subcommand`() {
        class C : CliktCommand() {
            val x by argument().multiple()
            override fun run() {
                assertThat(x).containsExactly("123", "456")
            }
        }

        class Sub : CliktCommand(name = "sub") {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).isEqualTo("foo")
            }
        }

        C().subcommands(Sub()).parse(splitArgv("123 456 sub -xfoo"))
    }
}
