package com.github.ajalt.clikt.v2

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


inline fun <reified T : Any> CliktCommand.requireObject(): ReadOnlyProperty<CliktCommand, T> {
    return object : ReadOnlyProperty<CliktCommand, T> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
            return thisRef.context.findObject<T>()!!
        }
    }
}

inline fun <reified T : Any> CliktCommand.findObject(): ReadOnlyProperty<CliktCommand, T?> {
    return object : ReadOnlyProperty<CliktCommand, T?> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T? {
            return thisRef.context.findObject<T>()
        }
    }
}

inline fun <reified T : Any> CliktCommand.findObject(crossinline default: () -> T): ReadOnlyProperty<CliktCommand, T?> {
    return object : ReadOnlyProperty<CliktCommand, T> {
        override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
            return thisRef.context.findObject(default)
        }
    }
}
