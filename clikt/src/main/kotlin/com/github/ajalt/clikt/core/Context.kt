package com.github.ajalt.clikt.core

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class Context(val parent: Context?, val command: CliktCommand) {
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
