package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.inSet
import com.github.salomonbrys.kodein.provider
import java.io.File

class Clone : CliktCommand(
        help = """Clones a repository.

        This will clone the repository at SRC into the folder DEST. If DEST
        is not provided this will automatically use the last path component
        of SRC and create that folder.""") {
    val repo: Repo by requireObject()
    val src: String by argument()
    val dest: String? by argument().optional()
    val shallow: Boolean by option(help = "Makes a checkout shallow or deep.  Deep by default.")
            .switch("--shallow" to true, "--deep" to false)
            .default(false)

    val rev: String by option("--rev", "-r", help = "Clone a specific revision instead of HEAD.")
            .default("HEAD")

    override fun run() {
        val destName = dest ?: File(src).name
        TermUi.echo("Cloning repo $src to ${File(destName).absolutePath}")
        repo.home = destName
        if (shallow) {
            TermUi.echo("Making shallow checkout")
        }
        TermUi.echo("Checking out revision $rev")
    }
}

val cloneModule = Kodein.Module {
    bind<CliktCommand>().inSet() with provider { Clone() }
}
