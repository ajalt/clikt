package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.mpp.readEnvvar

@JsName("Object")
external class JsObject : JsAny {
    operator fun get(key: JsString): JsAny?
    operator fun set(key: JsString, value: JsAny?)
}

// Can't use the wasm-interop definition because it only has get and set methods
@JsName("Array")
external class JsArray<T : JsAny?> : JsAny {
    fun push(value: T)
}


private external object Buffer {
    fun toString(encoding: String): String
}

private external interface FsStats {
    val mtimeMs: Double
}

private external interface FsModule {
    fun statSync(path: String): FsStats
    fun writeFileSync(path: String, data: String)
    fun readFileSync(path: String, encoding: String): String
    fun unlinkSync(path: String)
}

private external interface CryptoModule {
    fun randomBytes(size: Int): Buffer
}

private external interface ChildProcessModule {
    fun execSync(command: String, options: JsObject): Int
    fun spawnSync(command: String, args: JsArray<JsString>, options: JsAny?): JsObject
}

private fun importNodeFsModule(): FsModule =
    js("""require("fs")""")

private fun importNodeCryptoModule(): CryptoModule =
    js("""require("crypto")""")

private fun importNodeChildProcessModule(): ChildProcessModule =
    js("""require("child_process")""")

internal actual fun createEditor(
    editorPath: String?,
    env: Map<String, String>,
    requireSave: Boolean,
    extension: String,
): Editor {
    try {
        val fs = importNodeFsModule()
        val crypto = importNodeCryptoModule()
        val childProcess = importNodeChildProcessModule()
        return NodeJsEditor(fs, crypto, childProcess, editorPath, env, requireSave, extension)
    } catch (e: Exception) {
        throw IllegalStateException("Cannot edit files on this platform", e)
    }
}

private class NodeJsEditor(
    private val fs: FsModule,
    private val crypto: CryptoModule,
    private val childProcess: ChildProcessModule,
    private val editorPath: String?,
    private val env: Map<String, String>,
    private val requireSave: Boolean,
    private val extension: String,
) : Editor {
    private fun getEditorPath(): String {
        return editorPath ?: inferEditorPath { editor ->
            val options = jsObject(
                "timeout" to 100.toJsNumber(),
                "windowsHide" to true.toJsBoolean(),
                "stdio" to "ignore".toJsString(),
            )
            childProcess.execSync("${getWhichCommand()} $editor", options) == 0
        }
    }

    private fun getEditorCommand(): Array<String> {
        return getEditorPath().trim().split(" ").toTypedArray()
    }

    private fun editFileWithEditor(editorCmd: Array<String>, filename: String) {
        val cmd = editorCmd[0]
        val args = JsArray<JsString>()
        (editorCmd.drop(1) + filename).forEach { args.push(it.toJsString()) }
        val options = jsObject(
            "stdio" to "inherit".toJsString(),
            "env" to jsObject(*env.map { (k, v) -> k to v.toJsString() }.toTypedArray())
        )
        try {
            val exitCode = childProcess.spawnSync(cmd, args, options)
            if (exitCode["status".toJsString()]?.unsafeCast<JsNumber>()?.toInt() != 0) {
                throw CliktError("$cmd: Editing failed!")
            }
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

private fun jsObject(vararg pairs: Pair<String, JsAny?>): JsObject {
    val obj = JsObject()
    for ((k, v) in pairs) {
        obj[k.toJsString()] = v
    }
    return obj
}
