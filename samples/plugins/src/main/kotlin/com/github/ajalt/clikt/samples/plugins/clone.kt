package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider
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
            .flag("--deep")

    val rev: String by option("--rev", "-r", help = "Clone a specific revision instead of HEAD.")
            .default("HEAD")

    override fun run() {
        val destName = dest ?: File(src).name
        echo("Cloning repo $src to ${File(destName).absolutePath}")
        repo.home = destName
        if (shallow) {
            echo("Making shallow checkout")
        }
        echo("Checking out revision $rev")
    }
}

val cloneModule = Kodein.Module("clone") {
    bind<CliktCommand>().inSet() with provider { Clone() }
}
