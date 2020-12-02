package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktError
import java.io.IOException
import java.util.concurrent.TimeUnit

internal actual fun createEditor(
        editorPath: String?,
        env: Map<String, String>,
        requireSave: Boolean,
        extension: String
): Editor = JvmEditor(editorPath, env, requireSave, extension)

private class JvmEditor(
        private val editorPath: String?,
        private val env: Map<String, String>,
        private val requireSave: Boolean,
        private val extension: String
) : Editor {
    private fun getEditorPath(): String {
        return editorPath ?: inferEditorPath { editor ->
            try {
                val process = ProcessBuilder(getWhichCommand(), editor).start()
                process.waitFor(100, TimeUnit.MILLISECONDS) && process.exitValue() == 0
            } catch (err: Exception) {
                when (err) {
                    is IOException, is SecurityException, is InterruptedException,
                    is IllegalThreadStateException -> false
                    else -> throw CliktError("Error staring editor", err)
                }
            }
        }
    }


    private fun getEditorCommand(): Array<String> {
        return getEditorPath().trim().split(" ").toTypedArray()
    }

    private fun editFileWithEditor(editorCmd: Array<String>, filename: String) {
        try {
            val process = ProcessBuilder(*editorCmd, filename).apply {
                environment() += env
                inheritIO()
            }.start()
            val exitCode = process.waitFor()
            if (exitCode != 0) throw CliktError("${editorCmd[0]}: Editing failed!")
        } catch (err: Exception) {
            when (err) {
                is CliktError -> throw err
                else -> throw CliktError("Error staring editor", err)
            }
        }
    }

    override fun editFile(filename: String) {
        editFileWithEditor(getEditorCommand(), filename)
    }

    @Suppress("DEPRECATION") // The replacement is experimental
    override fun edit(text: String): String? {
        val editorCmd = getEditorCommand()
        val textToEdit = normalizeEditorText(editorCmd[0], text)
        val file = createTempFile(suffix = extension)
        try {
            file.writeText(textToEdit)
            val ts = file.lastModified()
            editFileWithEditor(editorCmd, file.canonicalPath)

            if (requireSave && file.lastModified() == ts) {
                return null
            }

            return file.readText().replace("\r\n", "\n")
        } catch (err: Exception) {
            throw CliktError("Error staring editor", err)
        } finally {
            file.delete()
        }
    }
}
