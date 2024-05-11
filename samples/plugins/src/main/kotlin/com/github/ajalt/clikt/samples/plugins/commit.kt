package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider
import java.io.File

class Commit : CliktCommand() {
    override fun help(context: Context) = """
        Commits outstanding changes.

        Commit changes to the given files into the repository.  You will need to
        "repo push" to push up your changes to other repositories.

        If a list of files is omitted, all changes reported by "repo status"
        will be committed.
        """.trimIndent()

    val repo: Repo by requireObject()
    val message: List<String> by option("--message", "-m").multiple()
        .help(
            "The commit message. If provided multiple times " +
                    "each argument gets converted into a new line."
        )
    val files: List<File> by argument()
        .file()
        .multiple()

    override fun run() {
        echo("Files to be committed: $files")
        echo("Commit message:")
        echo(message.joinToString("\n"))
    }
}

val commitModule = Kodein.Module("commit") {
    bind<CliktCommand>().inSet() with provider { Commit() }
}
