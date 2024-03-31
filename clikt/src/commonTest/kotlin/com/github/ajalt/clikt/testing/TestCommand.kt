package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parsers.shlex
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.test.assertEquals

open class TestCommand(
    called: Boolean = true,
    count: Int? = null,
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = "",
    allowMultipleSubcommands: Boolean = false,
    treatUnknownOptionsAsArgs: Boolean = false,
    hidden: Boolean = false,
) : CliktCommand(
    help,
    epilog,
    name,
    invokeWithoutSubcommand,
    printHelpOnEmptyArgs,
    helpTags,
    autoCompleteEnvvar,
    allowMultipleSubcommands,
    treatUnknownOptionsAsArgs,
    hidden
) {
    init {
        context {
            terminal = parent?.terminal ?: Terminal(AnsiLevel.NONE)
        }
    }

    private val count = count ?: if (called) 1 else 0
    private var actualCount = 0
    final override fun run() {
        actualCount++
        run_()
    }

    open fun run_() = Unit

    companion object {
        fun assertCalled(cmd: BaseCliktCommand<*>) {
            if (cmd is TestCommand) {
                assertEquals(cmd.count, cmd.actualCount, "${cmd.commandName} call count")
            }
            for (sub in cmd.registeredSubcommands()) {
                assertCalled(sub)
            }
        }
    }
}

fun <T : TestCommand> T.parse(argv: String): T {
    parse(shlex("test", argv))
    TestCommand.assertCalled(this)
    return this
}
