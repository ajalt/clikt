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

private fun File.isSymlink(): Boolean {
    return toPath().toRealPath().toFile() != absoluteFile
}

private fun convertToFile(
        path: String,
        mustExist: Boolean,
        canBeFile: Boolean,
        canBeDir: Boolean,
        mustBeWritable: Boolean,
        mustBeReadable: Boolean,
        canBeSymlink: Boolean,
        fail: (String) -> Unit
): File {
    val name = pathType(canBeFile, canBeDir)
    return File(path).also {
        if (mustExist && !it.exists()) fail("$name \"$it\" does not exist.")
        if (!canBeFile && it.isFile) fail("$name \"$it\" is a file.")
        if (!canBeDir && it.isDirectory) fail("$name \"$it\" is a directory.")
        if (mustBeWritable && !it.canWrite()) fail("$name \"$it\" is not writable.")
        if (mustBeReadable && !it.canRead()) fail("$name \"$it\" is not readable.")
        if (!canBeSymlink && it.isSymlink()) fail("$name \"$it\" is a symlink.")
    }
}

// This overload exists so that calls to `file()` aren't marked as deprecated.
// Remove once the deprecated function is removed.
/**
 * Convert the argument to a [File].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 * @param canBeSymlink If false, fail if the given path is a symlink
 */
@Suppress("KDocUnresolvedReference")
fun RawArgument.file(): ProcessedArgument<File, File> = file(mustExist = false)

@Deprecated("Parameters have been renamed. All arguments must be called by name to remove this warning.", ReplaceWith(
        "this.file(mustExist=exists, canBeFile=fileOkay, canBeDir=folderOkay, mustBeWritable=writable, mustBeReadable=readable)"
))
fun RawArgument.file(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false
): ProcessedArgument<File, File> {
    return file(exists, fileOkay, folderOkay, writable, readable, canBeSymlink = true)
}

/**
 * Convert the argument to a [File].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 * @param canBeSymlink If false, fail if the given path is a symlink
 */
fun RawArgument.file(
        mustExist: Boolean = false,
        canBeFile: Boolean = true,
        canBeDir: Boolean = true,
        mustBeWritable: Boolean = false,
        mustBeReadable: Boolean = false,
        canBeSymlink: Boolean = true
): ProcessedArgument<File, File> {
    return convert(completionCandidates = CompletionCandidates.Path) {
        convertToFile(it, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink) { fail(it) }
    }
}

// This overload exists so that calls to `file()` aren't marked as deprecated.
// Remove once the deprecated function is removed.
/**
 * Convert the option to a [File].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 */
@Suppress("KDocUnresolvedReference")
fun RawOption.file(): NullableOption<File, File> = file(mustExist = false)

@Deprecated("Parameters have been renamed. All arguments must be called by name to remove this warning.", ReplaceWith(
        "this.file(mustExist=exists, canBeFile=fileOkay, canBeDir=folderOkay, mustBeWritable=writable, mustBeReadable=readable)"
))
fun RawOption.file(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false
): NullableOption<File, File> {
    return file(exists, fileOkay, folderOkay, writable, readable, canBeSymlink = true)
}

/**
 * Convert the option to a [File].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 * @param canBeSymlink If false, fail if the given path is a symlink
 */
fun RawOption.file(
        mustExist: Boolean = false,
        canBeFile: Boolean = true,
        canBeDir: Boolean = true,
        mustBeWritable: Boolean = false,
        mustBeReadable: Boolean = false,
        canBeSymlink: Boolean = true
): NullableOption<File, File> {
    val name = pathType(canBeFile, canBeDir)
    val split = if (TermUi.isWindows) Regex.fromLiteral(";") else Regex.fromLiteral(":")
    return convert(name.toUpperCase(), envvarSplit = split, completionCandidates = CompletionCandidates.Path) {
        convertToFile(it, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink) { fail(it) }
    }
}
