package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.NeverCalledCliktCommand
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Test

@Suppress("unused")
class IntTypeTest {
    @Test
    fun `int option`() = forall(
            row("", null),
            row("--xx=4", 4),
            row("-x5", 5)) { argv, expected ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int()
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `int option error`() {
        class C : NeverCalledCliktCommand() {
            val foo by option().int()
        }

        shouldThrow<BadParameterValue> { C().parse(splitArgv("--foo bar")) }
                .message shouldBe "Invalid value for \"--foo\": bar is not a valid integer"
    }

    @Test
    fun `int option with default`() = forall(
            row("", 111),
            row("--xx=4", 4),
            row("-x5", 5)) { argv, expected ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().default(111)
            override fun run() {
                x shouldBe expected
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `int argument`() = forall(
            row("", null, emptyList<Int>()),
            row("1 2", 1, listOf(2)),
            row("1 2 3", 1, listOf(2, 3))) { argv, ex, ey ->
        class C : CliktCommand() {
            val x by argument().int().optional()
            val y by argument().int().multiple()
            override fun run() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(splitArgv(argv))
    }
}
