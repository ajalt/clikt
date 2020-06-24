package com.github.ajalt.clikt.testing

fun splitArgv(argv: String): Array<String> {
    return if (argv.isBlank()) emptyArray() else argv.split(" ").toTypedArray()
}

/**
 * As of Kotlin 1.3.61, Kotlin/JS generates incorrect code for some uses of `@JsName`.
 * https://youtrack.jetbrains.com/issue/KT-33294
 *
 * This manifests in runtime access of undefined functions in tests that call `validate`. So we skip
 * those tests on JS for now.
 */
expect val skipDueToKT33294: Boolean
