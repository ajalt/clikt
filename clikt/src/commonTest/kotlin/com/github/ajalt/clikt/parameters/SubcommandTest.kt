package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.pair
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parsers.CommandInvocation
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import com.github.ajalt.clikt.testing.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("BooleanLiteralArgument")
class SubcommandTest {
    @Test
    fun subcommand() = forAll(
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
    fun `multiple subcommands`() = forAll(
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
    fun `normalized subcommand names`() = forAll(
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
            .context { tokenTransformer = { it.lowercase() } }
            .parse(argv)
    }

    @Test
    @JsName("aliased_subcommand_names")
    fun `aliased subcommand names`() = forAll(
        row("a b", false),
        row("a 1 sub -xfoo", true),
        row("a 2", true),
        row("3", true),
        row("a b 4 -xfoo", true),
        row("a b 4 1", true)
    ) { argv, called ->

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
            @Suppress("unused")
            val arg by argument()
        }

        val p = Parent()
        shouldThrow<UsageError> {
            p.subcommands(Child().subcommands(Grandchild()))
                .parse("child grandchild")
        }.let { p.getFormattedHelp(it) } shouldBe """
            |Usage: parent child grandchild [<options>] <arg>
            |
            |Error: missing argument <arg>
            """.trimMargin()
    }

    @Test
    @JsName("subcommandprintHelpOnEmptyArgs__true")
    fun `subcommand printHelpOnEmptyArgs = true`() {
        class C : TestCommand(called = false, printHelpOnEmptyArgs = true)
        class S : TestCommand(called = false, printHelpOnEmptyArgs = true) {
            val x by argument()
        }

        shouldThrow<PrintHelpMessage> {
            C().subcommands(S()).parse("s")
        }.error shouldBe true
        val c = C().subcommands(S().context { helpOptionNames = emptyList() })
        c.test("s").output.trim() shouldBe "Usage: c s <x>"
    }

    @Test
    fun noSuchSubcommand() = forAll(
        row("qux", "no such subcommand qux"),
        row("fo", "no such subcommand fo. Did you mean foo?"),
        row("fop", "no such subcommand fop. Did you mean foo?"),
        row("bart", "no such subcommand bart. Did you mean bar?"),
        row("ba", "no such subcommand ba. (Possible subcommands: bar, baz)")
    ) { argv, message ->
        shouldThrow<NoSuchSubcommand> {
            TestCommand()
                .subcommands(
                    TestCommand(name = "foo"),
                    TestCommand(name = "bar"),
                    TestCommand(name = "baz")
                )
                .parse(argv)
        }.formattedMessage shouldBe message
    }


    @Test
    @JsName("subcommand_cycle")
    fun `subcommand cycle`() {
        val root = TestCommand(called = false)
        val a = TestCommand(called = false, name = "a")
        val b = TestCommand(called = false)

        shouldThrow<IllegalStateException> {
            root.subcommands(a.subcommands(b.subcommands(a))).parse("a b a")
        }.formattedMessage shouldBe "Command a already registered"
    }

    @Test
    fun allowMultipleSubcommands() = forAll(
        row("foo a", 1, 0, "a", null, null),
        row("foo a foo b", 2, 0, "b", null, null),
        row("bar a", 0, 1, null, null, "a"),
        row("bar a bar b", 0, 2, null, null, "b"),
        row("bar --opt=o a", 0, 1, null, "o", "a"),
        row("foo a bar --opt=o b foo c bar d", 2, 2, "c", null, "d"),
        row("foo a bar b foo c bar --opt=o d", 2, 2, "c", "o", "d")
    ) { argv, fc, bc, fa, bo, ba ->
        val foo = MultiSubFoo(count = fc)
        val bar = MultiSubBar(count = bc)
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(
            foo,
            bar,
            TestCommand(called = false)
        )
        c.parse(argv)
        if (fc > 0) foo.arg shouldBe fa
        if (bc > 0) {
            bar.opt shouldBe bo
            bar.arg shouldBe ba
        }
    }

    @Test
    @JsName("multiple_subcommands_with_nesting")
    fun `multiple subcommands with nesting`() {
        val foo = MultiSubFoo(count = 2)
        val bar = MultiSubBar(count = 2)
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(foo.subcommands(bar))
        c.parse("foo f1 bar --opt=1 b1 foo f2 bar b2")
        foo.arg shouldBe "f2"
        bar.opt shouldBe null
        bar.arg shouldBe "b2"

        foo.invokedSubcommands shouldBe listOf(bar, bar)
        bar.invokedSubcommands shouldBe listOf(null, null)
    }

    @Test
    @JsName("multiple_subcommands_nesting_the_same_name")
    fun `multiple subcommands nesting the same name`() {
        val bar1 = MultiSubBar(count = 2)
        val bar2 = MultiSubBar(count = 2)
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(bar1.subcommands(bar2))
        val argv = "bar a11 bar a12 bar a12 bar --opt=o a22"
        c.parse(argv)
        bar1.arg shouldBe "a12"
        bar2.opt shouldBe "o"
        bar2.arg shouldBe "a22"

        c.invokedSubcommands shouldBe listOf(bar1, bar1)
        bar1.invokedSubcommands shouldBe listOf(bar2, bar2)
        bar2.invokedSubcommands shouldBe listOf(null, null)

        val result = CommandLineParser.parse(c, CommandLineParser.tokenize(argv))
        val invocation = result.invocation
        invocation.command shouldBe c
        invocation subcommandsShouldBe listOf(bar1, bar1)
        invocation.subcommandInvocations[0] subcommandsShouldBe listOf(bar2)
        invocation.subcommandInvocations[1] subcommandsShouldBe listOf(bar2)
    }

    private infix fun CommandInvocation<*>.subcommandsShouldBe(
        expected: List<BaseCliktCommand<*>>,
    ) {
        withClue("${command.commandName} subcommands") {
            subcommandInvocations.map { it.command.commandName }
                .shouldBe(expected.map { it.commandName })
        }
    }


    @Test
    @JsName("multiple_subcommands_with_varargs")
    fun `multiple subcommands with varargs`() = forAll(
        row("foo f1 baz", 1, 1, "f1", emptyList()),
        row("foo f1 foo f2 baz", 2, 1, "f2", emptyList()),
        row("baz b1 foo f1", 1, 1, "f1", listOf("b1")),
        row("baz foo f1 baz foo f2", 2, 2, "f2", listOf()),
        row("foo f1 baz b1 b2 foo f2", 2, 1, "f2", listOf("b1", "b2"))
    ) { argv, fc, bc, fa, ba ->
        class Baz : TestCommand(name = "baz", count = bc) {
            val arg by argument().multiple()
        }

        val foo = MultiSubFoo(count = fc)
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
    @JsName("multiple_subcommands_root_option")
    fun `multiple subcommands with root option`() {
        class C : TestCommand(count = 1, allowMultipleSubcommands = true) {
            val x by option()

            override fun run_() {
                x shouldBe "xx"
            }
        }

        val foo = MultiSubFoo(1)
        val bar = MultiSubBar(1)
        val c = C().subcommands(foo, bar)
        c.parse("--x=xx foo 1 bar 2")
        c.x shouldBe "xx"
        foo.invokedSubcommands shouldBe listOf(null)
        bar.invokedSubcommands shouldBe listOf(null)
        c.invokedSubcommands shouldBe listOf(foo)

        val c2 = C().subcommands(MultiSubFoo(1), MultiSubBar(1))
        shouldThrow<NoSuchArgument> {
            c2.parse("--x=xx foo 1 z bar 2")
        }.formattedMessage shouldBe "got unexpected extra argument (z)"
    }

    @Test
    @JsName("multiple_subcommands_required_option")
    fun `multiple subcommands with required option`() {
        class C : TestCommand(count = 1, allowMultipleSubcommands = true) {
            val x by option().required()

            override fun run_() {
                x shouldBe "xx"
            }
        }

        C().subcommands(MultiSubFoo(1), MultiSubBar(1)).parse("--x=xx foo 1 bar 2")
    }

    @Test
    @JsName("multiple_subcommands_with_excess_arguments")
    fun `multiple subcommands with excess arguments`() {
        val sub = TestCommand(name = "sub", called = true)
        val c = TestCommand(allowMultipleSubcommands = true).subcommands(sub)
        c.parse("sub foo")
        shouldThrow<NoSuchArgument> {
            c.parse("sub foo")
        }.formattedMessage shouldBe "got unexpected extra argument (foo)"
    }

    @Test
    @JsName("accessing_options_of_uninvoked_subcommand")
    fun `accessing options of uninvoked subcommand`() {
        class Sub(name: String, called: Boolean) : TestCommand(called, name = name) {
            val x by option().default("def")
        }

        shouldThrow<IllegalStateException> { Sub("sub", false).x }

        val sub1 = Sub("sub1", true)
        val sub2 = Sub("sub2", false)
        TestCommand().subcommands(sub1, sub2).parse("sub1")
        sub1.x shouldBe "def"
        shouldThrow<IllegalStateException> { sub2.x }
    }
}

private class MultiSubFoo(count: Int) : TestCommand(name = "foo", count = count) {
    val arg by argument()
}

private class MultiSubBar(count: Int) : TestCommand(name = "bar", count = count) {
    val opt by option()
    val arg by argument()
}
