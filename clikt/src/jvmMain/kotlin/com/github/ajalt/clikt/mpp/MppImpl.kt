package com.github.ajalt.clikt.mpp

import java.io.File
import java.text.BreakIterator
import kotlin.system.exitProcess

internal actual fun isWindowsMpp(): Boolean = System.getProperty("os.name")
        .contains(Regex("windows", RegexOption.IGNORE_CASE))

internal actual val String.graphemeLengthMpp: Int
    get() {
        val breaks = BreakIterator.getCharacterInstance().also { it.setText(replace(ANSI_CODE_RE, "")) }
        return generateSequence { breaks.next() }.takeWhile { it != BreakIterator.DONE }.count()
    }

internal actual fun exitProcessMpp(status: Int) {
    exitProcess(status)
}

internal actual fun isLetterOrDigit(c: Char): Boolean {
    return c.isLetterOrDigit()
}

internal actual fun readFileIfExists(filename: String): String? {
    val file = File(filename)
    if (!file.isFile) return null
    return file.bufferedReader().use { it.readText() }
}

internal actual fun readEnvvar(key: String): String? = System.getenv(key)
