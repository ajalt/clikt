package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import org.assertj.core.api.Assertions.fail

open class NeverCalledCliktCommand(help: String = "",
                              epilog: String = "",
                              name: String? = null,
                              invokeWithoutSubcommand: Boolean = false)
    : CliktCommand(help, epilog, name, invokeWithoutSubcommand) {
    override fun run() = fail("run should not be called")
}
