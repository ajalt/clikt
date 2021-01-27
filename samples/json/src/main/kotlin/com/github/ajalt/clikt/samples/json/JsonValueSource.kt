package com.github.ajalt.clikt.samples.json

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.sources.ValueSource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.io.File

/**
 * A [ValueSource] that uses Kotlin serialization to parse JSON files
 */
class JsonValueSource(
        private val root: JsonObject
) : ValueSource {
    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        var cursor: JsonElement? = root
        val parts = option.valueSourceKey?.split(".")
                ?: context.commandNameWithParents().drop(1) + ValueSource.name(option)
        for (part in parts) {
            if (cursor !is JsonObject) return emptyList()
            cursor = cursor[part]
        }
        if (cursor == null) return emptyList()

        // This implementation interprets a list as multiple invocations, but you could also
        // implement it as a single invocation with multiple values.
        if (cursor is JsonArray) return cursor.map { ValueSource.Invocation.value(it) }
        return ValueSource.Invocation.just(cursor)
    }

    companion object {
        fun from(file: File, requireValid: Boolean = false): JsonValueSource {
            if (!file.isFile) return JsonValueSource(JsonObject(emptyMap()))

            val json = try {
                Json.parseToJsonElement(file.readText()) as? JsonObject
                        ?: throw InvalidFileFormat(file.path, "object expected", 1)
            } catch (e: SerializationException) {
                if (requireValid) throw InvalidFileFormat(file.name, e.message ?: "could not read file")
                JsonObject(emptyMap())
            }
            return JsonValueSource(json)
        }

        fun from(file: String, requireValid: Boolean = false): JsonValueSource = from(File(file), requireValid)
    }
}
