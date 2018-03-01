package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.parser.*
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import kotlin.system.exitProcess

// TODO: better help arguments
abstract class CliktCommand(
        val help: String? = null,
        val version: String? = null,  // TODO use this
        name: String? = null,
        private val helpFormatter: HelpFormatter = PlaintextHelpFormatter(help ?: "", ""),
        val allowInterspersedArgs: Boolean = true) {
    val name = name ?: javaClass.simpleName.toLowerCase() // TODO: better name inference

    private var _context: Context2? = null
    val context: Context2
        get() {
            if (_context == null) parent?.let {
                _context = Context2(it.context, this)
            }
            checkNotNull(_context) { "Context accessed before parse has been called." }
            return _context!!
        }

    private var parent: CliktCommand? = null

    internal var subcommands: List<CliktCommand> = emptyList()
    internal val options: MutableList<Option> = mutableListOf(helpOption()) // TODO finish help option features
    internal val arguments: MutableList<Argument<*>> = mutableListOf()

    fun subcommands(vararg commands: CliktCommand): CliktCommand {
        subcommands += commands
        for (command in subcommands) command.parent = this
        return this
    }

    fun registerOption(option: Option) {
        options += option
    }

    fun registerArgument(argument: Argument<*>) {
        arguments += argument
    }

    private fun allHelpParams() = options.mapNotNull { it.parameterHelp } +
            arguments.mapNotNull { it.parameterHelp } +
            subcommands.map { it.helpAsSubcommand() }

    fun getFormattedUsage(): String {
        return helpFormatter.formatUsage(allHelpParams(), programName = name)
    }

    fun getFormattedHelp(): String {
        return helpFormatter.formatHelp(allHelpParams(), programName = name)
    }

    private fun helpAsSubcommand() = ParameterHelp(listOf(name), null, help ?: "",
            ParameterHelp.SECTION_SUBCOMMANDS, true, false) // TODO shortHelp, optional subcommands

    fun parse(argv: Array<String>) {
        _context = Context2(null, this)
        Parser2.parse(argv, context)
    }

    fun main(argv: Array<String>) {
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
