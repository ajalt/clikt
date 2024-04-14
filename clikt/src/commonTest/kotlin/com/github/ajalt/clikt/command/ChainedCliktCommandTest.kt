package com.github.ajalt.clikt.command

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.js.JsName
import kotlin.test.Test


class ChainedCliktCommandTest {
    @Test
    @JsName("chained_run")
    fun `chained run`() = runTest {
        class C : ChainedCliktCommand<List<Int>>() {
            override val allowMultipleSubcommands: Boolean = true
            override fun run(value: List<Int>): List<Int> {
                return value + 1
            }
        }

        class Sub : ChainedCliktCommand<List<Int>>() {
            val arg by argument().int()
            override fun run(value: List<Int>): List<Int> {
                return value + arg
            }
        }

        val sub = Sub()
        val c: C = C().subcommands(sub)
        val result = c.parse(listOf("sub", "2", "sub", "3"), emptyList())
        result shouldBe listOf(1, 2, 3)
    }


    @Test
    @JsName("chained_command_context")
    fun `chained command context`() {
        class C : ChainedCliktCommand<Int>() {
            val arg by argument().int()
            val ctx by requireObject<Int>()
            override fun run(value: Int): Int {
                return value + arg + ctx
            }
        }

        C().context { obj = 10 }.parse(listOf("1"), 100) shouldBe 111
    }


    @Test
    @JsName("chained_command_test")
    fun `chained command test`()  {
        class C : ChainedCliktCommand<Int>() {
            val arg by argument().int()
            override fun run(value: Int): Int {
                echo(value + arg)
                return 0
            }
        }

        C().test("10", 1).output shouldBe "11\n"
    }
}
