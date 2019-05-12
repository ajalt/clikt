package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import io.kotlintest.fail

open class NeverCalledCliktCommand(
        help: String = "",
        epilog: String = "",
        name: String? = null,
        invokeWithoutSubcommand: Boolean = false,
        printHelpOnEmptyArgs: Boolean = false
) : CliktCommand(help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs) {
    final override fun run() = fail("run should not be called")
}
