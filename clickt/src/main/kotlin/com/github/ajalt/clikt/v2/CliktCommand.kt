package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.options.Context

abstract class CliktCommand(help: String? = null, version: String? = null) {
    private var subcommands: List<CliktCommand> = emptyList()
    private val optionsByName: MutableMap<String, Option<*>> = mutableMapOf()
    private val argumentsByName: MutableMap<String, Argument<*>> = mutableMapOf()

    val context: Context = TODO()
    fun subcommands(vararg command: CliktCommand): CliktCommand {
        subcommands += command
        return this
    }

    open fun main(args: Array<String>): Unit = TODO()

    fun registerOption(option: Option<*>) {
        for (name in option.names) {
            optionsByName[name] = option
        }
    }

    fun registerArgument(argument: Argument<*>) {
        argumentsByName[argument.name] = argument
    }

    abstract fun run()
}
