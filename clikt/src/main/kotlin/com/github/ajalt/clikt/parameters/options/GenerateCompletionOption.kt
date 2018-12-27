package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parsers.OptionParser
import com.github.ajalt.clikt.parsers.OptionWithValuesParser

/**
 * An [Option] that generates the auto completion commands for a shell.
 */
class GenerateCompletionOption(
    override val names: Set<String>,
    override val help: String,
    override val hidden: Boolean
) : Option {
    override val nvalues: Int
        get() = 1

    override val secondaryNames: Set<String>
        get() = emptySet()

    override val parser: OptionParser = OptionWithValuesParser

    override val metavar: String?
        get() = "SHELL"

    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        TODO("Not implemented")
    }
}

fun <T : CliktCommand> T.generateCompletionOption(
    help: String = "Generate the auto completion commands for a shell",
    names: Set<String> = setOf("--generate-completion"),
    hidden: Boolean = false
): T =
    apply {
        registerOption(GenerateCompletionOption(names, help, hidden))
    }
