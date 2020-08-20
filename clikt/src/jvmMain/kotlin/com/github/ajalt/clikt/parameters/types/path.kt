package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
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

internal fun convertToPath(
        path: String,
        mustExist: Boolean,
        canBeFile: Boolean,
        canBeFolder: Boolean,
        mustBeWritable: Boolean,
        mustBeReadable: Boolean,
        canBeSymlink: Boolean,
        fileSystem: FileSystem,
        fail: (String) -> Unit
): Path {
    val name = pathType(canBeFile, canBeFolder)
    return fileSystem.getPath(path).also {
        if (mustExist && !Files.exists(it)) fail("$name \"$it\" does not exist.")
        if (!canBeFile && Files.isRegularFile(it)) fail("$name \"$it\" is a file.")
        if (!canBeFolder && Files.isDirectory(it)) fail("$name \"$it\" is a directory.")
        if (mustBeWritable && !Files.isWritable(it)) fail("$name \"$it\" is not writable.")
        if (mustBeReadable && !Files.isReadable(it)) fail("$name \"$it\" is not readable.")
        if (!canBeSymlink && Files.isSymbolicLink(it)) fail("$name \"$it\" is a symlink.")
    }
}

/**
 * Convert the argument to a [Path].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 * @param fileSystem The [FileSystem] with which to resolve paths
 * @param canBeSymlink If false, fail if the given path is a symlink
 */
fun RawArgument.path(
        mustExist: Boolean = false,
        canBeFile: Boolean = true,
        canBeDir: Boolean = true,
        mustBeWritable: Boolean = false,
        mustBeReadable: Boolean = false,
        canBeSymlink: Boolean = true,
        fileSystem: FileSystem = FileSystems.getDefault()
): ProcessedArgument<Path, Path> {
    return convert(completionCandidates = CompletionCandidates.Path) { str ->
        convertToPath(str, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, fileSystem) { fail(it) }
    }
}

/**
 * Convert the option to a [Path].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 * @param fileSystem The [FileSystem] with which to resolve paths.
 * @param canBeSymlink If false, fail if the given path is a symlink
 */
fun RawOption.path(
        mustExist: Boolean = false,
        canBeFile: Boolean = true,
        canBeDir: Boolean = true,
        mustBeWritable: Boolean = false,
        mustBeReadable: Boolean = false,
        canBeSymlink: Boolean = true,
        fileSystem: FileSystem = FileSystems.getDefault()
): NullableOption<Path, Path> {
    val name = pathType(canBeFile, canBeDir)
    return convert(name.toUpperCase(), completionCandidates = CompletionCandidates.Path) { str ->
        convertToPath(str, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, fileSystem) { fail(it) }
    }
}
