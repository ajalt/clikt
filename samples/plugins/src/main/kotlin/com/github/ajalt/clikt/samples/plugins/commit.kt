package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.inSet
import com.github.salomonbrys.kodein.provider
import java.io.File

class Commit : CliktCommand(
        help = """Commits outstanding changes.

        Commit changes to the given files into the repository.  You will need to
        "repo push" to push up your changes to other repositories.

        If a list of files is omitted, all changes reported by "repo status"
        will be committed.""") {
    val repo: Repo by requireObject()
    val message: List<String> by option("--message", "-m",
            help = "The commit message. If provided multiple times " +
                    "each argument gets converted into a new line.")
            .multiple()
    val files: List<File> by argument()
            .file()
            .multiple()

    override fun run() {
        val msg: String = if (message.isNotEmpty()) {
            message.joinToString("\n")
        } else {
            val marker = "# Files to be committed:"
            val text = buildString {
                append("\n\n").append(marker).append("\n#")
                for (file in files) {
                    append("\n#   ").append(file)
                }
            }

            val message = TermUi.editText(text)
            if (message == null) {
                echo("Aborted!")
                return
            }
            message.split(marker, limit = 2)[0].trim().apply {
                if (this.isEmpty()) {
                    echo("Aborting commit due to empty commit message.")
                    return
                }
            }
        }
        echo("Files to be commited: $files")
        echo("Commit message:")
        echo(msg)
    }
}

val commitModule = Kodein.Module {
    bind<CliktCommand>().inSet() with provider { Commit() }
}
