package com.github.ajalt.clikt.v2

open class Context2(val parent: Context2?, val command: CliktCommand) {
    var obj: Any? = null

    inline fun <reified T> findObject(): T? {
        var ctx: Context2? = this
        while (ctx != null) {
            if (ctx.obj is T) return ctx.obj as T
            ctx = ctx.parent
        }
        return null
    }

    inline fun <reified T> findObject(defaultValue: () -> T): T {
        return findObject<T>() ?: defaultValue().also { obj = it }
    }

    fun findRoot(): Context2 {
        var ctx = this
        while (ctx.parent != null) {
            ctx = ctx.parent!!
        }
        return ctx
    }
}
