package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Test

class LongTypeTest {
    @Test
    fun `int option`() = forall(
            row("", null),
            row("--xx=4", 4L),
            row("-x5", 5L)) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").long()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    fun `int option error`() {
        class C : TestCommand(called = false) {
            val foo by option().long()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
                .message shouldBe "Invalid value for \"--foo\": bar is not a valid integer"
    }

    @Test
    fun `int option with default`() = forall(
            row("", 111L),
            row("--xx=4", 4L),
            row("-x5", 5L)) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").long().default(111L)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    fun `int argument`() = forall(
            row("", null, emptyList<Long>()),
            row("1 2", 1L, listOf(2L)),
            row("1 2 3", 1L, listOf(2L, 3L))) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().long().optional()
            val y by argument().long().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }
}
