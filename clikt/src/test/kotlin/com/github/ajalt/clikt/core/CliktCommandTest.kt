package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Test

class CliktCommandTest {
    @Test
    fun `invokeWithoutSubcommand=false`() {
        shouldThrow<PrintHelpMessage> {
            TestCommand(called = false).subcommands(TestCommand(called = false)).parse("")
        }

        val child = TestCommand(called = true, name = "foo")
        TestCommand(called = true).subcommands(child).apply {
            parse("foo")
            context.invokedSubcommand shouldBe child
            child.context.invokedSubcommand shouldBe null
        }
    }

    @Test
    fun `invokeWithoutSubcommand=true`() {
        TestCommand(called = true, invokeWithoutSubcommand = true).subcommands(TestCommand(called = false)).apply {
            parse("")
            context.invokedSubcommand shouldBe null
        }

        val child = TestCommand(called = true, name = "foo")
        TestCommand(called = true, invokeWithoutSubcommand = true).subcommands(child).apply {
            parse("foo")
            context.invokedSubcommand shouldBe child
            child.context.invokedSubcommand shouldBe null
        }
    }


    @Test
    fun `printHelpOnEmptyArgs = true`() {
        class C : TestCommand(called = false, printHelpOnEmptyArgs = true)
        shouldThrow<PrintHelpMessage> { C().parse("") }
    }

    @Test
    fun `shortHelp extraction`() = forall(
            row("", ""),
            row("foo bar", "foo bar"),
            row("\n  \tfoo bar", "foo bar"),
            row("```\n  foo bar", "foo bar"),
            row("```\n  foo bar", "foo bar"),
            row("```foo\nbar", "foo")
    ) { help, expected ->
        class C : NoRunCliktCommand(help = help) {
            val sh = shortHelp()
        }
        C().sh shouldBe expected
    }

    @Test
    fun aliases() = forall(
            row("-xx", "x", emptyList()),
            row("a", "a", listOf("b")),
            row("a", "a", listOf("b")),
            row("b", null, listOf("-xa")),
            row("recurse", null, listOf("recurse")),
            row("recurse2", "foo", listOf("recurse", "recurse2"))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            val y by argument().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }

            override fun aliases() = mapOf(
                    "y" to listOf("-x"),
                    "a" to listOf("-xa", "b"),
                    "b" to listOf("--", "-xa"),
                    "recurse" to listOf("recurse"),
                    "recurse2" to listOf("recurse", "--xx=foo", "recurse2")
            )
        }

        C().parse(argv)
    }

    @Test
    fun `command usage`() {
        class Parent : TestCommand(called = false) {
            val arg by argument()
        }

        shouldThrow<UsageError> {
            Parent().parse("")
        }.helpMessage() shouldBe """
            |Usage: parent [OPTIONS] ARG
            |
            |Error: Missing argument "ARG".
            """.trimMargin()
    }

    @Test
    fun `command toString`() {
        class Cmd : TestCommand() {
            val opt by option("-o", "--option")
            val int by option().int()
            val arg by argument()
        }

        class Sub : TestCommand() {
            val foo by option()
        }

        Cmd().toString() shouldBe "<CliktCommand name=cmd options=[--option --int] arguments=[ARG]>"
        Cmd().apply { parse("--int=123 bar") }.toString() shouldBe "<CliktCommand name=cmd options=[--option=null --int=123 --help] arguments=[ARG=bar]>"
        Cmd().apply { parse("foo") }.toString() shouldBe "<CliktCommand name=cmd options=[--option=null --int=null --help] arguments=[ARG=foo]>"

        Cmd().subcommands(Sub()).apply { parse("-ooo bar sub --foo=baz") }.toString() shouldBe "<CliktCommand name=cmd options=[--option=oo --int=null --help] arguments=[ARG=bar] subcommands=[<CliktCommand name=sub options=[--foo=baz --help]>]>"
    }

    @Test
    fun `command with groups toString`() {
        class G : OptionGroup() {
            val opt by option("-o", "--option")
        }

        class G2 : OptionGroup() {
            val foo by option().required()
            val bar by option()
        }

        class Cmd : TestCommand() {
            val g by G()
            val g2 by G2().cooccurring()
            val ge by mutuallyExclusiveOptions(
                    option("--e1"),
                    option("--e2")
            )
        }

        Cmd().toString() shouldBe "<CliktCommand name=cmd options=[--option --foo --bar --e1 --e2]>"
        Cmd().apply { parse("-oo --foo=f --e1=1") }.toString() shouldBe "<CliktCommand name=cmd options=[--option=o --foo=f --bar=null --e1=1 --e2=null --help]>"
    }

    // https://github.com/ajalt/clikt/issues/64
    @Test
    fun `context is initialized when helpOptionNames is null`() {
        class D : TestCommand() {
            override fun run_() {
                context shouldNotBe null
            }
        }

        TestCommand().context { helpOptionNames = emptySet() }.subcommands(D()).parse("d")
    }
}
