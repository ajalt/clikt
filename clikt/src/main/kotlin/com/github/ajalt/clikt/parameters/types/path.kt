package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

private fun pathType(fileOkay: Boolean, folderOkay: Boolean): String = when {
    fileOkay && !folderOkay -> "File"
    !fileOkay && folderOkay -> "Directory"
    else -> "Path"
}

private fun convertToPath(path: String,
                          exists: Boolean,
                          fileOkay: Boolean,
                          folderOkay: Boolean,
                          writable: Boolean,
                          readable: Boolean,
                          fileSystem: FileSystem,
                          fail: (String) -> Unit): Path {
    val name = pathType(fileOkay, folderOkay)
    return fileSystem.getPath(path).also {
        if (exists && !Files.exists(it)) fail("$name \"$it\" does not exist.")
        if (!fileOkay && Files.isRegularFile(it)) fail("$name \"$it\" is a file.")
        if (!folderOkay && Files.isDirectory(it)) fail("$name \"$it\" is a directory.")
        if (writable && !Files.isWritable(it)) fail("$name \"$it\" is not writable.")
        if (readable && !Files.isReadable(it)) fail("$name \"$it\" is not readable.")
    }
}

/**
 * Convert the argument to a [Path].
 *
 * @param exists If true, fail if the given path does not exist
 * @param fileOkay If false, fail if the given path is a file
 * @param folderOkay If false, fail if the given path is a directory
 * @param writable If true, fail if the given path is not writable
 * @param readable If true, fail if the given path is not readable
 * @param fileSystem If specified, the [FileSystem] with which to resolve paths.
 */
fun RawArgument.path(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false,
        fileSystem: FileSystem = FileSystems.getDefault()
): ProcessedArgument<Path, Path> {
    return convert(completionCandidates = CompletionCandidates.Path) {
        convertToPath(it, exists, fileOkay, folderOkay, writable, readable, fileSystem) { fail(it) }
    }
}

/**
 * Convert the option to a [Path].
 *
 * @param exists If true, fail if the given path does not exist
 * @param fileOkay If false, fail if the given path is a file
 * @param folderOkay If false, fail if the given path is a directory
 * @param writable If true, fail if the given path is not writable
 * @param readable If true, fail if the given path is not readable
 * @param fileSystem If specified, the [FileSystem] with which to resolve paths.
 */
fun RawOption.path(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false,
        fileSystem: FileSystem = FileSystems.getDefault()
): NullableOption<Path, Path> {
    val name = pathType(fileOkay, folderOkay)
    val split = if (TermUi.isWindows) Regex.fromLiteral(";") else Regex.fromLiteral(":")
    return convert(name.toUpperCase(), envvarSplit = split, completionCandidates = CompletionCandidates.Path) {
        convertToPath(it, exists, fileOkay, folderOkay, writable, readable, fileSystem) { fail(it) }
    }
}
