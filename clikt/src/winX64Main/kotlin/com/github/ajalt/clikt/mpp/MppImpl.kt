package com.github.ajalt.clikt.mpp

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.getenv
import kotlin.system.exitProcess

private val ANSI_CODE_RE = Regex("${"\u001B"}\\[[^m]*m")

internal actual val String.graphemeLength: Int get() = replace(ANSI_CODE_RE, "").length

internal actual fun getSimpleClassName(it: Any): String = it::class.simpleName.orEmpty().split("$").last()

internal actual fun isLetterOrDigit(c: Char): Boolean = Regex("""[a-zA-Z0-9]""").matches(c.toString())

internal actual fun readEnvvar(key: String): String? = getenv(key)?.toKString()

internal actual fun isWindows(): Boolean = true

internal actual fun exitProcessMpp(status: Int): Nothing = exitProcess(status)

internal actual fun readFileIfExists(filename: String): String? {
    val file = fopen(filename, "r") ?: return null
    val chunks = StringBuilder()
    try {
        memScoped {
            val bufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(bufferLength)

           while(true){
                val chunk = fgets(buffer, bufferLength, file)?.toKString()
                if (chunk == null || chunk.isEmpty()) break
                chunks.append(chunk)
            }
        }
    } finally {
        fclose(file)
    }
    return chunks.toString()
}
