package com.github.ajalt.clikt.mpp

internal val ANSI_CODE_RE = Regex("${"\u001B"}\\[[^m]*m")

internal expect val String.graphemeLengthMpp: Int

internal expect fun readEnvvar(key: String): String?

internal expect fun isWindowsMpp(): Boolean

internal expect fun exitProcessMpp(status: Int): Nothing

internal expect fun isLetterOrDigit(c: Char): Boolean

internal expect fun readFileIfExists(filename: String): String?

internal expect fun Any.mppClassSimpleName(): String
