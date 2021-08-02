package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class SetUser : CliktCommand(
    name = "setuser",
    help = """Sets the user credentials.

        This will override the current user config.""") {
    val repo: Repo by requireObject()
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
        TermUi.echo("Changed credentials.")
    }
}

val setuserModule = Kodein.Module("setuser") {
    bind<CliktCommand>().inSet() with provider { SetUser() }
}
