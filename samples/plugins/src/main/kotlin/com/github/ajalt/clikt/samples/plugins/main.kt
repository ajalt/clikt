package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.setBinding

data class Repo(var home: String, val config: MutableMap<String, String>, var verbose: Boolean)

class Cli : CliktCommand(
    help = """Repo is a command line tool that showcases how to build complex
        command line interfaces with Clikt.

        This tool is supposed to look like a distributed version control
        system to show how something like this can be structured.""") {
    init {
        versionOption("1.0")
    }

    val repoHome: String by option(help = "Changes the repository folder location.")
        .default(".repo")
    val config: List<Pair<String, String>> by option(help = "Overrides a config key/value pair.")
        .pair()
        .multiple()
    val verbose: Boolean by option("-v", "--verbose", help = "Enables verbose mode.")
        .flag()

    override fun run() {
        val repo = Repo(repoHome, HashMap(), verbose)
        for ((k, v) in config) {
            repo.config[k] = v
        }
        currentContext.obj = repo
    }
}

fun main(args: Array<String>) {
    val kodein = Kodein {
        bind() from setBinding<CliktCommand>()
        import(cloneModule)
        import(deleteModule)
        import(setuserModule)
        import(commitModule)
    }

    val commands: Set<CliktCommand> by kodein.instance()

    Cli().subcommands(commands).main(args)
}
