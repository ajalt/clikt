package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.defaultLocalization
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@Suppress("unused")
class MultiUsageErrorTest {
    @Test
    fun optionalValue() = forAll(
        row("", listOf("Missing argument \"A\"", "Missing option \"--x\"", "Missing option \"--y\"")),
        row("--y=1", listOf("Missing argument \"A\"", "Missing option \"--x\"")),
        row("--x=foo 1", listOf("Invalid value for \"--x\": foo is not a valid integer", "Missing option \"--y\"")),
        row("--x=0 --y=0 1", listOf("Invalid value for \"A\": 1")),
        row("--y=0 --x=0 --n 1 2 3", listOf("no such option: \"--n\". (Possible options: --x, --y)")), // don't report arg error after unknown opts
        ) { argv, ex ->
        class C : TestCommand(called = false) {
            val x by option().int().required().check { it == 0 }
            val y by option().int().required().check { it == 0 }
            val a by argument().int().check { it == 0 }
        }

        val e = shouldThrow<UsageError> {
            C().parse(argv)
        }
        ((e as? MultiUsageError)?.errors ?: listOf(e))
            .map { it.formatMessage(defaultLocalization) }
            .shouldBe(ex)
    }
}
