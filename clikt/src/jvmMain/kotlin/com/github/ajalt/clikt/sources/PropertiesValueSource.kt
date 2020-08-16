package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.parameters.options.Option
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * A [ValueSource] that reads values from a [Properties] object.
 */
object PropertiesValueSource {
    /**
     * Parse a properties [file] into a value source.
     *
     * If the [file] does not exist, an empty value source will be returned.
     *
     * @param file The file to read from.
     * @param requireValid If true, a [InvalidFileFormat] will be thrown if the file doesn't parse correctly.
     * @param getKey A function that will return the property key for a given option. You can use
     *   [ValueSource.getKey] for most use cases.
     */
    fun from(
            file: Path,
            requireValid: Boolean = false,
            getKey: (Context, Option) -> String = ValueSource.getKey(joinSubcommands = ".")
    ): ValueSource {
        val properties = Properties()
        if (Files.isRegularFile(file)) {
            try {
                Files.newInputStream(file).buffered().use { properties.load(it) }
            } catch (e: Throwable) {
                if (requireValid) throw InvalidFileFormat(file.toString(), e.message ?: "could not read file")
            }
        }

        return from(properties, getKey)
    }

    /**
     * Parse a properties [file] into a value source.
     *
     * If the [file] does not exist, an empty value source will be returned.
     *
     * @param file The file to read from.
     * @param requireValid If true, a [InvalidFileFormat] will be thrown if the file doesn't parse correctly.
     * @param getKey A function that will return the property key for a given option. You can use
     *   [ValueSource.getKey] for most use cases.
     */
    fun from(
            file: File,
            requireValid: Boolean = false,
            getKey: (Context, Option) -> String = ValueSource.getKey(joinSubcommands = ".")
    ): ValueSource {
        val properties = Properties()
        if (file.isFile) {
            try {
                file.bufferedReader().use { properties.load(it) }
            } catch (e: Throwable) {
                if (requireValid) throw InvalidFileFormat(file.name, e.message ?: "could not read file")
            }
        }

        return from(properties, getKey)
    }

    /**
     * Parse a properties [file] into a value source.
     *
     * If the [file] does not exist, an empty value source will be returned.
     *
     * @param file The file to read from.
     * @param requireValid If true, a [InvalidFileFormat] will be thrown if the file doesn't parse correctly.
     * @param getKey A function that will return the property key for a given option. You can use
     *   [ValueSource.getKey] for most use cases.
     */
    fun from(
            file: String,
            requireValid: Boolean = false,
            getKey: (Context, Option) -> String = ValueSource.getKey(joinSubcommands = ".")
    ): ValueSource = from(File(file), requireValid, getKey)

    /**
     * Return a [ValueSource] that reads values from a [properties] object.
     *
     * The [properties] object is copied when this function is called; changes to the object will
     * not be reflected in the value source.
     *
     * @param properties The properties to read from.
     * @param getKey A function that will return the property key for a given option.
     */
    fun from(
            properties: Properties,
            getKey: (Context, Option) -> String = ValueSource.getKey(joinSubcommands = ".")
    ): ValueSource {
        val values = properties.entries.associate { it.key.toString() to it.value.toString() }
        return MapValueSource(values, getKey)
    }
}
