package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.options.Context

abstract class CliktCommand(help: String? = null, version: String? = null) {
    private var subcommands: List<CliktCommand> = emptyList()
    private var options: MutableList<Option<*>> = mutableListOf()

    val context: Context = TODO()
    fun subcommands(vararg command: CliktCommand): CliktCommand {
        subcommands += command
        return this
    }

    open fun main(args: Array<String>): Unit = TODO()
    abstract fun run()

    fun registerOption(option: Option<*>) {
        options.add(option)
    }
}
