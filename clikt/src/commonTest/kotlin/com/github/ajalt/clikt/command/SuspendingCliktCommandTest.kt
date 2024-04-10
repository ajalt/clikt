package com.github.ajalt.clikt.command

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parsers.CommandLineParser
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.js.JsName
import kotlin.test.Test


class SuspendingCliktCommandTest {
    @Test
    @JsName("suspending_run")
    fun `suspending run`() = runTest {
        class C : SuspendingCliktCommand() {
            val arg by argument()
            var ran = false

            override suspend fun run() {
                yield()
                arg shouldBe "foo"
                ran = true
            }
        }

        class Sub : SuspendingCliktCommand() {
            var ran = false

            override suspend fun run() {
                ran = true
            }
        }

        val sub = Sub()
        val c: C = C().subcommands(sub)
        c.parse(CommandLineParser.tokenize("foo sub"))
        c.ran shouldBe true
        sub.ran shouldBe true
    }
}
