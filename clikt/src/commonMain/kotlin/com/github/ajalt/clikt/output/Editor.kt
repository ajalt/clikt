package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.mpp.isWindowsMpp
import com.github.ajalt.clikt.mpp.readEnvvar

internal interface Editor {
    fun editFile(filename: String)
    fun edit(text: String): String?
}

internal expect fun createEditor(
        editorPath: String?,
        env: Map<String, String>,
        requireSave: Boolean,
        extension: String
): Editor

internal fun inferEditorPath(commandExists: (String) -> Boolean): String {
    for (key in arrayOf("VISUAL", "EDITOR")) {
        return readEnvvar(key) ?: continue
    }

    val editors = when {
        TermUi.isWindows -> arrayOf("vim", "nano", "notepad")
        else -> arrayOf("vim", "nano")
    }

    for (editor in editors) {
        if (commandExists(editor)) return editor
    }

    return if (TermUi.isWindows) "notepad" else "vi"
}

internal fun normalizeEditorText(editor: String, text: String): String {
    return when (editor) {
        "notepad" -> text.replace(Regex("(?<!\r)\n"), "\r\n")
        else -> text.replace("\r\n", "\n")
    }
}

internal fun getWhichCommand(): String = when {
    isWindowsMpp() -> "where"
    else -> "which"
}
