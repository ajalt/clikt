package com.github.ajalt.clikt.samples.plugins

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Delete : CliktCommand() {
    override fun help(context: Context) = """
    Deletes a repository.

    This will throw away the current repository.
    """.trimIndent()

    val repo: Repo by requireObject()

    override fun run() {
        echo("Destroying repo ${repo.home}")
        echo("Deleted!")
    }
}

val deleteModule = Kodein.Module("delete") {
    bind<CliktCommand>().inSet() with provider { Delete() }
}
