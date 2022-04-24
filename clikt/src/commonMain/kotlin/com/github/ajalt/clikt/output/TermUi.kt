package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.mpp.isWindowsMpp

object TermUi {
    /**
     * Edit [text] in the [editor].
     *
     * This blocks until the editor is closed.
     *
     * @param text The text to edit.
     * @param editor The path to the editor to use. Defaults to automatic detection.
     * @param env Environment variables to forward to the editor.
     * @param requireSave If the editor is closed without saving, null will be returned if true, otherwise
     *   [text] will be returned.
     * @param extension The extension of the temporary file that the editor will open. This can affect syntax
     *   coloring etc.
     * @return The edited text, or null if [requireSave] is true and the editor was closed without saving.
     * @throws CliktError if the editor cannot be opened.
     */
    fun editText(
        text: String,
        editor: String? = null,
        env: Map<String, String> = emptyMap(),
        requireSave: Boolean = false,
        extension: String = ".txt",
    ): String? {
        return createEditor(editor, env, requireSave, extension).edit(text)
    }

    /**
     * Edit the file with [filename] in the [editor].
     *
     * @see editText for usage and parameter descriptions.
     */
    fun editFile(
        filename: String,
        editor: String? = null,
        env: Map<String, String> = emptyMap(),
        requireSave: Boolean = false,
        extension: String = ".txt",
    ) {
        createEditor(editor, env, requireSave, extension).editFile(filename)
    }

    /** True if the current platform is a version of windows. */
    val isWindows: Boolean get() = isWindowsMpp()
}
