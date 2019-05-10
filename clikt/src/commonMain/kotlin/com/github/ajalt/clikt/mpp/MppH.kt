package com.github.ajalt.clikt.mpp

internal expect val String.graphemeLength: Int

internal expect fun readEnvvar(key: String): String?

internal expect fun isWindows(): Boolean

internal expect fun exitProcessMpp(status: Int): Nothing

internal expect fun getSimpleClassName(it: Any): String

internal expect fun isLetterOrDigit(c: Char) : Boolean

internal expect fun readFileIfExists(filename: String): String?
