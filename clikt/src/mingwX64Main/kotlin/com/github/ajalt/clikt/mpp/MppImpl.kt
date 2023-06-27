package com.github.ajalt.clikt.mpp

import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf16
import platform.windows.GetEnvironmentVariableW
import platform.windows.LPWSTR

// value from https://docs.microsoft.com/en-us/windows/win32/api/processenv/nf-processenv-getenvironmentvariablew
private const val MAX_ENVVAR_SIZE = 32768u

/**
 * Windows has whopping 6 different functions to read environment variables:
 * - GetEnvironmentVariableA, GetEnvironmentVariableW, getenv, _wgetenv, getenv_s, _wgetenv_s
 *
 * As discussed in [https://github.com/curl/curl/issues/4774], environment variables set via SetEnvironmentVariable are
 * visible to GetEnvironmentVariable but not to getenv. So we don't want to use any of the getenv variants.
 *
 * Additionally, we don't want to use GetEnvironmentVariableA, because it doesn't support unicode.
 *
 * The code in this function is a port of the example shown in
 * [https://docs.microsoft.com/en-us/windows/win32/procthread/changing-environment-variables] with the realloc loop
 * inspired by [https://github.com/curl/curl/pull/4863/files]
 */
internal actual fun readEnvvar(key: String): String? {
    var bufSize = MAX_ENVVAR_SIZE
    while (true) {
        memScoped {
            val buffer: LPWSTR = allocArray(bufSize.toLong())
            val readSize = GetEnvironmentVariableW(key, buffer, bufSize)
            when {
                // We don't bother checking GetLastError() since ENVVAR_NOT_FOUND is the only documented error value.
                readSize == 0u -> return null // envvar doesn't exist
                readSize > bufSize -> bufSize = readSize // buffer too small, realloc
                else -> return buffer.toKStringFromUtf16() // success
            }
        }
    }
}
