package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktError
import java.io.IOException
import java.util.concurrent.TimeUnit


internal class Editor(private val editorPath: String?,
                      private val env: Map<String, String>,
                      private val requireSave: Boolean,
                      private val extension: String) {
    private fun getEditorPath(): String {
        if (editorPath != null) return editorPath
        for (key in arrayOf("VISUAL", "EDITOR")) {
            return System.getenv(key) ?: continue
        }

        if (TermUi.isWindows) return "notepad"

        for (editor in arrayOf("vim", "nano")) {
            try {
                val process = ProcessBuilder("which", editor).start()
                if (process.waitFor(250, TimeUnit.MILLISECONDS) &&
                        process.exitValue() == 0) {
                    return editor
                }
            } catch (err: Exception) {
                when (err) {
                    is IOException, is SecurityException, is InterruptedException,
                    is IllegalThreadStateException -> Unit
                    else -> throw CliktError("Error staring editor", err)
                }
            }
        }

        return "vi"
    }

    fun editFile(filename: String) {
        val editor = getEditorPath()
        try {
            val process = ProcessBuilder(editor, filename).apply {
                environment() += env
            }.start()
            val exitCode = process.waitFor()
            if (exitCode != 0) throw CliktError("$editor: Editing failed!")
        } catch (err: Exception) {
            when (err) {
                is CliktError -> throw err
                else -> throw CliktError("Error staring editor", err)
            }
        }
    }

    fun edit(text: String): String? {
        var textToEdit = if (text.endsWith("\n")) text else text + "\n"
        if (TermUi.isWindows) {
            textToEdit = textToEdit.replace(Regex("(?<!\r)\n"), "\r\n")
        }
        val file = createTempFile(suffix = extension)
        try {
            file.writeText(textToEdit)


            val ts = file.lastModified()
            editFile(file.canonicalPath)

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
