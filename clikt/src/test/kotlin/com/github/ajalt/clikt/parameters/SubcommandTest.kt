package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.paired
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
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

    @Test
    fun `value -- before subcommand`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            val y by argument()
            override fun run() {
                assertThat(x).isEqualTo("--xx")
                assertThat(y).isEqualTo("--yy")
            }
        }

        class Sub : CliktCommand(name = "sub") {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).isEqualTo("foo")
            }
        }

        C().subcommands(Sub())
                .parse(splitArgv("--xx --xx -- --yy sub --xx foo"))
    }

    @Test
    fun `normalized subcommand names`() = parameterized(
            row("a b"),
            row("a b sub -xfoo"),
            row("a b SUB -xfoo"),
            row("a b SUB -xfoo SUB2 -xfoo"),
            row("a b SUB -xfoo sub2 -xfoo")) { (argv) ->

        class C : CliktCommand(invokeWithoutSubcommand = true) {
            val x by argument().multiple()
            override fun run() {
                assertThat(x).isEqualTo(listOf("a", "b"))
            }
        }

        class Sub : CliktCommand(name = "sub", invokeWithoutSubcommand = true) {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).isEqualTo("foo")
            }
        }

        class Sub2 : CliktCommand(name = "sub2") {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).isEqualTo("foo")
            }
        }

        C().subcommands(Sub().subcommands(Sub2()))
                .context { tokenTransformer = { it.toLowerCase() } }
                .parse(splitArgv(argv))
    }

    @Test
    fun `aliased subcommand names`() = parameterized(
            row("a b"),
            row("a b sub -xfoo"),
            row("a 1 sub -xfoo"),
            row("a 2"),
            row("3"),
            row("a b 4 -xfoo"),
            row("a b 4 1")) { (argv) ->

        class C : CliktCommand(invokeWithoutSubcommand = true) {
            val x by argument().multiple()
            override fun run() {
                assertThat(x).isEqualTo(listOf("a", "b"))
            }

            override fun aliases() = mapOf(
                    "1" to "b".split(" "),
                    "2" to "b sub -xfoo".split(" "),
                    "3" to "a b sub -xfoo".split(" "),
                    "4" to "sub".split(" ")
            )
        }

        class Sub : CliktCommand(name = "sub") {
            val x by option("-x", "--xx")
            override fun run() {
                assertThat(x).isEqualTo("foo")
            }

            override fun aliases() = mapOf(
                    "1" to listOf("-xfoo")
            )
        }

        C().subcommands(Sub())
                .parse(splitArgv(argv))
    }
}
