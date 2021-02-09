package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import java.io.IOException
import java.io.InputStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files

// region ========== Options ==========

/**
 * Convert the option to an [InputStream].
 *
 * The value given on the command line must be either a path to a readable file, or `-`. If `-` is
 * given, stdin will be used.
 *
 * If stdin is used, the resulting [InputStream] will be a proxy for [System. in] that will not close
 * the underlying stream. So you can always [close][InputStream.close] the resulting stream without
 * worrying about accidentally closing [System. in].
 */
fun RawOption.inputStream(
    fileSystem: FileSystem = FileSystems.getDefault(),
): NullableOption<InputStream, InputStream> {
    return convert({ localization.fileMetavar() }, CompletionCandidates.Path) { s ->
        convertToInputStream(s, fileSystem, context) { fail(it) }
    }
}

/**
 * Use `-` as the default value for an [inputStream] option.
 */
fun NullableOption<InputStream, InputStream>.defaultStdin(): OptionWithValues<InputStream, InputStream, InputStream> {
    return default(UnclosableInputStream(System.`in`), "-")
}

// endregion
// region ========== Arguments ==========

/**
 * Convert the argument to an [InputStream].
 *
 * The value given on the command line must be either a path to a readable file, or `-`. If `-` is
 * given, stdin will be used.
 *
 * If stdin is used, the resulting [InputStream] will be a proxy for [System. in] that will not close
 * the underlying stream. So you can always [close][InputStream.close] the resulting stream without
 * worrying about accidentally closing [System. in].
 */
fun RawArgument.inputStream(
    fileSystem: FileSystem = FileSystems.getDefault(),
): ProcessedArgument<InputStream, InputStream> {
    return convert(completionCandidates = CompletionCandidates.Path) { s ->
        convertToInputStream(s, fileSystem, context) { fail(it) }
    }
}

/**
 * Use `-` as the default value for an [inputStream] argument.
 */
fun ProcessedArgument<InputStream, InputStream>.defaultStdin(): ArgumentDelegate<InputStream> {
    return default(UnclosableInputStream(System.`in`))
}

// endregion

/**
 * Checks whether this stream was returned from an [inputStream] parameter, and that it is
 * reading from [System. in] (because `-` was given, or no value was given and the parameter uses
 * [defaultStdin]).
 */
val InputStream.isCliktParameterDefaultStdin: Boolean
    get() = this is UnclosableInputStream

private fun convertToInputStream(
    s: String,
    fileSystem: FileSystem,
    context: Context,
    fail: (String) -> Unit,
): InputStream {
    return if (s == "-") {
        UnclosableInputStream(System.`in`)
    } else {
        val path = convertToPath(
            path = s,
            mustExist = true,
            canBeFile = true,
            canBeFolder = false,
            mustBeWritable = false,
            mustBeReadable = true,
            canBeSymlink = true,
            fileSystem = fileSystem,
            context = context,
            fail = fail
        )
        Files.newInputStream(path)
    }
}

private class UnclosableInputStream(private var delegate: InputStream?) : InputStream() {
    private val stream get() = delegate ?: throw IOException("Stream closed")
    override fun available(): Int = stream.available()
    override fun read(): Int = stream.read()
    override fun read(b: ByteArray, off: Int, len: Int): Int = stream.read(b, off, len)
    override fun skip(n: Long): Long = stream.skip(n)
    override fun reset() = stream.reset()
    override fun markSupported(): Boolean = stream.markSupported()
    override fun mark(readlimit: Int) {
        stream.mark(readlimit)
    }

    override fun close() {
        delegate = null
    }
}

// endregion
