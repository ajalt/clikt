package com.github.ajalt.clikt.fileconfig

import com.github.ajalt.clikt.core.CliktFileFormatError
import com.github.ajalt.clikt.core.Context
import java.io.File
import java.util.*

/**
 * A [CliktValuesSource] that reads values from a Java Properties file.
 *
 * @property file The file to read from.
 * @property requireValid If true, a [CliktFileFormatError] will be thrown if the file doesn't parse correctly.
 */
class PropertiesFileValuesSource(
        private val file: File,
        private val requireValid: Boolean = false
) : CliktValuesSource {
    private val properties = Properties()

    override fun initialize() {
        if (!file.isFile) return
        try {
            file.bufferedReader().use { properties.load(it) }
        } catch (e: Throwable) {
            if (requireValid) throw CliktFileFormatError(file.name, e.message ?: "could not read file")
        }
    }

    override fun readValues(context: Context, key: String): List<CliktValuesSource.Invocation> {
        return properties.getProperty(key)?.let { CliktValuesSource.Invocation.just(it) } ?: emptyList()
    }

    override fun close() {
        properties.clear()
    }
}

/**
 * Add one or more Properties files as value sources.
 *
 * @param filePaths Paths to files to be read, in the order that they should be searched. If no file
 *   exists at a given path, it is skipped.
 * @param requireValid If true, an error will be reported if a file fails to parse. Otherwise the
 *   file is skipped.
 */
fun Context.ValuesSourceBuilder.javaProperties(vararg filePaths: File, requireValid: Boolean = false) {
    for (path in filePaths) add(PropertiesFileValuesSource(path, requireValid))
}

/**
 * Add one or more Properties files as value sources.
 *
 * @param filePaths Paths to files to be read, in the order that they should be searched. If no file
 *   exists at a given path, it is skipped.
 * @param requireValid If true, an error will be reported if a file fails to parse. Otherwise the
 *   file is skipped.
 */
fun Context.ValuesSourceBuilder.javaProperties(vararg filePaths: String, requireValid: Boolean = false) {
    for (path in filePaths) add(PropertiesFileValuesSource(File(path), requireValid))
}
