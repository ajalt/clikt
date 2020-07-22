package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import io.kotest.matchers.shouldBe
import kotlin.test.assertEquals
import kotlin.test.fail

open class TestCommand(
        private val called: Boolean = true,
        private val count: Int? = null,
        help: String = "",
        epilog: String = "",
        name: String? = null,
        invokeWithoutSubcommand: Boolean = false,
        printHelpOnEmptyArgs: Boolean = false,
        helpTags: Map<String, String> = emptyMap(),
        autoCompleteEnvvar: String? = "",
        allowMultipleSubcommands: Boolean = false,
        treatUnknownOptionsAsArgs: Boolean = false
) : CliktCommand(
        help,
        epilog,
        name,
        invokeWithoutSubcommand,
        printHelpOnEmptyArgs,
        helpTags,
        autoCompleteEnvvar,
        allowMultipleSubcommands,
        treatUnknownOptionsAsArgs
) {
    private var actualCount = 0
    final override fun run() {
        if (count == null) actualCount shouldBe 0
        actualCount++
        run_()
    }

    open fun run_() = Unit

    companion object {
        fun assertCalled(cmd: CliktCommand) {
            if (cmd is TestCommand) {
                if (cmd.count != null) {
                    assertEquals(cmd.count, cmd.actualCount, "command call count")
                } else {
                    if (cmd.called && cmd.actualCount == 0) fail("${cmd.commandName} should have been called")
                    if (!cmd.called && cmd.actualCount > 0) fail("${cmd.commandName} should not be called")
                }
            }
            for (sub in cmd.registeredSubcommands()) {
                assertCalled(sub)
            }
        }
    }
}

fun <T: TestCommand>T.parse(argv: String): T {
    parse(splitArgv(argv))
    TestCommand.assertCalled(this)
    return this
}
