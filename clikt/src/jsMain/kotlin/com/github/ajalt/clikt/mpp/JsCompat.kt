package com.github.ajalt.clikt.mpp


// https://github.com/iliakan/detect-node
private val isNode: Boolean = js(
        "Object.prototype.toString.call(typeof process !== 'undefined' ? process : 0) === '[object process]'"
) as Boolean

/** Load module [mod], or throw an exception if not running on NodeJS */
internal fun nodeRequire(mod: String): dynamic {
    require(isNode) { "Module not available: $mod" }

    // This hack exists to silence webpack warnings when running on the browser. `require` is a
    // built-in function on Node, and doesn't exist on browsers. Webpack will statically look for
    // calls to `require` and rewrite them into its own module loading system. This means that when
    // we have `require("fs")` in our code, webpack will complain during compilation with two types
    // of warnings:
    //
    // 1. It will warn about the module not existing (since it's node-only), even if we never
    //    execute that statement at runtime on the browser.
    // 2. It will complain with a different warning if the argument to `require` isn't static
    //    (e.g. `fun myRequire(m:String) { require(m) }`).
    //
    // If we do run `require("fs")` on the browser, webpack will normally cause it to throw a
    // `METHOD_NOT_FOUND` error. If the user marks `fs` as "external" in their webpack
    // configuration, it will silence the first type of warning above, and the `require` call
    // will now return `undefined` instead of throwing an exception.
    //
    // So since we never call `require` at runtime on browsers anyway, we hide our `require`
    // calls from webpack by loading the method dynamically. This prevents any warnings, and
    // doesn't require users to add anything to their webpack config.

    val imported = try {
        js("module['' + 'require']")(mod)
    } catch (e: dynamic) {
        throw IllegalArgumentException("Module not available: $mod", e as? Throwable)
    }
    require(
            imported != null && js("typeof imported !== 'undefined'").unsafeCast<Boolean>()
    ) { "Module not available: $mod" }
    return imported
}
