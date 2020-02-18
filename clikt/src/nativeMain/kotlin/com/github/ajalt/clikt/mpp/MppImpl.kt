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

private val LETTER_OR_DIGIT_RE = Regex("""[a-zA-Z0-9]""")

internal actual val String.graphemeLengthMpp: Int get() = replace(ANSI_CODE_RE, "").length

internal actual fun isLetterOrDigit(c: Char): Boolean = LETTER_OR_DIGIT_RE.matches(c.toString())

internal actual fun readEnvvar(key: String): String? = getenv(key)?.toKString()

internal actual fun isWindowsMpp(): Boolean = Platform.osFamily == OsFamily.WINDOWS

internal actual fun exitProcessMpp(status: Int): Nothing = exitProcess(status)

internal actual fun readFileIfExists(filename: String): String? {
    val file = fopen(filename, "r") ?: return null
    val chunks = StringBuilder()
    try {
        memScoped {
            val bufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(bufferLength)

            while (true) {
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

internal actual fun Any.mppClassSimpleName(): String = this::class.simpleName.orEmpty().split("$").last().toLowerCase()
