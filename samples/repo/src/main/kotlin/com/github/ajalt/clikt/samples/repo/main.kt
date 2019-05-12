package com.github.ajalt.clikt.samples.repo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

data class RepoConfig(var home: String, val config: MutableMap<String, String>, var verbose: Boolean)

class Repo : CliktCommand(
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
        val repo = RepoConfig(repoHome, HashMap(), verbose)
        for ((k, v) in config) {
            repo.config[k] = v
        }
        context.obj = repo
    }
}


class Clone : CliktCommand(
        help = """Clones a repository.

        This will clone the repository at SRC into the folder DEST. If DEST
        is not provided this will automatically use the last path component
        of SRC and create that folder.""") {
    val repo: RepoConfig by requireObject()
    val src: File by argument().file()
    val dest: File? by argument().file().optional()
    val shallow: Boolean by option(help = "Makes a checkout shallow or deep.  Deep by default.")
            .flag("--deep")

    val rev: String by option("--rev", "-r", help = "Clone a specific revision instead of HEAD.")
            .default("HEAD")

    override fun run() {
        val destName = dest?.name ?: src.name
        echo("Cloning repo $src to ${File(destName).absolutePath}")
        repo.home = destName
        if (shallow) {
            echo("Making shallow checkout")
        }
        echo("Checking out revision $rev")
    }
}

class Delete : CliktCommand(
        help = """Deletes a repository.

        This will throw away the current repository.""") {
    val repo: RepoConfig by requireObject()

    override fun run() {
        echo("Destroying repo ${repo.home}")
        echo("Deleted!")
    }
}

class SetUser : CliktCommand(
        name = "setuser",
        help = """Sets the user credentials.

        This will override the current user config.""") {
    val repo: RepoConfig by requireObject()
    val username: String by option(help = "The developer's shown username.")
            .prompt()
    val email: String by option(help = "The developer's email address.")
            .prompt(text = "E-Mail")
    val password: String by option(help = "The login password.")
            .prompt(hideInput = true, requireConfirmation = true)

    override fun run() {
        repo.config["username"] = username
        repo.config["email"] = email
        repo.config["password"] = "*".repeat(password.length)
        echo("Changed credentials.")
    }
}


class Commit : CliktCommand(
        help = """Commits outstanding changes.

        Commit changes to the given files into the repository.  You will need to
        "repo push" to push up your changes to other repositories.

        If a list of files is omitted, all changes reported by "repo status"
        will be committed.""") {
    val repo: RepoConfig by requireObject()
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

fun main(args: Array<String>) = Repo()
        .subcommands(Clone(), Delete(), SetUser(), Commit())
        .main(args)


