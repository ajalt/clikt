package com.github.ajalt.clikt.parameters

import io.kotest.data.blocking.forAll
import io.kotest.data.row
import kotlin.test.Test

class EnvvarInferTest {
    @Test
    fun inferEnvvar() = forAll(
        row(setOf("--foo"), null, null, null),
        row(setOf("--bar"), null, "FOO", "FOO_BAR"),
        row(setOf("/bar"), null, "FOO", "FOO_BAR"),
        row(setOf("-b"), null, "FOO", "FOO_B"),
        row(setOf("-b", "--bar"), null, "FOO", "FOO_BAR")
    ) { names, envvar, prefix, expected ->
        TODO()
    //        inferEnvvar(names, envvar, prefix) shouldBe expected
    }
}
