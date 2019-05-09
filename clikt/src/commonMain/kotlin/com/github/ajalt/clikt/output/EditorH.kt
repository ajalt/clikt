package com.github.ajalt.clikt.output

internal expect class Editor(
        editorPath: String?,
        env: Map<String, String>,
        requireSave: Boolean,
        extension: String
) {
    fun editFile(filename: String)
    fun edit(text: String): String?
}
