package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.output.TermUi
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.inSet
import com.github.salomonbrys.kodein.provider

class Delete : CliktCommand(
        help = """Deletes a repository.

        This will throw away the current repository.""") {
    val repo: Repo by requireObject()

    override fun run() {
        TermUi.echo("Destroying repo ${repo.home}")
        TermUi.echo("Deleted!")
    }
}

val deleteModule = Kodein.Module {
    bind<CliktCommand>().inSet() with provider { Delete() }
}
