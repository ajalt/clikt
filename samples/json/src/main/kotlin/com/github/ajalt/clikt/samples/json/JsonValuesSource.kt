package com.github.ajalt.clikt.samples.json

import com.github.ajalt.clikt.core.CliktFileFormatError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.fileconfig.CliktValuesSource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElementTypeMismatchException
import kotlinx.serialization.json.JsonObject
import java.io.File

/**
 * A [CliktValuesSource] that uses Gson to parse files
 */
@UseExperimental(UnstableDefault::class)
class JsonValuesSource(
        private val file: File,
        private val requireValid: Boolean = false
) : CliktValuesSource {
    private var map: Map<String, Any> = emptyMap()

    override fun initialize() {
        if (!file.isFile) return

        try {
            map = Json.plain.parseJson(file.readText()) as? JsonObject
                    ?: throw JsonElementTypeMismatchException("root", "object")

        } catch (e: SerializationException) {
            if (requireValid) throw CliktFileFormatError(file.name, e.message
                    ?: "could not read file")
        }
    }

    override fun readValues(context: Context, key: String): List<CliktValuesSource.Invocation> {
        val parts = key.split(".")
        var cursor: Any? = map
        for (part in parts) {
            if (cursor !is Map<*, *>) return emptyList()
            cursor = cursor[part]
        }
        if (cursor == null) return emptyList()

        // This implementation interprets a list as multiple invocations, but you could also
        // implement it as a single invocation with multiple values.
        if (cursor is List<*>) return cursor.map { CliktValuesSource.Invocation.value(it) }
        return CliktValuesSource.Invocation.just(cursor)
    }

    override fun close() {
        map = emptyMap()
    }
}

/** Add one or more json files as configuration sources */
fun Context.ValuesSourceBuilder.json(vararg filePaths: String, requireValid: Boolean = false) {
    for (path in filePaths) add(JsonValuesSource(File(path), requireValid))
}
