package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.mpp.exitProcessMpp

object CliktUtil {
    /**
     * Exit the current process with the given status code.
     *
     * On browsers, where it's not possible to exit the process, this is a no-op.
     */
    fun exitProcess(status: Int) {
        exitProcessMpp(status)
    }
}
