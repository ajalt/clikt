package com.github.ajalt.clikt.samples.json

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.sources.ValueSource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * A [ValueSource] that uses Kotlin serialization to parse JSON files
 */
class JsonValueSource(
    private val root: JsonObject,
    private val referencePrefix: String,
) : ValueSource {
    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        var cursor: JsonElement? = root
        val parts = option.valueSourceKey?.split(".")
            ?: (context.commandNameWithParents().drop(1) + ValueSource.name(option))
        for (part in parts) {
            if (cursor !is JsonObject) return emptyList()
            cursor = cursor[part]
        }
        if (cursor == null) return emptyList()

        try {
            val jsonReference = referencePrefix + buildJsonPointer(parts)
            // This implementation interprets a list as multiple invocations, but you could also
            // implement it as a single invocation with multiple values.
            if (cursor is JsonArray) return cursor.map {
                ValueSource.Invocation.value(value = it.jsonPrimitive.content, location = jsonReference)
            }
            return ValueSource.Invocation.just(value = cursor.jsonPrimitive.content, location = jsonReference)
        } catch (e: IllegalArgumentException) {
            // This implementation skips invalid values, but you could handle them differently.
            return emptyList()
        }
    }

    private fun buildJsonPointer(parts: List<String>): String =
        parts.joinToString(separator = "/", prefix = "/") { it.replace("~", "~0").replace("/", "~1") }

    companion object {
        fun from(file: File, requireValid: Boolean = false): JsonValueSource {
            if (!file.isFile) return JsonValueSource(JsonObject(emptyMap()), referencePrefix = "")

            val json = try {
                Json.parseToJsonElement(file.readText()) as? JsonObject
                    ?: throw InvalidFileFormat(file.path, "object expected", 1)
            } catch (e: SerializationException) {
                if (requireValid) {
                    throw InvalidFileFormat(file.name, e.message ?: "could not read file")
                }
                JsonObject(emptyMap())
            }
            return JsonValueSource(json, referencePrefix = file.invariantSeparatorsPath + "#")
        }

        fun from(file: String, requireValid: Boolean = false): JsonValueSource {
            return from(File(file), requireValid)
        }
    }
}
