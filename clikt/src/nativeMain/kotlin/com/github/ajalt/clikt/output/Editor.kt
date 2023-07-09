@file:OptIn(ExperimentalForeignApi::class)

package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.mpp.isWindowsMpp
import com.github.ajalt.clikt.mpp.readEnvvar
import com.github.ajalt.clikt.mpp.readFileIfExists
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.*

internal actual fun createEditor(
    editorPath: String?,
    env: Map<String, String>,
    requireSave: Boolean,
    extension: String,
): Editor = NativeEditor(editorPath, env, requireSave, extension)

private class NativeEditor(
    private val editorPath: String?,
    private val env: Map<String, String>,
    private val requireSave: Boolean,
    private val extension: String,
) : Editor {
    private fun getEditorPath(): String {
        val nul = if (isWindowsMpp()) "nul" else "/dev/null"
        return editorPath ?: inferEditorPath { editor ->
            system("${getWhichCommand()} $editor >$nul 2>$nul") == 0
        }
    }

    override fun editFile(filename: String) {
        editFileWithEditor(getEditorPath(), filename)
    }

    private fun editFileWithEditor(editorCmd: String, filename: String) {
        val exitCode = system("$editorCmd $filename")
        if (exitCode != 0) throw CliktError("${editorCmd.takeWhile { !it.isWhitespace() }}: Editing failed!")
    }

    override fun edit(text: String): String? = memScoped {
        var filename = "${
            tmpnam(null)!!.toKString().trimEnd('.').replace("\\", "/")
        }.${extension.trimStart('.')}"

        // workaround for minGW bug that tries to create temp files in the root directory
        // https://sourceforge.net/p/mingw-w64/bugs/555/
        if (filename.startsWith("/")) filename = (readEnvvar("TMP") ?: ".") + filename

        val file =
            fopen(filename, "w") ?: throw CliktError("Error creating temporary file (errno=$errno)")
        try {
            val editorCmd = getEditorPath()
            fputs(normalizeEditorText(editorCmd, text), file)
            fclose(file)

            val lastModified = getModificationTime(filename)

            editFileWithEditor(editorCmd, filename)

            if (requireSave && getModificationTime(filename) == lastModified) {
                return null
            }

            return readFileIfExists(filename)?.replace("\r\n", "\n")
                ?: throw CliktError("Could not read file")
        } finally {
            remove(filename)
        }
    }
}

internal expect fun MemScope.getModificationTime(filename: String): Long
