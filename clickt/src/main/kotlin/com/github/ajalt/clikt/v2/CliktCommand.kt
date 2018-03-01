package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.*
import kotlin.system.exitProcess

typealias ContextFactory = (CliktCommand, Context2?) -> Context2

// TODO: better help arguments
abstract class CliktCommand(
        val help: String? = null,
        val version: String? = null,  // TODO use this
        name: String? = null,
        private val helpFormatter: HelpFormatter = PlaintextHelpFormatter(help ?: "", ""),
        val allowInterspersedArgs: Boolean = true) {
    companion object {
        private fun defaultContextFactory(): ContextFactory = { cmd, parent -> Context2(parent, cmd) }
    }

    val name = name ?: javaClass.simpleName.toLowerCase() // TODO: better name inference
    internal var subcommands: List<CliktCommand> = emptyList()

    internal val options: MutableList<Option<*>> = mutableListOf()
    internal val arguments: MutableList<Argument<*>> = mutableListOf()

    val context: Context2 by lazy { Context2(null, this) } // TODO: parent

    fun subcommands(vararg commands: CliktCommand): CliktCommand {
        subcommands += commands
        return this
    }

    fun registerOption(option: Option<*>) {
        options += option
    }

    fun registerArgument(argument: Argument<*>) {
        arguments += argument
    }


    fun parse(argv: Array<String>) {
        Parser2.parse(argv, context)
    }

    fun main(argv: Array<String>) {
        try {
            Parser2.parse(argv, context)
        } catch (e: PrintHelpMessage) {
            println(e.command.getFormattedHelp())
            exitProcess(0)
        } catch (e: PrintMessage) {
            println(e.message)
            exitProcess(0)
        } catch (e: UsageError) {
//            println(e.formatMessage(context)) // TODO: update help formatter
            exitProcess(1)
        } catch (e: CliktError) {
            println(e.message)
            exitProcess(1)
        } catch (e: Abort) {
            println()
            exitProcess(1)
        }
    }

    abstract fun run()
}
