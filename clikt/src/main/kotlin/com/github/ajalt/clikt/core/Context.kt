package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.output.PlaintextHelpFormatter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Context(val parent: Context?, val command: CliktCommand,
              val allowInterspersedArgs: Boolean = true,
              val helpOptionNames: Set<String> = setOf("-h", "--help"),
              val helpOptionMessage: String = "Show this message and exit",
              val helpFormatter: HelpFormatter = PlaintextHelpFormatter()) {
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

    class Builder(val parent: Context? = null) {
        var allowInterspersedArgs: Boolean = parent?.allowInterspersedArgs ?: true
        var helpOptionNames: Set<String> = parent?.helpOptionNames ?: setOf("-h", "--help")
        var helpOptionMessage: String = parent?.helpOptionMessage ?: "Show this message and exit"
        var helpFormatter: HelpFormatter = parent?.helpFormatter ?: PlaintextHelpFormatter()

        fun build(command: CliktCommand): Context {
            return Context(parent, command, allowInterspersedArgs,
                    helpOptionNames, helpOptionMessage, helpFormatter)
        }
    }

    companion object {
        inline fun build(command: CliktCommand, parent: Context? = null, block: Builder.() -> Unit): Context {
            return Builder(parent).run { block(); build(command) }
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
