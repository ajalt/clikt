package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parsers.FlagOptionParser
import com.github.ajalt.clikt.parsers.OptionParser

class EagerOption(
        override val help: String,
        override val names: Set<String>,
        private val callback: EagerOption.(Context, String) -> Unit) : Option {
    override val secondaryNames: Set<String> get() = emptySet()
    override val parser: OptionParser = FlagOptionParser
    override val metavar: String? get() = null
    override val nargs: Int get() = 0
    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        this.callback(context, invocations.first().name)
    }
}

internal fun helpOption(names: Set<String>, message: String) = EagerOption(message, names, { ctx, _ ->
    throw PrintHelpMessage(ctx.command)
})

inline fun <T : CliktCommand> T.versionOption(
        version: String,
        help: String = "Show the version and exit.",
        names: Set<String> = setOf("--version"),
        crossinline message: (String) -> String = { "$name version $it" }): T {
    registerOption(EagerOption(help, names) { _, _ ->
        throw PrintMessage(message(version))
    })
    return this
}
