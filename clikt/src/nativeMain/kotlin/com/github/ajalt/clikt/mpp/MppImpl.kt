@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package com.github.ajalt.clikt.mpp

import kotlinx.cinterop.*
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import kotlin.experimental.ExperimentalNativeApi
import kotlin.system.exitProcess

internal actual fun isWindowsMpp(): Boolean = Platform.osFamily == OsFamily.WINDOWS

internal actual fun exitProcessMpp(status: Int): Unit = exitProcess(status)

internal actual fun readFileIfExists(filename: String): String? {
    val file = fopen(filename, "r") ?: return null
    val chunks = StringBuilder()
    try {
        memScoped {
            val bufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(bufferLength)

            while (true) {
                val chunk = fgets(buffer, bufferLength, file)?.toKString()
                if (chunk.isNullOrEmpty()) break
                chunks.append(chunk)
            }
        }
    } finally {
        fclose(file)
    }
    return chunks.toString()
}
