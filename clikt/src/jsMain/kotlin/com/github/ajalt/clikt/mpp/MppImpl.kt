package com.github.ajalt.clikt.mpp

private external val process: dynamic

private interface JsMppImpls {
    fun readEnvvar(key: String): String?
    fun isWindowsMpp(): Boolean
    fun exitProcessMpp(status: Int)
    fun readFileIfExists(filename: String): String?
}

private object BrowserMppImpls : JsMppImpls {
    override fun readEnvvar(key: String): String? = null
    override fun isWindowsMpp(): Boolean = false
    override fun exitProcessMpp(status: Int) {}
    override fun readFileIfExists(filename: String): String? = null
}

private class NodeMppImpls(private val fs: dynamic) : JsMppImpls {
    override fun readEnvvar(key: String): String? = process.env[key] as? String
    override fun isWindowsMpp(): Boolean = process.platform == "win32"
    override fun exitProcessMpp(status: Int): Unit = process.exit(status).unsafeCast<Unit>()
    override fun readFileIfExists(filename: String): String? {
        return try {
            fs.readFileSync(filename, "utf-8") as? String
        } catch (e: Throwable) {
            null
        }
    }
}

private val impls: JsMppImpls = try {
    NodeMppImpls(nodeRequire("fs"))
} catch (e: Exception) {
    BrowserMppImpls
}

private val LETTER_OR_DIGIT_RE = Regex("""[a-zA-Z0-9]""")

internal actual val String.graphemeLengthMpp: Int get() = replace(ANSI_CODE_RE, "").length

internal actual fun isLetterOrDigit(c: Char): Boolean = LETTER_OR_DIGIT_RE.matches(c.toString())

internal actual fun readEnvvar(key: String): String? = impls.readEnvvar(key)
internal actual fun isWindowsMpp(): Boolean = impls.isWindowsMpp()
internal actual fun exitProcessMpp(status: Int): Unit = impls.exitProcessMpp(status)
internal actual fun readFileIfExists(filename: String): String? = impls.readFileIfExists(filename)
