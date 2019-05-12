package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.defaultCliktConsole
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A object used to control command line parsing and pass data between commands.
 *
 * A new Context instance is created for each command each time the command line is parsed.
 *
 * @property parent If this context is the child of another command, [parent] is the parent command's context.
 * @property command The command that this context associated with.
 * @property allowInterspersedArgs If false, options and arguments cannot be mixed; the first time an argument is
 *   encountered, all remaining tokens are parsed as arguments.
 * @property autoEnvvarPrefix The prefix to add to inferred envvar names. If null, the prefix is based on the
 *   parent's prefix, if there is one. If no command specifies, a prefix, envvar lookup is disabled.
 * @property printExtraMessages Set this to false to prevent extra messages from being printed automatically.
 *   You can still access them at [CliktCommand.messages] inside of [CliktCommand.run].
 * @property helpOptionNames The names to use for the help option. If any names in the set conflict with other
 *   options, the conflicting name will not be used for the help option. If the set is empty, or contains no
 *   unique names, no help option will be added.
 * @property helpOptionMessage The description of the help option.
 * @property helpFormatter The help formatter for this command.
 * @property tokenTransformer An optional transformation function that is called to transform command line
 *   tokens (options and commands) before parsing. This can be used to implement e.g. case insensitive
 *   behavior.
 * @property console The console to use to print messages.
 */
class Context(
        val parent: Context?,
        val command: CliktCommand,
        val allowInterspersedArgs: Boolean,
        val autoEnvvarPrefix: String?,
        val printExtraMessages: Boolean,
        val helpOptionNames: Set<String>,
        val helpOptionMessage: String,
        val helpFormatter: HelpFormatter,
        val tokenTransformer: Context.(String) -> String,
        val console: CliktConsole
) {
    var invokedSubcommand: CliktCommand? = null
        internal set
    var obj: Any? = null

    /** Find the closest object of type [T] */
    inline fun <reified T> findObject(): T? {
        var ctx: Context? = this
        while (ctx != null) {
            if (ctx.obj is T) return ctx.obj as T
            ctx = ctx.parent
        }
        return null
    }

    /** Find the closest object of type [T], setting `this.`[obj] if one is not found. */
    inline fun <reified T> findObject(defaultValue: () -> T): T {
        return findObject<T>() ?: defaultValue().also { obj = it }
    }

    /** Find the outermost context */
    fun findRoot(): Context {
        var ctx = this
        while (ctx.parent != null) {
            ctx = ctx.parent!!
        }
        return ctx
    }

    /** Throw a [UsageError] with the given message */
    fun fail(message: String = ""): Nothing = throw UsageError(message)

    class Builder(command: CliktCommand,
                  parent: Context? = null) {
        /**
         * If false, options and arguments cannot be mixed; the first time an argument is encountered, all
         * remaining tokens are parsed as arguments.
         */
        var allowInterspersedArgs: Boolean = parent?.allowInterspersedArgs ?: true
        /**
         * Set this to false to prevent extra messages from being printed automatically.
         *
         * You can still access them at [CliktCommand.messages] inside of [CliktCommand.run].
         */
        var printExtraMessages: Boolean = parent?.printExtraMessages ?: true
        /**
         * The names to use for the help option.
         *
         * If any names in the set conflict with other options, the conflicting name will not be used for the
         * help option. If the set is empty, or contains no unique names, no help option will be added.
         */
        var helpOptionNames: Set<String> = parent?.helpOptionNames ?: setOf("-h", "--help")
        /** The description of the help option.*/
        var helpOptionMessage: String = parent?.helpOptionMessage ?: "Show this message and exit"
        /** The help formatter for this command*/
        var helpFormatter: HelpFormatter = parent?.helpFormatter ?: CliktHelpFormatter()
        /** An optional transformation function that is called to transform command line */
        var tokenTransformer: Context.(String) -> String = parent?.tokenTransformer ?: { it }
        /**
         * The prefix to add to inferred envvar names.
         *
         * If null, the prefix is based on the parent's prefix, if there is one. If no command specifies, a
         * prefix, envvar lookup is disabled.
         */
        var autoEnvvarPrefix: String? = parent?.autoEnvvarPrefix?.let {
            it + "_" + command.commandName.replace(Regex("\\W"), "_").toUpperCase()
        }

        /**
         * The console that will handle reading and writing text.
         *
         * The default uses [System. in] and [System.out].
         */
        var console: CliktConsole = parent?.console ?: defaultCliktConsole()
    }

    companion object {
        inline fun build(command: CliktCommand, parent: Context? = null, block: Builder.() -> Unit): Context {
            with(Builder(command, parent)) {
                block()
                return Context(parent, command, allowInterspersedArgs, autoEnvvarPrefix, printExtraMessages,
                        helpOptionNames, helpOptionMessage, helpFormatter, tokenTransformer, console)
            }
        }
    }
}

/** Find the closest object of type [T], or throw a [NullPointerException] */
@Suppress("unused")
inline fun <reified T : Any> CliktCommand.requireObject(): ReadOnlyProperty<CliktCommand, T> {
    return object : ReadOnlyProperty<CliktCommand, T> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
            return thisRef.context.findObject<T>()!!
        }
    }
}

/** Find the closest object of type [T], or null */
@Suppress("unused")
inline fun <reified T : Any> CliktCommand.findObject(): ReadOnlyProperty<CliktCommand, T?> {
    return object : ReadOnlyProperty<CliktCommand, T?> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T? {
            return thisRef.context.findObject<T>()
        }
    }
}

/** Find the closest object of type [T], setting `context.obj` if one is not found. */
@Suppress("unused")
inline fun <reified T : Any> CliktCommand.findObject(crossinline default: () -> T): ReadOnlyProperty<CliktCommand, T> {
    return object : ReadOnlyProperty<CliktCommand, T> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
            return thisRef.context.findObject(default)
        }
    }
}
