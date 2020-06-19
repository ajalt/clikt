package com.github.ajalt.clikt.output

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import platform.posix.stat

internal actual fun MemScope.getModificationTime(filename: String): Long {
    val stat = alloc<stat>()
    stat(filename, stat.ptr)
    return stat.st_mtimespec.tv_nsec
}
