package com.github.ajalt.clikt.samples.aliases

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import java.io.File


/**
 * @param configFile A config file containing aliases, one per line, in the form `token = alias`. Aliases can
 *   have multiple tokens (e.g. `cm = commit -m`).
 */
class AliasedCli(private val configFile: File) : NoRunCliktCommand(
        help = "An example that supports aliased subcommands") {
    override fun aliases(): Map<String, List<String>> {
        return configFile.readLines().map { it.split("=", limit = 2) }
                .associate { it[0].trim() to it[1].trim().split(Regex("\\s+")) }
    }
}


class Push : CliktCommand(help = "push changes") {
    override fun run() = TermUi.echo("push")
}

class Pull : CliktCommand(help = "pull changes") {
    override fun run() = TermUi.echo("pull")
}

class Clone : CliktCommand(help = "clone a repository") {
    override fun run() = TermUi.echo("clone")
}

class Commit : CliktCommand(help = "clone a repository") {
    val message by option("-m", "--message").multiple()
    override fun run() = TermUi.echo("commit message=${message.joinToString("\n")}")
}

fun main(args: Array<String>) {
    // The file path is relative to the project root for use with `runsample`
    AliasedCli(File("samples/aliases/src/main/kotlin/com/github/ajalt/clikt/samples/aliases/aliases.cfg"))
            .subcommands(Push(), Pull(), Clone(), Commit())
            .main(args)
}
