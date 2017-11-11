package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.Command


open class Context(parent: Context?, val command: Command, var obj: Any?) {
    var parent: Context? = parent
        internal set

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
