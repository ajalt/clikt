package com.github.ajalt.clikt.samples.json

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.FileFormatError
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.sources.ExperimentalValueSourceApi
import com.github.ajalt.clikt.sources.MapValueSource
import com.github.ajalt.clikt.sources.ValueSource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.io.File

/**
 * A [ValueSource] that uses Kotlin serialization to parse JSON files
 */
@OptIn(ExperimentalValueSourceApi::class)
class JsonValueSource(
        private val root: JsonObject
) : ValueSource {
    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        var cursor: JsonElement? = root
        for (part in context.commandNameWithParents().drop(1) + ValueSource.name(option)) {
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
        @OptIn(UnstableDefault::class)
        fun from(file: File, requireValid: Boolean = false): JsonValueSource {
            if (!file.isFile) return JsonValueSource(JsonObject(emptyMap()))

            val json = try {
                Json.plain.parseJson(file.readText()) as? JsonObject
                        ?: throw FileFormatError(file.path, "object expected", 1)
            } catch (e: SerializationException) {
                if (requireValid) throw FileFormatError(file.name, e.message ?: "could not read file")
                JsonObject(emptyMap())
            }
            return JsonValueSource(json)
        }

        fun from(file: String, requireValid: Boolean = false): JsonValueSource = from(File(file), requireValid)
    }
}
