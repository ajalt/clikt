package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import java.io.File
import java.nio.file.FileSystems

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
    return convert(CompletionCandidates.Path) { str ->
        convertToPath(
            path = str,
            mustExist = mustExist,
            canBeFile = canBeFile,
            canBeFolder = canBeDir,
            mustBeWritable = mustBeWritable,
            mustBeReadable = mustBeReadable,
            canBeSymlink = canBeSymlink,
            fileSystem = FileSystems.getDefault(),
            context = context
        ) { fail(it) }.toFile()
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
    return convert({ localization.pathMetavar() }, CompletionCandidates.Path) { str ->
        convertToPath(
            path = str,
            mustExist = mustExist,
            canBeFile = canBeFile,
            canBeFolder = canBeDir,
            mustBeWritable = mustBeWritable,
            mustBeReadable = mustBeReadable,
            canBeSymlink = canBeSymlink,
            fileSystem = FileSystems.getDefault(),
            context = context
        ) { fail(it) }.toFile()
    }
}
