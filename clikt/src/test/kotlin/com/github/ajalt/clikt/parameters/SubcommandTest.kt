package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.pair
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class SubcommandTest {
    @Test
    fun `subcommand`() = parameterized(
            row("--xx 2 sub --xx 3 --yy 4"),
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
                x shouldBe "2"
            }
        }

        class Sub : CliktCommand(name = "sub") {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run() {
                x shouldBe "3"
                y shouldBe "4"
            }
        }

        C().subcommands(Sub()).parse(splitArgv(argv))
    }

    @Test
    fun `multiple subcommands`() = parameterized(
            row("-x1 sub1 2 3", true)
    ) { (argv, firstCalled) ->
        class C : CliktCommand() {
            var called = false
            val x by option("-x", "--xx")
            override fun run() {
                called = true
                x shouldBe "1"
            }
        }

        class Sub1 : CliktCommand(name = "sub1") {
            var called = false
            val z by argument().pair()
            override fun run() {
                called = true
                z shouldBe ("2" to "3")
            }
        }

        class Sub2 : CliktCommand(name = "sub2") {
            var called = false
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run() {
                called = true
                x shouldBe "2"
                y shouldBe "3"
            }
        }

        val s1 = Sub1()
        val s2 = Sub2()
        val c: C = C().subcommands(s1, s2)

        c.parse(splitArgv(argv))

        c.called shouldBe true
        s1.called shouldBe firstCalled
        s2.called shouldNotBe firstCalled
    }

    @Test
    fun `argument before subcommand`() {
        class C : CliktCommand() {
            val x by argument().multiple()
            override fun run() {
                x shouldBe listOf("123", "456")
            }
        }

        class Sub : CliktCommand(name = "sub") {
            val x by option("-x", "--xx")
            override fun run() {
                x shouldBe "foo"
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
                x shouldBe "--xx"
                y shouldBe "--yy"
            }
        }

        class Sub : CliktCommand(name = "sub") {
            val x by option("-x", "--xx")
            override fun run() {
                x shouldBe "foo"
            }
        }

        C().subcommands(Sub())
                .parse(splitArgv("--xx --xx -- --yy sub --xx foo"))
    }

    @Test
    fun `normalized subcommand names`() = parameterized(
            row("a b"),
            row("a b SUB -xfoo"),
            row("a b SUB -xfoo SUB2 -xfoo"),
            row("a b SUB -xfoo sub2 -xfoo")) { (argv) ->

        class C : CliktCommand(invokeWithoutSubcommand = true) {
            val x by argument().multiple()
            override fun run() {
                x shouldBe listOf("a", "b")
            }
        }

        class Sub : CliktCommand(name = "sub", invokeWithoutSubcommand = true) {
            val x by option("-x", "--xx")
            override fun run() {
                x shouldBe "foo"
            }
        }

        class Sub2 : CliktCommand(name = "sub2") {
            val x by option("-x", "--xx")
            override fun run() {
                x shouldBe "foo"
            }
        }

        C().subcommands(Sub().subcommands(Sub2()))
                .context { tokenTransformer = { it.toLowerCase() } }
                .parse(splitArgv(argv))
    }

    @Test
    fun `aliased subcommand names`() = parameterized(
            row("a b"),
            row("a 1 sub -xfoo"),
            row("a 2"),
            row("3"),
            row("a b 4 -xfoo"),
            row("a b 4 1")) { (argv) ->

        class C : CliktCommand(invokeWithoutSubcommand = true) {
            val x by argument().multiple()
            override fun run() {
                x shouldBe listOf("a", "b")
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
                x shouldBe "foo"
            }

            override fun aliases() = mapOf(
                    "1" to listOf("-xfoo")
            )
        }

        C().subcommands(Sub())
                .parse(splitArgv(argv))
    }
}
