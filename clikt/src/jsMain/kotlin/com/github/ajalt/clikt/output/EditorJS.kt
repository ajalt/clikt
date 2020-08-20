package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.mpp.nodeRequire
import com.github.ajalt.clikt.mpp.readEnvvar

internal actual fun createEditor(
        editorPath: String?,
        env: Map<String, String>,
        requireSave: Boolean,
        extension: String
): Editor {
    try {
        val fs = nodeRequire("fs")
        val crypto = nodeRequire("crypto")
        val childProcess = nodeRequire("child_process")
        return NodeJsEditor(fs, crypto, childProcess, editorPath, env, requireSave, extension)
    } catch (e: Exception) {
        throw IllegalStateException("Cannot edit files on this platform", e)
    }
}

private class NodeJsEditor(
        private val fs: dynamic,
        private val crypto: dynamic,
        private val childProcess: dynamic,
        private val editorPath: String?,
        private val env: Map<String, String>,
        private val requireSave: Boolean,
        private val extension: String
) : Editor {
    private fun getEditorPath(): String {
        return editorPath ?: inferEditorPath { editor ->
            val options = jsObject(
                    "timeout" to 100,
                    "windowsHide" to true,
                    "stdio" to "ignore"
            )
            childProcess.execSync("${getWhichCommand()} $editor", options) == 0
        }
    }

    private fun getEditorCommand(): Array<String> {
        return getEditorPath().trim().split(" ").toTypedArray()
    }

    private fun editFileWithEditor(editorCmd: Array<String>, filename: String) {
        val cmd = editorCmd[0]
        val args = (editorCmd.drop(1) + filename).toTypedArray()
        val options = jsObject("stdio" to "inherit", "env" to env.toJsObject())
        try {
            val exitCode = childProcess.spawnSync(cmd, args, options)
            if (exitCode.status != 0) throw CliktError("$cmd: Editing failed!")
        } catch (err: CliktError) {
            throw err
        } catch (err: Throwable) {
            throw CliktError("Error staring editor")
        }
    }

    override fun editFile(filename: String) {
        editFileWithEditor(getEditorCommand(), filename)
    }

    private fun getTmpFileName(extension: String): String {
        val rand = crypto.randomBytes(8).toString("hex")
        val dir = readEnvvar("TMP") ?: "."
        return "$dir/clikt_tmp_$rand.${extension.trimStart { it == '.' }}"
    }

    private fun getLastModified(path: String): Int {
        return (fs.statSync(path).mtimeMs as Number).toInt()
    }

    override fun edit(text: String): String? {
        val editorCmd = getEditorCommand()
        val textToEdit = normalizeEditorText(editorCmd[0], text)
        val tmpFilename = getTmpFileName(extension)
        try {
            fs.writeFileSync(tmpFilename, textToEdit)
            try {
                val lastModified = getLastModified(tmpFilename)
                editFileWithEditor(editorCmd, tmpFilename)
                if (requireSave && getLastModified(tmpFilename) == lastModified) {
                    return null
                }
                val readFileSync = fs.readFileSync(tmpFilename, "utf8")
                return (readFileSync as String?)?.replace("\r\n", "\n")
            } finally {
                try {
                    fs.unlinkSync(tmpFilename)
                } catch (ignored: Throwable) {
                }
            }
        } catch (e: Throwable) {
            throw CliktError("Error staring editing text: ${e.message}")
        }
    }
}

private fun <K, V> Map<K, V>.toJsObject(): dynamic {
    val result = js("{}")
    for ((key, value) in this) {
        result[key] = value
    }
    return result
}

private fun jsObject(vararg pairs: Pair<Any, Any>): dynamic {
    return pairs.toMap().toJsObject()
}
