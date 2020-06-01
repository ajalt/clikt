package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.forall
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.tables.row
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused")
class CliktCommandTest {
    @Test
    @Suppress("ClassName")
    @JsName("inferring_command_name")
    fun `inferring command name`() {
        class ListAllValuesCommand: TestCommand()
        class LGTMMeansLookingGoodToMe: TestCommand()
        class `nothing-to-change`: TestCommand()
        ListAllValuesCommand().commandName shouldBe "list-all-values"
        LGTMMeansLookingGoodToMe().commandName shouldBe "lgtmmeans-looking-good-to-me"
        `nothing-to-change`().commandName shouldBe "nothing-to-change"
    }

    @Test
    @JsName("invokeWithoutSubcommand_false")
    fun `invokeWithoutSubcommand=false`() {
        shouldThrow<PrintHelpMessage> {
            TestCommand(called = false).subcommands(TestCommand(called = false)).parse("")
        }.error shouldBe true

        val child = TestCommand(called = true, name = "foo")
        TestCommand(called = true).subcommands(child).apply {
            parse("foo")
            currentContext.invokedSubcommand shouldBe child
            child.currentContext.invokedSubcommand shouldBe null
        }
    }

    @Test
    @JsName("invokeWithoutSubcommand_true")
    fun `invokeWithoutSubcommand=true`() {
        TestCommand(called = true, invokeWithoutSubcommand = true).subcommands(TestCommand(called = false)).apply {
            parse("")
            currentContext.invokedSubcommand shouldBe null
        }

        val child = TestCommand(called = true, name = "foo")
        TestCommand(called = true, invokeWithoutSubcommand = true).subcommands(child).apply {
            parse("foo")
            currentContext.invokedSubcommand shouldBe child
            child.currentContext.invokedSubcommand shouldBe null
        }
    }


    @Test
    @JsName("printHelpOnEmptyArgs__true")
    fun `printHelpOnEmptyArgs = true`() {
        class C : TestCommand(called = false, printHelpOnEmptyArgs = true)
        shouldThrow<PrintHelpMessage> {
            C().parse("")
        }.error shouldBe true
    }

    @Test
    @JsName("shortHelp_extraction")
    fun `shortHelp extraction`() = forall(
            row("", ""),
            row("foo bar", "foo bar"),
            row("\n  \tfoo bar", "foo bar"),
            row("```\n  foo bar", "foo bar"),
            row("```\n  foo bar", "foo bar"),
            row("```foo\nbar", "foo")
    ) { help, expected ->
        class C : NoOpCliktCommand(help = help) {
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
    @JsName("command_usage")
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
    @JsName("command_toString")
    fun `command toString`() {
        class Cmd : TestCommand() {
            val opt by option("-o", "--option")
            val int by option().int()
            val arg by argument()
        }

        class Sub : TestCommand() {
            val foo by option()
        }

        Cmd().toString() shouldBe "<Cmd name=cmd options=[--option --int] arguments=[ARG]>"
        Cmd().apply { parse("--int=123 bar") }.toString() shouldBe "<Cmd name=cmd options=[--option=null --int=123 --help] arguments=[ARG=bar]>"
        Cmd().apply { parse("foo") }.toString() shouldBe "<Cmd name=cmd options=[--option=null --int=null --help] arguments=[ARG=foo]>"

        Cmd().subcommands(Sub()).apply { parse("-ooo bar sub --foo=baz") }.toString().shouldBe(
                "<Cmd name=cmd options=[--option=oo --int=null --help] arguments=[ARG=bar] " +
                        "subcommands=[<Sub name=sub options=[--foo=baz --help]>]>"
        )
    }

    @Test
    @JsName("command_with_groups_toString")
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

        Cmd().toString() shouldBe "<Cmd name=cmd options=[--option --foo --bar --e1 --e2]>"
        Cmd().apply { parse("-oo --foo=f --e1=1") }.toString() shouldBe "<Cmd name=cmd options=[--option=o --foo=f --bar=null --e1=1 --e2=null --help]>"
    }

    // https://github.com/ajalt/clikt/issues/64
    @Test
    @JsName("context_is_initialized_when_helpOptionNames_is_null")
    fun `context is initialized when helpOptionNames is null`() {
        class D : TestCommand() {
            override fun run_() {
                currentContext shouldNotBe null
            }
        }

        TestCommand().context { helpOptionNames = emptySet() }.subcommands(D()).parse("d")
    }

    @Test
    @JsName("command_registered_functions")
    fun `command registered functions`() {
        val child1 = TestCommand(name = "foo", called = false)
        val child2 = TestCommand(name = "bar", called = false)

        class G : OptionGroup() {
            val og by option()
        }

        val g = G()

        class C : TestCommand(called = false) {
            val o1 by option()
            val o2 by option().flag()
            val a by argument()
            val g by g
        }

        val c = C()
        c.registeredSubcommands() should beEmpty()
        c.subcommands(child1, child2)

        c.registeredSubcommands().shouldContainExactlyInAnyOrder(child1, child2)
        c.registeredOptions().map { it.names.single() }.shouldContainExactlyInAnyOrder(
                "--o1", "--o2", "--og"
        )
        c.registeredArguments().map { it.name } shouldBe listOf("A")
        c.registeredParameterGroups() shouldBe listOf(g)
    }

    @Test
    @JsName("subcommand_cycle")
    fun `subcommand cycle`() {
        val root = TestCommand(called = false)
        val a = TestCommand(called = false, name = "a")
        val b = TestCommand(called = false)

        shouldThrow<IllegalStateException> {
            root.subcommands(a.subcommands(b.subcommands(a))).parse("a b a")
        }.message shouldBe "Command a already registered"
    }

    @Test
    @JsName("multiple_subcommands")
    fun `multiple subcommands`() = forall(
            row("foo a", 1, 0, "a", null, null),
            row("foo a foo b", 2, 0, "b", null, null),
            row("bar a", 0, 1, null, null, "a"),
            row("bar a bar b", 0, 2, null, null, "b"),
            row("bar --opt=o a", 0, 1, null, "o", "a"),
            row("foo a bar --opt=o b foo c bar d", 2, 2, "c", null, "d"),
            row("foo a bar b foo c bar --opt=o d", 2, 2, "c", "o", "d")
    ) { argv, fc, bc, fa, bo, ba ->
        val foo = MultiSub1(count = fc)
        val bar = MultiSub2(count = bc)
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(foo, bar, TestCommand(called = false))
        c.parse(argv)
        if (fc > 0) foo.arg shouldBe fa
        bar.opt shouldBe bo
        if (bc > 0) bar.arg shouldBe ba
    }

    @Test
    @JsName("multiple_subcommands_with_nesting")
    fun `multiple subcommands with nesting`() {
        val foo = MultiSub1(count = 2)
        val bar = MultiSub2(count = 2)
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(foo.subcommands(bar))
        c.parse("foo f1 bar --opt=1 b1 foo f2 bar b2")
        foo.arg shouldBe "f2"
        bar.opt shouldBe null
        bar.arg shouldBe "b2"
    }

    @Test
    @JsName("multiple_subcommands_nesting_the_same_name")
    fun `multiple subcommands nesting the same name`() {
        val bar1 = MultiSub2(count = 2)
        val bar2 = MultiSub2(count = 2)
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(bar1.subcommands(bar2))
        c.parse("bar a11 bar a12 bar a12 bar --opt=o a22")
        bar1.arg shouldBe "a12"
        bar2.opt shouldBe "o"
        bar2.arg shouldBe "a22"
    }

    @Test
    @JsName("multiple_subcommands_with_varargs")
    fun `multiple subcommands with varargs`() = forall(
            row("foo f1 baz", 1, 1, "f1", emptyList()),
            row("foo f1 foo f2 baz", 2, 1, "f2", emptyList()),
            row("baz foo", 0, 1, "", listOf("foo")),
            row("baz foo baz foo", 0, 1, "", listOf("foo", "baz", "foo")),
            row("foo f1 baz foo f2", 1, 1, "f1", listOf("foo", "f2"))
    ) { argv, fc, bc, fa, ba ->
        class Baz : TestCommand(name = "baz", count = bc) {
            val arg by argument().multiple()
        }

        val foo = MultiSub1(count = fc)
        val baz = Baz()
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(foo, baz)
        c.parse(argv)

        if (fc > 0) foo.arg shouldBe fa
        baz.arg shouldBe ba
    }

    @Test
    @JsName("multiple_subcommands_nesting_multiple_subcommands")
    fun `multiple subcommands nesting multiple subcommands`() {
        val c = TestCommand(allowMultipleSubcommands = true)
                .subcommands(TestCommand(allowMultipleSubcommands = true))
        shouldThrow<IllegalArgumentException> {
            c.parse("")
        }.message shouldContain "allowMultipleSubcommands"
    }

    @Test
    @JsName("treat_unknown_options_as_arguments")
    fun `treat unknown options as arguments`() {
        class C : TestCommand(treatUnknownOptionsAsArgs = true) {
            val foo by option().flag()
            val args by argument().multiple()

            override fun run_() {
                foo shouldBe true
                args shouldBe listOf("--bar", "baz", "--qux=qoz")
            }
        }

        C().parse("--bar --foo baz --qux=qoz")
    }

    @Test
    @JsName("treat_unknown_options_as_arguments_with_grouped_flag")
    fun `treat unknown options as arguments with grouped flag`() {
        class C(called:Boolean) : TestCommand(called=called, treatUnknownOptionsAsArgs = true) {
            val foo by option("-f").flag()
            val args by argument().multiple()
        }

        val c = C(true)
        c.parse("-f -g -i")
        c.foo shouldBe true
        c.args shouldBe listOf("-g", "-i")
        shouldThrow<NoSuchOption> {
            C(false).parse("-fgi")
        }.message shouldBe "no such option: \"-g\"."
    }
}

private class MultiSub1(count: Int) : TestCommand(name = "foo", count = count) {
    val arg by argument()
}

private class MultiSub2(count: Int) : TestCommand(name = "bar", count = count) {
    val opt by option()
    val arg by argument()
}
