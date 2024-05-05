package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EnvvarInferTest {
    @Test
    fun inferEnvvar() = forAll(
        row(arrayOf("--foo"), null, null),
        row(arrayOf("--bar"), "FOO", "FOO_BAR"),
        row(arrayOf("/bar"), "FOO", "FOO_BAR"),
        row(arrayOf("-b"), "FOO", "FOO_B"),
        row(arrayOf("-b", "--bar"), "FOO", "FOO_BAR")
    ) { names, prefix, expected ->
        class C : TestCommand(autoCompleteEnvvar = null) {
            val o by option(*names)

            init {
                context {
                    // Return the key as the value of the envvar
                    envvarReader = { it }
                    autoEnvvarPrefix = prefix
                }
            }
        }
        C().parse("").o shouldBe expected
    }
}
