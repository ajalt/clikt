package com.github.ajalt.clickt.samples.repo

import com.github.ajalt.clikt.options.*
import com.github.ajalt.clikt.parser.Command
import java.io.File

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PassRepo

data class Repo(var home: String, val config: MutableMap<String, String>, var verbose: Boolean)

@ClicktCommand(name = "repo", help =
"""Repo is a command line tool that showcases how to build complex
command line interfaces with Clickt.

This tool is supposed to look like a distributed version control
system to show how something like this can be structured.""")
@AddVersionOption("1.0")
fun cli(@PassContext context: Context,
        @StringOption("--repo-home", default = ".repo", // TODO: custom metavars
                help = "Changes the repository folder location.") repoHome: String,
        @StringOption(nargs = 2, help = "Overrides a config key/value pair.") config: List<String>?, // TODO: multiple
        @FlagOption("-v", "--verbose", help = "Enables verbose mode.") verbose: Boolean) {
    val repo = Repo(repoHome, HashMap(), verbose)
    if (config != null) repo.config[config[0]] = config[1]
    context.obj = repo
}

@ClicktCommand(help =
"""Clones a repository.

This will clone the repository at SRC into the folder DEST. If DEST
is not provided this will automatically use the last path component
of SRC and create that folder.""")
fun clone(@PassRepo repo: Repo,
          @StringArgument(required = true) src: String, // TODO: required = true by default
          @StringArgument dest: String?,
          @FlagOption("--shallow/--deep", default = false,
                  help = "Makes a checkout shallow or deep.  Deep by default.") shallow: Boolean,
          @StringOption("--rev", "-r", default = "HEAD",
                  help = "Clone a specific revision instead of HEAD.") rev: String) {
    val destName = dest ?: File(src).name
    println("Cloning repo $src to ${File(destName).absolutePath}")
    repo.home = destName
    if (shallow) {
        println("Makeing shallow checkout")
    }
    println("Checking out revision $rev")
}

@ClicktCommand(help =
"""Deletes a repository.

This will throw away the current repository.""")
fun delete(@PassRepo repo: Repo) {
    println("Destroying repo ${repo.home}")
    println("Deleted!")
}

@ClicktCommand(help =
"""Sets the user credentials.

This will override the current user config.""")
fun setuser(@PassRepo repo: Repo,
            @StringOption(help = "The developer's shown username.") username: String?, // TODO prompt
            @StringOption(help = "The developer's email address.") email: String?,
            @StringOption(help = "The login password.") password: String?) {// TODO password_option
    username?.let { repo.config["username"] = it }
    email?.let { repo.config["email"] = it }
    password?.let { repo.config["password"] = "*".repeat(password.length) }
    println("Changed credentials.")
}

@ClicktCommand(help =
"""Commits outstanding changes.

Commit changes to the given files into the repository.  You will need to
"repo push" to push up your changes to other repositories.

If a list of files is omitted, all changes reported by "repo status"
will be committed.""")
fun commit(@PassRepo repo: Repo,
           @StringOption(help = "The commit message.") message: String?,
           @FileArgument(nargs = -1) files: List<File>?) {
    val msg = if (message == null) {
        message // TODO: click.edit
    } else {
        message
    }
    println("Files to be commited: $files")
    println("Commit message:")
    println(msg)
}


fun main(args: Array<String>) = Command.build(::cli) {
    passObjectParameter<PassRepo> { it.findObject<Repo>() }
    subcommand(::clone)
    subcommand(::delete)
    subcommand(::setuser)
    subcommand(::commit)
}.main(args)
