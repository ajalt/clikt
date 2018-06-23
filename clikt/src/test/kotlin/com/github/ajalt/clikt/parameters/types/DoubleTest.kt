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
class DoubleTest {
    @Test
    fun `double option`() = forall(
            row("", null),
            row("--xx 3", 3.0),
            row("--xx=4.0", 4.0),
            row("-x5.5", 5.5)) { argv, expected ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").double()
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `double option error`() {
        class C : NeverCalledCliktCommand() {
            val foo by option().double()
        }

        shouldThrow<BadParameterValue> { C().parse(splitArgv("--foo bar")) }
                .message shouldBe "Invalid value for \"--foo\": bar is not a valid floating point value"
    }

    @Test
    fun `double option with default`() = forall(
            row("", -1.0),
            row("--xx=4.0", 4.0),
            row("-x5.5", 5.5)) { argv, expected ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").double().default(-1.0)
            override fun run() {
                x shouldBe expected
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `double argument`() = forall(
            row("", null, emptyList<Float>()),
            row("1.1 2", 1.1, listOf(2.0)),
            row("1.1 2 3", 1.1, listOf(2.0, 3.0))) { argv, ex, ey ->
        class C : CliktCommand() {
            val x by argument().double().optional()
            val y by argument().double().multiple()
            override fun run() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(splitArgv(argv))
    }
}
