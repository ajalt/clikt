package com.github.ajalt.clikt.fileconfig

import com.github.ajalt.clikt.core.Context

/**
 * A [CliktValuesSource] that looks for values in multiple other sources.
 */
class ChainedValuesSource(private val sources: List<CliktValuesSource>) : CliktValuesSource {
    init {
        require(sources.isNotEmpty()) { "Must provide configuration sources" }
    }

    override fun initialize() {
        for (source in sources) {
            source.initialize()
        }
    }

    override fun readValues(context: Context, key: String): List<CliktValuesSource.Invocation> {
        return sources.asSequence()
                .map { it.readValues(context, key) }
                .firstOrNull { it.isNotEmpty() }
                ?: emptyList()
    }

    override fun close() {
        for (source in sources) {
            source.close()
        }
    }
}
