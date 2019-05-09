package com.github.ajalt.clikt.mpp

import java.text.BreakIterator
import kotlin.system.exitProcess

private val ANSI_CODE_RE = Regex("${"\u001B"}\\[[^m]*m")

internal actual val String.graphemeLength: Int
    get() {
        val breaks = BreakIterator.getCharacterInstance().also { it.setText(replace(ANSI_CODE_RE, "")) }
        return generateSequence { breaks.next() }.takeWhile { it != BreakIterator.DONE }.count()
    }

/** Read the value for for the envronment variable [key] */
internal actual fun readEnvvar(key: String): String? = System.getenv(key)

internal actual fun isWindows(): Boolean {
    return System.getProperty("os.name")
            .contains(Regex("windows", RegexOption.IGNORE_CASE))
}

internal actual class IOError : Throwable()

internal actual fun exitProcessMpp(status: Int): Nothing {
    exitProcess(status)
}

internal actual fun getSimpleClassName(it: Any): String {
    return it.javaClass.simpleName.split("$").last()
}

internal actual fun isLetterOrDigit(c: Char): Boolean {
    return c.isLetterOrDigit()
}
