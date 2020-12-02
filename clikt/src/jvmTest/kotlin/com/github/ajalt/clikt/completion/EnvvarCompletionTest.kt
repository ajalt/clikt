package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldContain
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import kotlin.test.Test


class EnvvarCompletionTest {
    @Rule
    @JvmField
    val env = EnvironmentVariables()

    @Test
    fun `test completion from envvar`() = forAll(
        row("bash"),
        row("zsh"),
        row("fish")
    ) { shell ->
        env.set("TEST_COMPLETE", shell)
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE")

        shouldThrow<PrintCompletionMessage> {
            C().subcommands(C()).parse("")
        }.message shouldContain shell
    }
}
