package com.github.ajalt.clikt.v2

import com.github.ajalt.clikt.options.Context
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


inline fun <reified T : Any> CliktCommand.requireObject(): ReadOnlyProperty<CliktCommand, T> = FindObjectNonNull(T::class)
inline fun <reified T : Any> CliktCommand.findObject(): ReadOnlyProperty<CliktCommand, T?> = FindObjectNullable(T::class)
inline fun <reified T : Any> CliktCommand.findObject(noinline default: () -> T): ReadOnlyProperty<CliktCommand, T?> = FindObjectDefault(T::class, default)

@PublishedApi
internal class FindObjectNonNull<out T : Any>(private val clazz: KClass<T>) : ReadOnlyProperty<CliktCommand, T> {
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
        var ctx: Context2? = thisRef.context
        while (ctx != null) {
            @Suppress("UNCHECKED_CAST")
            if (clazz.isInstance(ctx.obj)) return ctx.obj as T
            ctx = ctx.parent
        }
        throw NullPointerException()
    }
}

@PublishedApi
internal class FindObjectNullable<out T : Any>(private val clazz: KClass<T>) : ReadOnlyProperty<CliktCommand, T?> {
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T? {
        var ctx: Context2? = thisRef.context
        while (ctx != null) {
            @Suppress("UNCHECKED_CAST")
            if (clazz.isInstance(ctx.obj)) return ctx.obj as T
            ctx = ctx.parent
        }
        return null
    }
}

@PublishedApi
internal class FindObjectDefault<out T : Any>(private val clazz: KClass<T>,
                                              private val default: () -> T) : ReadOnlyProperty<CliktCommand, T> {
    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): T {
        var ctx: Context2? = thisRef.context
        while (ctx != null) {
            @Suppress("UNCHECKED_CAST")
            if (clazz.isInstance(ctx.obj)) return ctx.obj as T
            ctx = ctx.parent
        }
        return default()
    }
}
