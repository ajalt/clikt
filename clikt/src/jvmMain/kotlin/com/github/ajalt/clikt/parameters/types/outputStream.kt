package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import java.io.IOException
import java.io.OutputStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption.*

// region ========== Options ==========

/**
 * Convert the option to an [OutputStream].
 *
 * The value given on the command line must be either a path to a writable file, or `-`. If `-` is
 * given, stdout will be used.
 *
 * If stdout is used, the resulting [OutputStream] will be a proxy for [System.out] that will not close
 * the underlying stream. So you can always [close][OutputStream.close] the resulting stream without
 * worrying about accidentally closing [System.out].
 *
 * @param createIfNotExist If false, an error will be reported if the given value doesn't exist. By default, the file will be created.
 * @param truncateExisting If true, existing files will be truncated when opened. By default, the file will be appended to.
 */
fun RawOption.outputStream(
    createIfNotExist: Boolean = true,
    truncateExisting: Boolean = false,
    fileSystem: FileSystem = FileSystems.getDefault(),
): NullableOption<OutputStream, OutputStream> {
    return convert({ localization.fileMetavar() }, CompletionCandidates.Path) { s ->
        convertToOutputStream(s, createIfNotExist, truncateExisting, fileSystem, context) { fail(it) }
    }
}

/**
 * Use `-` as the default value for an [outputStream] option.
 */
fun NullableOption<OutputStream, OutputStream>.defaultStdout(): OptionWithValues<OutputStream, OutputStream, OutputStream> {
    return default(UnclosableOutputStream(System.out), "-")
}

// endregion
// region ========== Arguments ==========

/**
 * Convert the argument to an [OutputStream].
 *
 * The value given on the command line must be either a path to a writable file, or `-`. If `-` is
 * given, stdout will be used.
 *
 * If stdout is used, the resulting [OutputStream] will be a proxy for [System.out] that will not close
 * the underlying stream. So you can always [close][OutputStream.close] the resulting stream without
 * worrying about accidentally closing [System.out].
 *
 * @param createIfNotExist If false, an error will be reported if the given value doesn't exist. By default, the file will be created.
 * @param truncateExisting If true, existing files will be truncated when opened. By default, the file will be appended to.
 */
fun RawArgument.outputStream(
    createIfNotExist: Boolean = true,
    truncateExisting: Boolean = false,
    fileSystem: FileSystem = FileSystems.getDefault(),
): ProcessedArgument<OutputStream, OutputStream> {
    return convert(completionCandidates = CompletionCandidates.Path) { s ->
        convertToOutputStream(s, createIfNotExist, truncateExisting, fileSystem, context) { fail(it) }
    }
}

/**
 * Use `-` as the default value for an [outputStream] argument.
 */
fun ProcessedArgument<OutputStream, OutputStream>.defaultStdout(): ArgumentDelegate<OutputStream> {
    return default(UnclosableOutputStream(System.out))
}

// endregion

/**
 * Checks whether this stream was returned from an [outputStream] parameter, and that it is
 * writing to [System.out] (because `-` was given, or no value was given and the parameter uses
 * [defaultStdout]).
 */
val OutputStream.isCliktParameterDefaultStdout: Boolean
    get() = this is UnclosableOutputStream

private fun convertToOutputStream(
    s: String,
    createIfNotExist: Boolean,
    truncateExisting: Boolean,
    fileSystem: FileSystem,
    context: Context,
    fail: (String) -> Unit,
): OutputStream {
    return if (s == "-") {
        UnclosableOutputStream(System.out)
    } else {
        val path = convertToPath(
            s,
            mustExist = !createIfNotExist,
            canBeFile = true,
            canBeFolder = false,
            mustBeWritable = !createIfNotExist,
            mustBeReadable = false,
            canBeSymlink = true,
            fileSystem = fileSystem,
            context = context
        ) { fail(it) }
        val openType = if (truncateExisting) TRUNCATE_EXISTING else APPEND
        val options = arrayOf(WRITE, CREATE, openType)
        Files.newOutputStream(path, *options)
    }
}

private class UnclosableOutputStream(private var delegate: OutputStream?) : OutputStream() {
    private val stream get() = delegate ?: throw IOException("Stream closed")

    override fun write(b: Int) = stream.write(b)
    override fun write(b: ByteArray) = stream.write(b)
    override fun write(b: ByteArray, off: Int, len: Int) = stream.write(b, off, len)
    override fun flush() = stream.flush()
    override fun close() {
        delegate?.flush()
        delegate = null
    }
}
