package com.github.ajalt.clikt.mpp


private external interface FsModule {
    fun readFileSync(path: String, encoding: String): String
}

@Suppress("ClassName")
private external object process {
    val platform: String
    fun exit(status: Int)
}


@Suppress("RedundantNullableReturnType") // invalid diagnostic due to KTIJ-28239
private fun nodeReadEnvvar(@Suppress("UNUSED_PARAMETER") key: String): String? =
    js("process.env[key]")

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

private class NodeMppImpls(private val fs: FsModule) : JsMppImpls {
    override fun readEnvvar(key: String): String? = nodeReadEnvvar(key)
    override fun isWindowsMpp(): Boolean = process.platform == "win32"
    override fun exitProcessMpp(status: Int): Unit = process.exit(status)
    override fun readFileIfExists(filename: String): String? {
        return try {
            fs.readFileSync(filename, "utf-8")
        } catch (e: Throwable) {
            null
        }
    }
}

// See jsMain/MppImpl.kt for the details of node detection
private fun runningOnNode(): Boolean =
    js("Object.prototype.toString.call(typeof process !== 'undefined' ? process : 0) === '[object process]'")


private fun importNodeFsModule(): FsModule =
    js("""require("fs")""")

private val impls: JsMppImpls = when {
    runningOnNode() -> NodeMppImpls(importNodeFsModule())
    else -> BrowserMppImpls
}

internal actual fun readEnvvar(key: String): String? = impls.readEnvvar(key)
internal actual fun isWindowsMpp(): Boolean = impls.isWindowsMpp()
internal actual fun exitProcessMpp(status: Int): Unit = impls.exitProcessMpp(status)
internal actual fun readFileIfExists(filename: String): String? = impls.readFileIfExists(filename)
