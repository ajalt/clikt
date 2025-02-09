package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.test
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class ExceptionsTest {
    @[Test JsName("exceptions_statusCode")]
    fun `exceptions statusCode`() = forAll(
        row(CliktError(), 1),
        row(CliktError(statusCode = 2), 2),
        row(UsageError(""), 1),
        row(UsageError("", statusCode = 2), 2),
        row(PrintHelpMessage(null), 0),
        row(PrintHelpMessage(null, statusCode = 2), 2),
        row(PrintMessage(""), 0),
        row(PrintMessage("", statusCode = 2), 2),
        row(PrintCompletionMessage(""), 0),
        row(Abort(), 1),
        row(ProgramResult(2), 2),
        row(NoSuchOption(""), 1),
    ) { err, code ->
        class C : TestCommand() {
            override fun run_() {
                throw err
            }
        }
        C().test("").statusCode shouldBe code
    }
}
