package com.github.ajalt.clikt.mpp

import java.io.File
import kotlin.system.exitProcess

internal actual fun isWindowsMpp(): Boolean = System.getProperty("os.name")
    .contains(Regex("windows", RegexOption.IGNORE_CASE))

internal actual fun exitProcessMpp(status: Int) {
    exitProcess(status)
}

internal actual fun readFileIfExists(filename: String): String? {
    val file = File(filename)
    if (!file.isFile) return null
    return file.bufferedReader().use { it.readText() }
}

internal actual fun readEnvvar(key: String): String? = System.getenv(key)
