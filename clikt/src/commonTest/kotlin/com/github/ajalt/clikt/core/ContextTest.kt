package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.js.JsName
import kotlin.test.Test

class ContextTest {
    class Foo

    @Test
    @JsName("find_functions_single_context")
    fun `find functions single context`() {
        class C : TestCommand() {
            val o1 by findObject<String>()
            val o2 by findOrSetObject { "foo" }
            val o3 by findObject<String>()
            val o4 by findObject<Int>()

            override fun run_() {
                currentContext.findRoot() shouldBe currentContext
            }
        }

        val c = C().apply { parse(emptyArray()) }

        c.o1 shouldBe null
        c.o2 shouldBe "foo"
        c.o3 shouldBe "foo"
        c.o4 shouldBe null
    }

    @Test
    @JsName("find_functions_parent_context")
    fun `find functions parent context`() {
        val foo = Foo()

        class C : TestCommand(invokeWithoutSubcommand = true) {
            val o1 by findObject<Foo>()
            val o2 by findOrSetObject { foo }
            val o3 by findObject<Foo>()
            val o4 by findObject<Int>()

            override fun run_() {
                currentContext.findRoot() shouldBe currentContext
            }
        }

        val child = C()
        val parent = C().subcommands(child).apply { parse(emptyArray()) }
        parent.o1 shouldBe child.o1
        parent.o1 shouldBe null
        parent.o2 shouldBe child.o2
        parent.o2 shouldBe foo
        parent.o3 shouldBe child.o3
        parent.o3 shouldBe foo
        parent.o4 shouldBe child.o4
        parent.o4 shouldBe null
    }

    @Test
    @JsName("requireObject_with_parent_context")
    fun `requireObject with parent context`() {
        class C : TestCommand(invokeWithoutSubcommand = true) {
            val o1 by findOrSetObject { Foo() }
            val o2 by requireObject<Foo>()
        }

        val child = C()
        val parent = C().subcommands(child).apply { parse(emptyArray()) }

        shouldThrow<NullPointerException> { parent.o2 }
        shouldThrow<NullPointerException> { child.o2 }

        parent.o1 should beInstanceOf(Foo::class)
        parent.o2 shouldBeSameInstanceAs parent.o1
        child.o1 shouldBeSameInstanceAs parent.o1
        child.o2 shouldBeSameInstanceAs parent.o1
    }

    @Test
    @JsName("default_help_option_names")
    fun `default help option names`() {
        class C : TestCommand()

        shouldThrow<PrintHelpMessage> { C().parse("--help") }
        shouldThrow<PrintHelpMessage> { C().parse("-h") }
        shouldThrow<PrintHelpMessage> {
            C().context { helpOptionNames = setOf("-x") }.parse("-x")
        }
        shouldThrow<NoSuchOption> {
            C().context { helpOptionNames = setOf("--x") }.parse("--help")
        }
    }

    @Test
    fun originalArgv() {
        class C : TestCommand()
        class S : TestCommand() {
            val opt by option()
            val args by argument().multiple()
            override fun run_() {
                opt shouldBe "o"
                args shouldBe listOf("1", "2")
                currentContext.originalArgv shouldBe listOf("s", "--opt", "o", "1", "2")
            }
        }

        C().subcommands(S()).parse("s --opt o 1 2")
    }

    @Test
    @JsName("assign_obj_through_context_builder")
    fun `assign obj through context builder`() {
        val foo = Foo()
        val c = TestCommand()
            .context { obj = foo }

        c.parse("")

        c.currentContext.obj shouldBeSameInstanceAs foo
    }
}
