package com.github.ajalt.clikt.mpp

internal expect fun readEnvvar(key: String): String?

internal expect fun isWindowsMpp(): Boolean

/** Doesn't return [Nothing], since it's a no-op on the browser */
internal expect fun exitProcessMpp(status: Int)

internal expect fun readFileIfExists(filename: String): String?
