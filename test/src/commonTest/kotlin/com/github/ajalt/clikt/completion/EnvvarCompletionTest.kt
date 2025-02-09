package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test


class EnvvarCompletionTest {
    @[Test JsName("test_completion_from_envvar")]
    fun `test completion from envvar`() = forAll(
        row("bash"),
        row("zsh"),
        row("fish")
    ) { shell ->
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE") {
            init {
                context {
                    readEnvvar = mapOf("TEST_COMPLETE" to shell)::get
                }
            }
        }

        shouldThrow<PrintCompletionMessage> {
            C().subcommands(C()).parse("")
        }.message shouldContain shell
    }
}
