package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test


class CliktCommandTest {
    @Test
    fun `invokeWithoutSubcommand=false`() {
        class C : CliktCommand(name = "foo") {
            var ran = false
            override fun run() {
                ran = true
            }
        }

        C().apply {
            parse(emptyArray())
            ran shouldBe true
        }

        var child = C()
        C().subcommands(child).apply {
            shouldThrow<PrintHelpMessage> {
                parse(emptyArray())
            }
            ran shouldBe false
            child.ran shouldBe false
        }

        child = C()
        C().subcommands(child).apply {
            parse(splitArgv("foo"))
            ran shouldBe true
            context.invokedSubcommand shouldBe child
            child.ran shouldBe true
            child.context.invokedSubcommand shouldBe null
        }
    }

    @Test
    fun `invokeWithoutSubcommand=true`() {
        class C : CliktCommand(name = "foo", invokeWithoutSubcommand = true) {
            var ran = false
            override fun run() {
                ran = true
            }
        }

        C().apply {
            parse(emptyArray())
            ran shouldBe true
        }

        var child = C()
        C().subcommands(listOf(child)).apply {
            parse(emptyArray())
            ran shouldBe true
            context.invokedSubcommand shouldBe null
            child.ran shouldBe false
        }

        child = C()
        C().subcommands(child).apply {
            parse(splitArgv("foo"))
            ran shouldBe true
            context.invokedSubcommand shouldBe child
            child.ran shouldBe true
            child.context.invokedSubcommand shouldBe null
        }
    }

    @Test
    fun `aliases`() = parameterized(
            row("-xx", "x", emptyList()),
            row("a", "a", listOf("b")),
            row("a", "a", listOf("b")),
            row("b", null, listOf("-xa")),
            row("recurse", null, listOf("recurse")),
            row("recurse2", "foo", listOf("recurse", "recurse2"))
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            val y by argument().multiple()
            override fun run() {
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

        C().parse(splitArgv(argv))
    }
}
