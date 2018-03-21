package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Context(val parent: Context?,
              val command: CliktCommand,
              val allowInterspersedArgs: Boolean = true,
              val autoEnvvarPrefix: String? = null,
              val helpOptionNames: Set<String> = setOf("-h", "--help"),
              val helpOptionMessage: String = "Show this message and exit",
              val helpFormatter: HelpFormatter = PlaintextHelpFormatter(),
              val tokenTransformer: Context.(String) -> String = { it }) {
    var invokedSubcommand: CliktCommand? = null
        internal set
    var obj: Any? = null

    inline fun <reified T> findObject(): T? {
        var ctx: Context? = this
        while (ctx != null) {
            if (ctx.obj is T) return ctx.obj as T
            ctx = ctx.parent
        }
        return null
    }

    inline fun <reified T> findObject(defaultValue: () -> T): T {
        return findObject<T>() ?: defaultValue().also { obj = it }
    }

    fun findRoot(): Context {
        var ctx = this
        while (ctx.parent != null) {
            ctx = ctx.parent!!
        }
        return ctx
    }

    fun fail(message: String = ""): Nothing = throw UsageError(message)

    class Builder(private val command: CliktCommand,
                  private val parent: Context? = null) {
        var allowInterspersedArgs: Boolean = parent?.allowInterspersedArgs ?: true
        var helpOptionNames: Set<String> = parent?.helpOptionNames ?: setOf("-h", "--help")
        var helpOptionMessage: String = parent?.helpOptionMessage ?: "Show this message and exit"
        var helpFormatter: HelpFormatter = parent?.helpFormatter ?: PlaintextHelpFormatter()
        var tokenTransformer: Context.(String) -> String = parent?.tokenTransformer ?: { it }
        var autoEnvvarPrefix: String? = parent?.autoEnvvarPrefix?.let {
            it + "_" + command.name.replace(Regex("\\W"), "_").toUpperCase()
        }

        fun build(): Context {
            return Context(parent, command, allowInterspersedArgs, autoEnvvarPrefix,
                    helpOptionNames, helpOptionMessage, helpFormatter, tokenTransformer)
        }
    }

    companion object {
        inline fun build(command: CliktCommand, parent: Context? = null, block: Builder.() -> Unit): Context {
            return Builder(command, parent).run { block(); build() }
        }
    }
}

@Suppress("unused")
inline fun <reified T : Any> CliktCommand.requireObject(): ReadOnlyProperty<CliktCommand, T> {
    return object : ReadOnlyProperty<CliktCommand, T> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
            return thisRef.context.findObject<T>()!!
        }
    }
}

@Suppress("unused")
inline fun <reified T : Any> CliktCommand.findObject(): ReadOnlyProperty<CliktCommand, T?> {
    return object : ReadOnlyProperty<CliktCommand, T?> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T? {
            return thisRef.context.findObject<T>()
        }
    }
}

@Suppress("unused")
inline fun <reified T : Any> CliktCommand.findObject(crossinline default: () -> T): ReadOnlyProperty<CliktCommand, T?> {
    return object : ReadOnlyProperty<CliktCommand, T> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
            return thisRef.context.findObject(default)
        }
    }
}
