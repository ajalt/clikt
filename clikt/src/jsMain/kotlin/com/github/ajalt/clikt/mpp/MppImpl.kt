package com.github.ajalt.clikt.mpp

private external val process: dynamic
private external fun require(mod: String): dynamic

private val fs = require("fs")

private val LETTER_OR_DIGIT_RE = Regex("""[a-zA-Z0-9]""")

internal actual val String.graphemeLengthMpp: Int get() = replace(ANSI_CODE_RE, "").length

internal actual fun isLetterOrDigit(c: Char): Boolean = LETTER_OR_DIGIT_RE.matches(c.toString())

internal actual fun readEnvvar(key: String): String? = process.env[key] as? String

internal actual fun isWindowsMpp(): Boolean = process.platform == "win32"

internal actual fun exitProcessMpp(status: Int): Nothing = process.exit(status).unsafeCast<Nothing>()

internal actual fun readFileIfExists(filename: String): String? {
    return try {
        fs.readFileSync(filename, "utf-8") as? String
    } catch (e: Throwable) {
        null
    }
}
