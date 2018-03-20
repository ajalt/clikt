package com.github.ajalt.clikt.samples.aliases

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import java.io.File


/**
 * @param configFile A config file containing aliases, one per line, in the form `token = alias`. Aliases can
 *   have multiple tokens (e.g. `cm = commit -m`).
 */
class AliasedCli(private val configFile: File) : CliktCommand(
        help = "An example that supports aliased subcommands") {
    override fun aliases(): Map<String, List<String>> {
        return configFile.readLines().map { it.split("=", limit = 2) }
                .associate { it[0].trim() to it[1].trim().split(Regex("\\s+")) }
    }

    override fun run() = Unit
}


class Push: CliktCommand(help="push changes") {
    override fun run() = println("push")
}

class Pull: CliktCommand(help="pull changes") {
    override fun run() = println("pull")
}

class Clone: CliktCommand(help="clone a repository") {
    override fun run() = println("clone")
}

class Commit: CliktCommand(help="clone a repository") {
    val message by option("-m", "--message").multiple()
    override fun run() = println("commit message=${message.joinToString("\n")}")
}

fun main(args: Array<String>) {
    AliasedCli(File("src/main/kotlin/com/github/ajalt/clikt/samples/aliases/aliases.cfg"))
            .subcommands(Push(), Pull(), Clone(), Commit())
            .main(args)
}
