package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.StaticallyGroupedOption
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parsers.OptionParser
import com.github.ajalt.clikt.parsers.OptionWithValuesParser

private val choices = arrayOf("bash", "zsh", "fish")

internal class CompletionOption(
    override val names: Set<String>,
    override val optionHelp: String,
    override val hidden: Boolean
) : StaticallyGroupedOption {
    override val secondaryNames: Set<String> get() = emptySet()
    override val parser: OptionParser = OptionWithValuesParser
    override fun metavar(context: Context): String = choices.joinToString("|", prefix = "[", postfix = "]")
    override val valueSourceKey: String? get() = null
    override val groupName: String? get() = null
    override val nvalues: Int get() = 1
    override val helpTags: Map<String, String> get() = emptyMap()
    override fun postValidate(context: Context) {}
    override fun finalize(context: Context, invocations: List<OptionParser.Invocation>) {
        val shell = invocations.lastOrNull()?.values?.single() ?: return
        CompletionGenerator.throwCompletionMessage(context.command, shell)
    }
}

/**
 * Add an option to a command that will print a completion script for the given shell when invoked.
 */
fun <T : CliktCommand> T.completionOption(
    vararg names: String = arrayOf("--generate-completion"),
    help: String = "",
    hidden: Boolean = false,
): T = apply {
    registerOption(CompletionOption(names.toSet(), help, hidden))
}

/**
 * A subcommand that will print a completion script for the given shell when invoked.
 */
class CompletionCommand(
    help: String = "Generate a tab-complete script for the given shell",
    epilog: String = "",
    name: String = "generate-completion",
) : CliktCommand(help, epilog, name) {
    private val shell by argument("SHELL").choice(*choices)
    override fun run() {
        CompletionGenerator.throwCompletionMessage(this, shell)
    }
}
