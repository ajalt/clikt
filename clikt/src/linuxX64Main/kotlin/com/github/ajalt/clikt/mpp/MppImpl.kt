@file:OptIn(ExperimentalForeignApi::class)

package com.github.ajalt.clikt.mpp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

internal actual fun readEnvvar(key: String): String? = getenv(key)?.toKString()
