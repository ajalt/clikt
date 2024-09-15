package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.test.assertEquals

class TestException(message: String) : Exception(message)

open class TestCommand(
    called: Boolean = true,
    count: Int? = null,
    private val help: String = "",
    private val epilog: String = "",
    name: String? = null,
    override val invokeWithoutSubcommand: Boolean = false,
    override val printHelpOnEmptyArgs: Boolean = false,
    override val helpTags: Map<String, String> = emptyMap(),
    override val autoCompleteEnvvar: String? = "",
    override val allowMultipleSubcommands: Boolean = false,
    override val treatUnknownOptionsAsArgs: Boolean = false,
    override val hiddenFromHelp: Boolean = false,
    noHelp: Boolean = false,
) : CliktCommand(name) {
    init {
        context {
            terminal = parent?.terminal ?: Terminal(AnsiLevel.NONE)
            if (noHelp) helpOptionNames = emptyList()
        }
    }

    override fun help(context: Context): String = help

    override fun helpEpilog(context: Context): String = epilog

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
    parse(CommandLineParser.tokenize(argv))
    TestCommand.assertCalled(this)
    return this
}

