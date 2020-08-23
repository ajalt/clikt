package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.Context
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

private fun pathType(context: Context, fileOkay: Boolean, folderOkay: Boolean): String = when {
    fileOkay && !folderOkay -> context.localization.pathTypeFile()
    !fileOkay && folderOkay -> context.localization.pathTypeDirectory()
    else -> context.localization.pathTypeOther()
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
        context: Context,
        fail: (String) -> Unit
): Path {
    val name = pathType(context, canBeFile, canBeFolder)
    return with(context.localization) {
        fileSystem.getPath(path).also {
            if (mustExist && !Files.exists(it)) fail(pathDoesNotExist(name, it.toString()))
            if (!canBeFile && Files.isRegularFile(it)) fail(pathIsFile(name, it.toString()))
            if (!canBeFolder && Files.isDirectory(it)) fail(pathIsDirectory(name, it.toString()))
            if (mustBeWritable && !Files.isWritable(it)) fail(pathIsNotWritable(name, it.toString()))
            if (mustBeReadable && !Files.isReadable(it)) fail(pathIsNotReadable(name, it.toString()))
            if (!canBeSymlink && Files.isSymbolicLink(it)) fail(pathIsSymlink(name, it.toString()))
        }
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
        convertToPath(str, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, fileSystem, context) { fail(it) }
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
    return convert({ localization.pathMetavar() }, CompletionCandidates.Path) { str ->
        convertToPath(str, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, fileSystem, context) { fail(it) }
    }
}
