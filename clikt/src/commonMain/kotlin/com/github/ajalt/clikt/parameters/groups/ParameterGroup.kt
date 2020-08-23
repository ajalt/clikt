package com.github.ajalt.clikt.parameters.groups

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.GroupableOption
import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ParameterGroup {
    /**
     * The name of the group, or null if parameters in the group should not be separated from other
     * parameters in the help output.
     */
    val groupName: String?

    /**
     * A help message to display for this group.
     *
     * If [groupName] is null, the help formatter will ignore this value.
     */
    val groupHelp: String?

    fun parameterHelp(context: Context): HelpFormatter.ParameterHelp.Group? {
        val n = groupName
        val h = groupHelp
        return if (n == null || h == null) null else HelpFormatter.ParameterHelp.Group(n, h)
    }

    /**
     * Called after this command's argv is parsed and all options are validated to validate the group constraints.
     *
     * @param context The context for this parse
     * @param invocationsByOption The invocations of options in this group.
     */
    fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>)

    /**
     * Called after all of a command's parameters have been [finalize]d to perform validation of the final values.
     */
    fun postValidate(context: Context)
}

interface ParameterGroupDelegate<out T> : ParameterGroup, ReadOnlyProperty<CliktCommand, T> {
    /** Implementations must call [CliktCommand.registerOptionGroup] */
    operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T>
}

/**
 * A group of options that can be shown together in help output, or restricted to be [cooccurring].
 *
 * Declare a subclass with option delegate properties, then use an instance of your subclass is a
 * delegate property in your command with [provideDelegate].
 *
 * ### Example:
 *
 * ```
 * class UserOptions : OptionGroup(name = "User Options", help = "Options controlling the user") {
 *   val name by option()
 *   val age by option().int()
 * }
 *
 * class Tool : CliktCommand() {
 *   val userOptions by UserOptions()
 * }
 * ```
 */
open class OptionGroup(
        name: String? = null,
        help: String? = null
) : ParameterGroup, ParameterHolder {
    internal val options: MutableList<GroupableOption> = mutableListOf()
    override val groupName: String? = name
    override val groupHelp: String? = help

    override fun registerOption(option: GroupableOption) {
        option.parameterGroup = this
        options += option
    }

    override fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>) {
        for ((option, invocations) in invocationsByOption) {
            check(option in options) { "Internal Clikt Error: finalizing unregistered option [${option.names}]" }
            option.finalize(context, invocations)
        }

        // Finalize options not provided on the command line so that they can apply default values etc.
        options.forEach { o -> if (o !in invocationsByOption) o.finalize(context, emptyList()) }
    }

    override fun postValidate(context: Context) = options.forEach { it.postValidate(context) }
}

operator fun <T : OptionGroup> T.provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, T> {
    thisRef.registerOptionGroup(this)
    options.forEach { thisRef.registerOption(it) }
    return object : ReadOnlyProperty<CliktCommand, T> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T = this@provideDelegate
    }
}
