package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.fail

class ContextTest {
    class Foo

    @[Test JsName("find_functions_single_context")]
    fun `find functions single context`() {
        class C : TestCommand() {
            val o1 by findObject<String>()
            val o2 by findOrSetObject { "foo" }
            val o3 by findObject<String>()
            val o4 by findObject<Int>()

            val o5 by findObject<String>("key")
            val o6 by findOrSetObject("key") { "foo" }
            val o7 by findObject<String>("key")
            val o8 by requireObject<String>("key")

            override fun run_() {
                currentContext.findRoot() shouldBe currentContext
            }
        }

        val c = C().apply { parse(emptyArray()) }

        c.o1 shouldBe null
        c.o2 shouldBe "foo"
        c.o3 shouldBe "foo"
        c.o4 shouldBe null

        c.o5 shouldBe null
        c.o6 shouldBe "foo"
        c.o7 shouldBe "foo"
        c.o8 shouldBe "foo"
    }

    @[Test JsName("find_functions_parent_context")]
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

    @[Test JsName("requireObject_with_parent_context")]
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

    @[Test JsName("default_help_option_names")]
    fun `default help option names`() {
        class C : TestCommand()

        shouldThrow<PrintHelpMessage> {
            C().parse("--help")
        }.statusCode shouldBe 0
        shouldThrow<PrintHelpMessage> { C().parse("-h") }
        shouldThrow<PrintHelpMessage> {
            C().context { helpOptionNames = setOf("-x") }.parse("-x")
        }
        shouldThrow<NoSuchOption> {
            C().context { helpOptionNames = setOf("--x") }.parse("--help")
        }
    }

    @[Test JsName("assign_obj_through_context_builder")]
    fun `assign obj through context builder`() {
        val foo = Foo()
        val c = TestCommand()
            .context { obj = foo }

        c.parse("")

        c.currentContext.obj shouldBeSameInstanceAs foo
    }

    @[Test JsName("register_closeable_multiple_subcommands")]
    fun `register closeable multiple subcommands`() {
        var parentCount = 0
        var childCount = 0
        var childSaw0 = 0
        var childSaw1 = 0

        class Parent : TestCommand(allowMultipleSubcommands = true) {
            override fun run_() {
                currentContext.callOnClose { parentCount++ }
            }
        }


        class Child : TestCommand(count = 2) {
            override fun run_() {
                when (childCount) {
                    0 -> childSaw0++
                    1 -> childSaw1++
                    else -> fail("too many calls")
                }
                parentCount shouldBe 0
                val c = object : AutoCloseable {
                    override fun close() {
                        childCount++
                    }
                }
                currentContext.registerCloseable(c) shouldBeSameInstanceAs c
            }
        }
        Parent().subcommands(Child()).parse("child child")
        parentCount shouldBe 1
        childSaw0 shouldBe 1
        childSaw1 shouldBe 1
        childCount shouldBe 2
    }

    @[Test JsName("register_closeable_throws")]
    fun `register closeable throws`() {
        var count1 = 0
        var count2 = 0
        var count3 = 0

        class E1 : Exception()
        class E2 : Exception()
        class E3 : Exception()

        class C : TestCommand() {
            override fun run_() {
                currentContext.callOnClose {
                    count1++
                    throw E1()
                }
                currentContext.callOnClose {
                    count2++
                    throw E2()
                }
                currentContext.callOnClose {
                    count3++
                    throw E3()
                }
            }
        }
        shouldThrow<E3> {
            C().parse("")
        }

        count1 shouldBe 1
        count2 shouldBe 1
        count3 shouldBe 1
    }

    @[Test JsName("custom_exitProcess")]
    fun `custom exitProcess`() {
        var status = Int.MAX_VALUE

        class C : NoOpCliktCommand() {
            init {
                context {
                    exitProcess = { status = it }
                    echoMessage = { _, _, _, _ -> }
                }
            }
        }
        C().main(listOf("--help"))
        status shouldBe 0

        status = Int.MAX_VALUE
        C().main(listOf("--foo"))
        status shouldBe 1
    }
}
