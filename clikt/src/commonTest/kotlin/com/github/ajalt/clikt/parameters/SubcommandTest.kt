package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.pair
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import io.kotest.data.forall
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.tables.row
import kotlin.js.JsName
import kotlin.test.Test

class SubcommandTest {
    @Test
    fun subcommand() = forall(
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
    ) { argv ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe "2"
            }
        }

        class Sub : TestCommand(name = "sub") {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run_() {
                x shouldBe "3"
                y shouldBe "4"
            }
        }

        C().subcommands(Sub()).parse(argv)
    }

    @Test
    @JsName("multiple_subcommands")
    fun `multiple subcommands`() = forall(
            row("-x1 sub1 2 3", true),
            row("-x1 sub2 -x2 -y3", false)
    ) { argv, sub1Called ->
        class C : TestCommand(called = true) {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe "1"
            }
        }

        class Sub1 : TestCommand(called = sub1Called, name = "sub1") {
            val z by argument().pair()
            override fun run_() {
                z shouldBe ("2" to "3")
            }
        }

        class Sub2 : TestCommand(called = !sub1Called, name = "sub2") {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run_() {
                x shouldBe "2"
                y shouldBe "3"
            }
        }

        val s1 = Sub1()
        val s2 = Sub2()
        val c: C = C().subcommands(s1, s2)

        c.parse(argv)
    }

    @Test
    @JsName("argument_before_subcommand")
    fun `argument before subcommand`() {
        class C : TestCommand() {
            val x by argument().multiple()
            override fun run_() {
                x shouldBe listOf("123", "456")
            }
        }

        class Sub : TestCommand(name = "sub") {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().subcommands(Sub()).parse("123 456 sub -xfoo")
    }

    @Test
    @JsName("value_minus_minus_before_subcommand")
    fun `value -- before subcommand`() {
        class C : TestCommand() {
            val x by option("-x", "--xx")
            val y by argument()
            override fun run_() {
                x shouldBe "--xx"
                y shouldBe "--yy"
            }
        }

        class Sub : TestCommand(name = "sub") {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().subcommands(Sub())
                .parse("--xx --xx -- --yy sub --xx foo")
    }

    @Test
    @JsName("normalized_subcommand_names")
    fun `normalized subcommand names`() = forall(
            row("a b", false, false),
            row("a b SUB -xfoo", true, false),
            row("a b SUB -xfoo SUB2 -xfoo", true, true),
            row("a b SUB -xfoo sub2 -xfoo", true, true)
    ) { argv, call1, call2 ->

        class C : TestCommand(invokeWithoutSubcommand = true) {
            val x by argument().multiple()
            override fun run_() {
                x shouldBe listOf("a", "b")
            }
        }

        class Sub : TestCommand(called = call1, name = "sub", invokeWithoutSubcommand = true) {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe "foo"
            }
        }

        class Sub2 : TestCommand(called = call2, name = "sub2") {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().subcommands(Sub().subcommands(Sub2()))
                .context { tokenTransformer = { it.toLowerCase() } }
                .parse(argv)
    }

    @Test
    @JsName("aliased_subcommand_names")
    fun `aliased subcommand names`() = forall(
            row("a b", false),
            row("a 1 sub -xfoo", true),
            row("a 2", true),
            row("3", true),
            row("a b 4 -xfoo", true),
            row("a b 4 1", true)) { argv, called ->

        class C : TestCommand(invokeWithoutSubcommand = true) {
            val x by argument().multiple()
            override fun run_() {
                x shouldBe listOf("a", "b")
            }

            override fun aliases() = mapOf(
                    "1" to "b".split(" "),
                    "2" to "b sub -xfoo".split(" "),
                    "3" to "a b sub -xfoo".split(" "),
                    "4" to "sub".split(" ")
            )
        }

        class Sub : TestCommand(called = called, name = "sub") {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe "foo"
            }

            override fun aliases() = mapOf(
                    "1" to listOf("-xfoo")
            )
        }

        C().subcommands(Sub()).parse(argv)
    }

    @Test
    @JsName("subcommand_usage")
    fun `subcommand usage`() {
        class Parent : TestCommand()
        class Child : TestCommand()
        class Grandchild : TestCommand(called = false) {
            val arg by argument()
        }

        shouldThrow<UsageError> {
            Parent().subcommands(Child().subcommands(Grandchild()))
                    .parse("child grandchild")
        }.helpMessage() shouldBe """
            |Usage: parent child grandchild [OPTIONS] ARG
            |
            |Error: Missing argument "ARG".
            """.trimMargin()
    }
}
