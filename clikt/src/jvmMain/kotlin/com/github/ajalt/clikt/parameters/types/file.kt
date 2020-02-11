package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import java.io.File

private fun pathType(fileOkay: Boolean, folderOkay: Boolean): String = when {
    fileOkay && !folderOkay -> "File"
    !fileOkay && folderOkay -> "Directory"
    else -> "Path"
}

private fun convertToFile(path: String,
                          exists: Boolean,
                          fileOkay: Boolean,
                          folderOkay: Boolean,
                          writable: Boolean,
                          readable: Boolean,
                          fail: (String) -> Unit): File {
    val name = pathType(fileOkay, folderOkay)
    return File(path).also {
        if (exists && !it.exists()) fail("$name \"$it\" does not exist.")
        if (!fileOkay && it.isFile) fail("$name \"$it\" is a file.")
        if (!folderOkay && it.isDirectory) fail("$name \"$it\" is a directory.")
        if (writable && !it.canWrite()) fail("$name \"$it\" is not writable.")
        if (readable && !it.canRead()) fail("$name \"$it\" is not readable.")
    }
}

/**
 * Convert the argument to a [File].
 *
 * @param exists If true, fail if the given path does not exist
 * @param fileOkay If false, fail if the given path is a file
 * @param folderOkay If false, fail if the given path is a directory
 * @param writable If true, fail if the given path is not writable
 * @param readable If true, fail if the given path is not readable
 */
fun RawArgument.file(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false
): ProcessedArgument<File, File> {
    return convert(completionCandidates = CompletionCandidates.Path) {
        convertToFile(it, exists, fileOkay, folderOkay, writable, readable) { fail(it) }
    }
}

/**
 * Convert the option to a [File].
 *
 * @param exists If true, fail if the given path does not exist
 * @param fileOkay If false, fail if the given path is a file
 * @param folderOkay If false, fail if the given path is a directory
 * @param writable If true, fail if the given path is not writable
 * @param readable If true, fail if the given path is not readable
 */
fun RawOption.file(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false
): NullableOption<File, File> {
    val name = pathType(fileOkay, folderOkay)
    val split = if (TermUi.isWindows) Regex.fromLiteral(";") else Regex.fromLiteral(":")
    return convert(name.toUpperCase(), envvarSplit = split, completionCandidates = CompletionCandidates.Path) {
        convertToFile(it, exists, fileOkay, folderOkay, writable, readable) { fail(it) }
    }
}
