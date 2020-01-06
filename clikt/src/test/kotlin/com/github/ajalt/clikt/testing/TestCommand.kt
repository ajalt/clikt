package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import io.kotlintest.fail
import io.kotlintest.shouldBe

open class TestCommand(
        private val called: Boolean = true,
        help: String = "",
        epilog: String = "",
        name: String? = null,
        invokeWithoutSubcommand: Boolean = false,
        printHelpOnEmptyArgs: Boolean = false,
        helpTags: Map<String, String> = emptyMap(),
        autoCompleteEnvvar: String? = ""
) : CliktCommand(help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs, helpTags, autoCompleteEnvvar) {
    private var wasCalled = false
    final override fun run() {
        wasCalled shouldBe false
        wasCalled = true
        run_()
    }

    open fun run_() = Unit

    fun parse(argv: String) {
        parse(splitArgv(argv))
        assertCalled(this)
    }

    companion object {
        private fun assertCalled(cmd: CliktCommand) {
            if (cmd is TestCommand) {
                if (cmd.called && !cmd.wasCalled) fail("${cmd.commandName} should have been called")
                if (!cmd.called && cmd.wasCalled) fail("${cmd.commandName} should not be called")
            }
            for (sub in cmd._subcommands) {
                if (sub is TestCommand) {
                    assertCalled(sub)
                }
            }
        }
    }
}
