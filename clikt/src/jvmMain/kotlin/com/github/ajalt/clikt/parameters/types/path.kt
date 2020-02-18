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

private fun Path.isSymlink(): Boolean {
    return Files.isSymbolicLink(this) || toRealPath() != toAbsolutePath()
}

private fun convertToPath(
        path: String,
        exists: Boolean,
        fileOkay: Boolean,
        folderOkay: Boolean,
        writable: Boolean,
        readable: Boolean,
        canBeSymlink: Boolean,
        fileSystem: FileSystem,
        fail: (String) -> Unit
): Path {
    val name = pathType(fileOkay, folderOkay)
    val it = when {
        path.startsWith("~") && !System.getProperty("user.home").isNullOrBlank() -> {
            fileSystem.getPath(System.getProperty("user.home") + path.drop(1))
        }
        else -> fileSystem.getPath(path)
    }
    if (exists && !Files.exists(it)) fail("$name \"$it\" does not exist.")
    if (!fileOkay && Files.isRegularFile(it)) fail("$name \"$it\" is a file.")
    if (!folderOkay && Files.isDirectory(it)) fail("$name \"$it\" is a directory.")
    if (writable && !Files.isWritable(it)) fail("$name \"$it\" is not writable.")
    if (readable && !Files.isReadable(it)) fail("$name \"$it\" is not readable.")
    if (!canBeSymlink && it.isSymlink()) fail("$name \"$it\" is a symlink.")
    return fileSystem.getPath(path)
}

// This overload exists so that calls to `file()` aren't marked as deprecated.
// Remove once the deprecated function is removed.
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
@Suppress("KDocUnresolvedReference")
fun RawArgument.path(fileSystem: FileSystem = FileSystems.getDefault()): ProcessedArgument<Path, Path> {
    return path(mustExist = false, fileSystem = fileSystem)
}

@Deprecated("Parameters have been renamed. All arguments must be called by name to remove this warning.", ReplaceWith(
        "this.path(mustExist=exists, canBeFile=fileOkay, canBeDir=folderOkay, mustBeWritable=writable, mustBeReadable=readable, fileSystem=fileSystem)"
))
fun RawArgument.path(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false,
        fileSystem: FileSystem = FileSystems.getDefault()
): ProcessedArgument<Path, Path> {
    return path(exists, fileOkay, folderOkay, writable, readable, true, fileSystem)
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
    return convert(completionCandidates = CompletionCandidates.Path) {
        convertToPath(it, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, fileSystem) { fail(it) }
    }
}

// This overload exists so that calls to `file()` aren't marked as deprecated.
// Remove once the deprecated function is removed.
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
@Suppress("KDocUnresolvedReference")
fun RawOption.path(fileSystem: FileSystem = FileSystems.getDefault()): NullableOption<Path, Path> {
    return path(mustExist = false, fileSystem = fileSystem)
}

@Deprecated("Parameters have been renamed. All arguments must be called by name to remove this warning.", ReplaceWith(
        "this.path(mustExist=exists, canBeFile=fileOkay, canBeDir=folderOkay, mustBeWritable=writable, mustBeReadable=readable, fileSystem=fileSystem)"
))
fun RawOption.path(
        exists: Boolean = false,
        fileOkay: Boolean = true,
        folderOkay: Boolean = true,
        writable: Boolean = false,
        readable: Boolean = false,
        fileSystem: FileSystem = FileSystems.getDefault()
): NullableOption<Path, Path> {
    return path(exists, fileOkay, folderOkay, writable, readable, true, fileSystem)
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
    val split = if (TermUi.isWindows) Regex.fromLiteral(";") else Regex.fromLiteral(":")
    return convert(name.toUpperCase(), envvarSplit = split, completionCandidates = CompletionCandidates.Path) {
        convertToPath(it, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, fileSystem) { fail(it) }
    }
}
