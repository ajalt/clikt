package com.github.ajalt.clikt.command

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parsers.CommandLineParser
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.js.JsName
import kotlin.test.Test


class ChainedCliktCommandTest {
    @Test
    @JsName("chained_run")
    fun `chained run`() = runTest {
        class C : ChainedCliktCommand<List<Int>>(
            allowMultipleSubcommands = true
        ) {
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
        val result = c.parse(listOf("sub", "2",  "sub", "3"), emptyList())
        result shouldBe listOf(1, 2, 3)
    }
}
