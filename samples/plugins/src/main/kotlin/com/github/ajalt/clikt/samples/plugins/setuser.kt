package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.inSet
import com.github.salomonbrys.kodein.provider

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

val setuserModule = Kodein.Module {
    bind<CliktCommand>().inSet() with provider { SetUser() }
}
