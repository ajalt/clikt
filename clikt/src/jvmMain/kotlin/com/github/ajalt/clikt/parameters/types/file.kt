package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import java.io.File
import java.nio.file.Files

private fun pathType(context: Context, fileOkay: Boolean, folderOkay: Boolean): String = when {
    fileOkay && !folderOkay -> context.localization.pathTypeFile()
    !fileOkay && folderOkay -> context.localization.pathTypeDirectory()
    else -> context.localization.pathTypeOther()
}

private fun convertToFile(
        path: String,
        mustExist: Boolean,
        canBeFile: Boolean,
        canBeDir: Boolean,
        mustBeWritable: Boolean,
        mustBeReadable: Boolean,
        canBeSymlink: Boolean,
        context: Context,
        fail: (String) -> Unit
): File {
    val name = pathType(context, canBeFile, canBeDir)
    return with(context.localization) {
        File(path).also {
            if (mustExist && !it.exists()) fail(pathDoesNotExist(name, it.toString()))
            if (!canBeFile && it.isFile) fail(pathIsFile(name, it.toString()))
            if (!canBeDir && it.isDirectory) fail(pathIsDirectory(name, it.toString()))
            if (mustBeWritable && !it.canWrite()) fail(pathIsNotWritable(name, it.toString()))
            if (mustBeReadable && !it.canRead()) fail(pathIsNotReadable(name, it.toString()))
            if (!canBeSymlink && Files.isSymbolicLink(it.toPath())) fail(pathIsSymlink(name, it.toString()))
        }
    }
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
    return convert(completionCandidates = CompletionCandidates.Path) { str ->
        convertToFile(str, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, context) { fail(it) }
    }
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
    return convert("PATH", completionCandidates = CompletionCandidates.Path) { str ->
        convertToFile(str, mustExist, canBeFile, canBeDir, mustBeWritable, mustBeReadable, canBeSymlink, context) { fail(it) }
    }
}
