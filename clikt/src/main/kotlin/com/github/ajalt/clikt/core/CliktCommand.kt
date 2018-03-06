package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import com.github.ajalt.clikt.parameters.*
import com.github.ajalt.clikt.parsers.Parser
import kotlin.system.exitProcess

// TODO: better output arguments
abstract class CliktCommand(
        val help: String = "",
        epilog: String = "",
        name: String? = null,
        val allowInterspersedArgs: Boolean = true,
        private val helpOptionNames: Set<String> = setOf("-h", "--help"),
        private val helpOptionMessage: String = "Show this message and exit",
        private val helpFormatter: HelpFormatter = PlaintextHelpFormatter(help, epilog)) {
    val name = name ?: javaClass.simpleName.toLowerCase() // TODO: better name inference

    private var _context: Context? = null
    val context: Context
        get() {
            if (_context == null) parent?.let {
                _context = Context(it.context, this)
            }
            checkNotNull(_context) { "Context accessed before parse has been called." }
            return _context!!
        }

    internal var parent: CliktCommand? = null
    internal var subcommands: List<CliktCommand> = emptyList()
    internal val options: MutableList<Option> = mutableListOf()
    internal val arguments: MutableList<Argument<*>> = mutableListOf()

    private fun registeredOptionNames() = options.flatMapTo(HashSet()) { it.names }

    fun registerOption(option: Option) {
        val names = registeredOptionNames()
        for (name in option.names) {
            require(name !in names) { "Duplicate option name $name" }
        }
        options += option
    }

    fun registerArgument(argument: Argument<*>) {
        arguments += argument
    }

    private fun allHelpParams() = options.mapNotNull { it.parameterHelp } +
            arguments.mapNotNull { it.parameterHelp } +
            subcommands.map { it.helpAsSubcommand() }

    open fun getFormattedUsage(): String {
        return helpFormatter.formatUsage(allHelpParams(), programName = name)
    }

    open fun getFormattedHelp(): String {
        return helpFormatter.formatHelp(allHelpParams(), programName = name)
    }

    private fun helpAsSubcommand(): ParameterHelp {
        val shortHelp = help.split(".", "\n", limit=2).first().trim()
        return ParameterHelp(listOf(name), null, shortHelp,
                ParameterHelp.SECTION_SUBCOMMANDS, true, false)
    }

    fun parse(argv: Array<String>) {
        _context = Context(null, this)
        if (helpOptionNames.isNotEmpty()) {
            val names = helpOptionNames - registeredOptionNames()
            if (names.isNotEmpty()) options += helpOption(names, helpOptionMessage)
        }
        Parser.parse(argv, context)
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
            println(e.formatMessage(context))
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

fun <T : CliktCommand> T.subcommands(vararg commands: CliktCommand): T {
    subcommands += commands
    for (command in subcommands) command.parent = this
    return this
}
