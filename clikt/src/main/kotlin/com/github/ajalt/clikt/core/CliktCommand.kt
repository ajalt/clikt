package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import com.github.ajalt.clikt.parameters.Argument
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.helpOption
import com.github.ajalt.clikt.parsers.Parser
import kotlin.system.exitProcess

abstract class CliktCommand(
        val help: String = "",
        epilog: String = "",
        name: String? = null,
        val allowInterspersedArgs: Boolean = true,
        val invokeWithoutSubcommand: Boolean = false,
        private val helpOptionNames: Set<String> = setOf("-h", "--help"),
        private val helpOptionMessage: String = "Show this message and exit",
        private val helpFormatter: HelpFormatter = PlaintextHelpFormatter(help, epilog)) {
    val name = name ?: javaClass.simpleName.toLowerCase()
    internal var subcommands: List<CliktCommand> = emptyList()
    internal val options: MutableList<Option> = mutableListOf()
    internal val arguments: MutableList<Argument> = mutableListOf()
    private var _context: Context? = null
    val context: Context
        get() {
            checkNotNull(_context) { "Context accessed before parse has been called." }
            return _context!!
        }

    private fun registeredOptionNames() = options.flatMapTo(HashSet()) { it.names }

    private fun createHelpOption(optionNames: Set<String>) {
        if (optionNames.isEmpty()) return
        val names = optionNames - registeredOptionNames()
        if (names.isNotEmpty()) options += helpOption(names, helpOptionMessage)
        for (command in subcommands) {
            command.createHelpOption(optionNames)
        }
    }

    private fun createContext(parent: Context? = null) {
        _context = Context(parent, this)
        for (command in subcommands) {
            command.createContext(context)
        }
    }

    private fun helpAsSubcommand(): ParameterHelp.Subcommand {
        val shortHelp = help.split(".", "\n", limit = 2).first().trim()
        return ParameterHelp.Subcommand(name, shortHelp)
    }

    private fun allHelpParams() = options.mapNotNull { it.parameterHelp } +
            arguments.mapNotNull { it.parameterHelp } +
            subcommands.map { it.helpAsSubcommand() }

    fun registerOption(option: Option) {
        val names = registeredOptionNames()
        for (name in option.names) {
            require(name !in names) { "Duplicate option name $name" }
        }
        options += option
    }

    fun registerArgument(argument: Argument) {
        arguments += argument
    }

    open fun getFormattedUsage(): String {
        return helpFormatter.formatUsage(allHelpParams(), programName = name)
    }

    open fun getFormattedHelp(): String {
        return helpFormatter.formatHelp(allHelpParams(), programName = name)
    }

    fun parse(argv: Array<String>, context: Context? = null) {
        createContext(context)
        createHelpOption(helpOptionNames)
        Parser.parse(argv, this.context)
    }

    open fun main(argv: Array<String>) {
        try {
            parse(argv)
        } catch (e: PrintHelpMessage) {
            println(e.command.getFormattedHelp())
            exitProcess(0)
        } catch (e: PrintMessage) {
            println(e.message)
            exitProcess(0)
        } catch (e: UsageError) {
            println(e.helpMessage(context))
            exitProcess(1)
        } catch (e: CliktError) {
            println(e.message)
            exitProcess(1)
        } catch (e: Abort) {
            println("Aborted!")
            exitProcess(1)
        }
    }

    abstract fun run()
}

fun <T : CliktCommand> T.subcommands(commands: Iterable<CliktCommand>): T {
    subcommands += commands
    return this
}

fun <T : CliktCommand> T.subcommands(vararg commands: CliktCommand): T {
    subcommands += commands
    return this
}
