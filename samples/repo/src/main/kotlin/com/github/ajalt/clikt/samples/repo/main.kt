package com.github.ajalt.clikt.samples.repo

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

data class RepoConfig(
    var home: String,
    val config: MutableMap<String, String>,
    var verbose: Boolean,
)

class Repo : CliktCommand() {
    override fun help(context: Context): String = """
    Repo is a command line tool that showcases how to build complex
    command line interfaces with Clikt.

    This tool is supposed to look like a distributed version control
    system to show how something like this can be structured.
    """.trimIndent()

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
        currentContext.obj = repo
    }
}


class Clone : CliktCommand() {
    override fun help(context: Context): String = """
    Clones a repository.

    This will clone the repository at SRC into the folder DEST. If DEST
    is not provided this will automatically use the last path component
    of SRC and create that folder.
    """.trimIndent()

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

class Delete : CliktCommand() {
    override fun help(context: Context): String = """
    Deletes a repository.

    This will throw away the current repository.
    """.trimIndent()

    val repo: RepoConfig by requireObject()

    override fun run() {
        echo("Destroying repo ${repo.home}")
        echo("Deleted!")
    }
}

class SetUser : CliktCommand(name = "setuser") {
    override fun help(context: Context): String = """
    Sets the user credentials.

    This will override the current user config.
    """.trimIndent()

    val repo: RepoConfig by requireObject()
    val username: String by option(help = "The developer's shown username.")
        .prompt()
    val email: String by option(help = "The developer's email address.")
        .prompt(text = "E-Mail")
    val password: String by option(help = "The login password.")
        .prompt(hideInput = true)

    override fun run() {
        repo.config["username"] = username
        repo.config["email"] = email
        repo.config["password"] = "*".repeat(password.length)
        echo("Changed credentials.")
    }
}


class Commit : CliktCommand() {
    override fun help(context: Context) = """
    Commits outstanding changes.

    Commit changes to the given files into the repository.  You will need to
    "repo push" to push up your changes to other repositories.

    If a list of files is omitted, all changes reported by "repo status"
    will be committed.
    """.trimIndent()

    val repo: RepoConfig by requireObject()
    val message: List<String> by option("--message", "-m").multiple()
        .help(
            "The commit message. If provided multiple times " +
                    "each argument gets converted into a new line."
        )
    val files: List<File> by argument().file().multiple()

    override fun run() {
        echo("Files to be committed: $files")
        echo("Commit message:")
        echo(message.joinToString("\n"))
    }
}

fun main(args: Array<String>) = Repo()
    .subcommands(Clone(), Delete(), SetUser(), Commit())
    .main(args)
